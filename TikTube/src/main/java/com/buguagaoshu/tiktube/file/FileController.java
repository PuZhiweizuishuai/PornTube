package com.buguagaoshu.tiktube.file;

import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.enums.FileTypeEnum;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.repository.FileRepository;
import com.buguagaoshu.tiktube.repository.impl.FileRepositoryInOSS;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.FileTableService;
import com.buguagaoshu.tiktube.utils.AesUtil;
import com.buguagaoshu.tiktube.utils.FileUtils;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import com.buguagaoshu.tiktube.vo.VditorFiles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 14:52
 *
 * 文件的上传与读取。
 *
 * 之所以放在 controller 包之外：播放视频时响应已预设 Content-Type 'video/mp4'，
 * 一旦此时抛异常，{@code TikTubeControllerAdvice} 返回的 ResponseDetails 会因为
 * 找不到匹配该 Content-Type 的 HttpMessageConverter 而报
 * HttpMessageNotWritableException，所以本类必须脱离那个 advice 的 basePackages，
 * 并把异常都在自己身上处理干净。
 */
@Slf4j
@Controller
public class FileController {

    /** 图片类文件（头像、首页大图、文章封面）大小上限，与 FileRepositoryImpl 的校验值一致 */
    private static final long MAX_IMAGE_SIZE = 2048000;

    private static final String MAX_IMAGE_SIZE_TEXT = "2MB";

    /** 播放授权 key 解密后的明文段数：用户ID#文件ID#过期时间戳#文件名 */
    private static final int PLAY_KEY_FIELDS = 3;

    /** 本地文件读缓存策略：视频按 Range 分段请求，public 缓存会绕开播放权限校验 */
    private static final String FILE_CACHE_CONTROL = "private, max-age=86400";

    private static final String CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

    /** 用户可上传 SVG，顶层打开时沙箱化，避免同源存储型 XSS */
    private static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";

    private final FileRepository fileRepository;

    private final ArticleService articleService;

    private final FileTableService fileTableService;

    private final FileRepositoryInOSS fileRepositoryInOSS;

    @Autowired
    public FileController(FileRepository fileRepository,
                          ArticleService articleService,
                          FileTableService fileTableService,
                          FileRepositoryInOSS fileRepositoryInOSS) {
        this.fileRepository = fileRepository;
        this.articleService = articleService;
        this.fileTableService = fileTableService;
        this.fileRepositoryInOSS = fileRepositoryInOSS;
    }

    /* ------------------------------------------------ 上传 ------------------------------------------------ */

    @PostMapping("/api/upload/article")
    @ResponseBody
    public VditorFiles save(@RequestParam(value = "file[]") MultipartFile[] files,
                            HttpServletRequest request) {
        return fileRepository.vditorFileSave(files, JwtUtil.getUserId(request));
    }

    @PostMapping("/api/upload/video")
    @ResponseBody
    public ResponseDetails saveVideo(@RequestParam(value = "file[]") MultipartFile[] files,
                                     HttpServletRequest request) {
        return upload(files, request, FileTypeEnum.VIDEO.getCode(), 0);
    }

    @PostMapping("/api/upload/photo")
    @ResponseBody
    public ResponseDetails savePhoto(@RequestParam(value = "file[]") MultipartFile[] files,
                                     HttpServletRequest request) {
        return upload(files, request, FileTypeEnum.PHOTO.getCode(), 0);
    }

    /**
     * 文章封面上传。
     * 与 {@code /api/upload/photo} 的区别：那条在 VipInterceptor 名单里属于 VIP 权益，
     * 而文章投稿是所有登录用户都能用的，所以封面单独走这条只做登录校验、并限制 2MB 的通道。
     */
    @PostMapping("/api/upload/article/cover")
    @ResponseBody
    public ResponseDetails saveArticleCover(@RequestParam(value = "file[]") MultipartFile[] files,
                                            HttpServletRequest request) {
        return upload(files, request, FileTypeEnum.PHOTO.getCode(), MAX_IMAGE_SIZE);
    }

    @PostMapping("/api/upload/avatar")
    @ResponseBody
    public ResponseDetails saveAvatar(@RequestParam(value = "file[]") MultipartFile[] files,
                                      HttpServletRequest request) {
        return upload(files, request, FileTypeEnum.AVATAR.getCode(), MAX_IMAGE_SIZE);
    }

    @PostMapping("/api/upload/top")
    @ResponseBody
    public ResponseDetails saveTop(@RequestParam(value = "file[]") MultipartFile[] files,
                                   HttpServletRequest request) {
        return upload(files, request, FileTypeEnum.TOP_IMAGE.getCode(), MAX_IMAGE_SIZE);
    }

    /**
     * 上传的公共流程：登录用户取值 → 可选的大小校验 → 存储 → 统一包装错误
     *
     * @param type    {@link FileTypeEnum} 的类型码，决定存储目录校验规则
     * @param maxSize 单文件大小上限，小于等于 0 表示不限制
     */
    private ResponseDetails upload(MultipartFile[] files, HttpServletRequest request, int type, long maxSize) {
        long userId = JwtUtil.getUserId(request);
        if (files.length == 0) {
            return ResponseDetails.ok(ReturnCodeEnum.DATA_VALID_EXCEPTION.getCode(), "请选择要上传的文件！");
        }
        if (maxSize > 0) {
            for (MultipartFile file : files) {
                if (file.getSize() > maxSize) {
                    return ResponseDetails.ok(ReturnCodeEnum.DATA_VALID_EXCEPTION.getCode(),
                            "图片大小不能超过 " + MAX_IMAGE_SIZE_TEXT);
                }
            }
        }
        try {
            List<FileTableEntity> list = fileRepository.videoAndPhotoSave(files, type, userId);
            // null 表示大小超限，空列表表示所有文件都没存成功；两者都不能让前端去取 data[0]
            if (list == null || list.isEmpty()) {
                return ResponseDetails.ok(ReturnCodeEnum.DATA_VALID_EXCEPTION.getCode(),
                        "上传失败，请检查文件格式与大小后重试！");
            }
            return ResponseDetails.ok().put("data", list);
        } catch (FileNotFoundException e) {
            return ResponseDetails.ok(ReturnCodeEnum.DATA_VALID_EXCEPTION).put("data", e.getMessage());
        }
    }

    /* ------------------------------------------------ 读取 ------------------------------------------------ */

    /**
     * 对象存储上的文件：校验播放权限后 302 到预签名地址
     */
    @GetMapping("/api/upload/{code}/oss/{year}/{month}/{day}/{filename:.+}")
    public ResponseEntity<Void> getOSS(@PathVariable(value = "code") Integer code,
                                       @PathVariable(value = "year") String year,
                                       @PathVariable(value = "month") String month,
                                       @PathVariable(value = "day") String day,
                                       @PathVariable(value = "filename") String filename,
                                       @RequestParam(value = "key", required = false) String key,
                                       HttpServletRequest request) {
        String date = year + "/" + month + "/" + day;
        // 路径片段校验，防止对象存储 key 被路径穿越篡改
        if (!FileUtils.isSafeSegment(filename) || !FileUtils.isSafeRelativePath(date)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (isVideo(filename) && !videoPlayable(filename, key, request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String url = fileRepositoryInOSS.getFileUrl(date + "/" + filename, code);
        if (url == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(FILE_CACHE_CONTROL);
        headers.add(HttpHeaders.LOCATION, url);
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    @GetMapping("/api/upload/file/{year}/{month}/{day}/{filename:.+}")
    public ResponseEntity<Resource> get(@PathVariable(value = "year") String year,
                                        @PathVariable(value = "month") String month,
                                        @PathVariable(value = "day") String day,
                                        @PathVariable(value = "filename") String filename,
                                        @RequestParam(value = "key", required = false) String key,
                                        HttpServletRequest request) {
        return getFile(year + "/" + month + "/" + day, filename, key, request);
    }

    @GetMapping("/api/upload/file/{date}/{filename:.+}")
    public ResponseEntity<Resource> get(@PathVariable(value = "date") String date,
                                        @PathVariable(value = "filename") String filename,
                                        @RequestParam(value = "key", required = false) String key,
                                        HttpServletRequest request) {
        return getFile(date, filename, key, request);
    }

    /**
     * 带 Range 的同名接口。播放器（安卓 Edge、Safari）seek 视频时会带 Range 请求头，
     * 只回 200 全量响应会让它反复重发请求，每次都重新查一遍文件表。
     * 返回类型必须写成 {@code ResponseEntity<ResourceRegion>}：
     * ResourceRegionHttpMessageConverter 只按声明的泛型判断能否写出，写成 {@code ?} 会报
     * No converter for [class ResourceRegion]。
     */
    @GetMapping(value = "/api/upload/file/{year}/{month}/{day}/{filename:.+}", headers = HttpHeaders.RANGE)
    public ResponseEntity<ResourceRegion> getRange(@PathVariable(value = "year") String year,
                                                   @PathVariable(value = "month") String month,
                                                   @PathVariable(value = "day") String day,
                                                   @PathVariable(value = "filename") String filename,
                                                   @RequestParam(value = "key", required = false) String key,
                                                   HttpServletRequest request) {
        return getRangeFile(year + "/" + month + "/" + day, filename, key, request);
    }

    @GetMapping(value = "/api/upload/file/{date}/{filename:.+}", headers = HttpHeaders.RANGE)
    public ResponseEntity<ResourceRegion> getRange(@PathVariable(value = "date") String date,
                                                   @PathVariable(value = "filename") String filename,
                                                   @RequestParam(value = "key", required = false) String key,
                                                   HttpServletRequest request) {
        return getRangeFile(date, filename, key, request);
    }

    private ResponseEntity<Resource> getFile(String date, String filename, String key, HttpServletRequest request) {
        Resource resource = openResource(date, filename, key, request);
        if (resource == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        HttpHeaders headers = fileHeaders(request, date, filename);
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    private ResponseEntity<ResourceRegion> getRangeFile(String date, String filename, String key,
                                                        HttpServletRequest request) {
        Resource resource = openResource(date, filename, key, request);
        if (resource == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        long contentLength;
        try {
            contentLength = resource.contentLength();
        } catch (IOException e) {
            log.warn("读取文件 {}/{} 失败: {}", date, filename, e.toString());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<HttpRange> ranges;
        try {
            ranges = HttpRange.parseRanges(request.getHeader(HttpHeaders.RANGE));
        } catch (Exception e) {
            ranges = List.of();
        }
        if (ranges.isEmpty()) {
            // 只支持 bytes 单位，其余一律按无法满足的区间处理
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + contentLength)
                    .build();
        }
        HttpRange range = ranges.get(0);
        long start = range.getRangeStart(contentLength);
        long end = range.getRangeEnd(contentLength);
        if (start < 0 || start >= contentLength || start > end) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + contentLength)
                    .build();
        }
        // Content-Range 与 Content-Length 由 ResourceRegionHttpMessageConverter 写入
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .headers(fileHeaders(request, date, filename))
                .body(new ResourceRegion(resource, start, end - start + 1));
    }

    /**
     * 校验路径与播放权限并打开本地文件，任何一步不通过都返回 null（统一按 404 响应，
     * 既不让客户端通过状态码区分「路径非法」和「文件不存在」，也不会出现空 body 的 200）
     */
    private Resource openResource(String date, String filename, String key, HttpServletRequest request) {
        if (!FileUtils.isSafeSegment(filename) || !FileUtils.isSafeRelativePath(date)) {
            return null;
        }
        try {
            Path path = fileRepository.load(date + "/" + filename);
            if (isVideo(filename) && !videoPlayable(filename, key, request)) {
                return null;
            }
            return new UrlResource(path.toUri());
        } catch (Exception e) {
            // 详细原因只记录日志，响应统一为 404，避免向客户端泄露内部路径、堆栈等信息
            log.warn("读取文件 {}/{} 失败: {}", date, filename, e.toString());
            return null;
        }
    }

    private HttpHeaders fileHeaders(HttpServletRequest request, String date, String filename) {
        String contentType = request.getServletContext().getMimeType(date + "/" + filename);
        MediaType mediaType = MediaType.parseMediaType(
                contentType == null || contentType.isEmpty() ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        // 上传内容与站点同源，禁止浏览器嗅探类型，并沙箱化被直接打开的文档（如 SVG）
        headers.add(CONTENT_TYPE_OPTIONS, "nosniff");
        headers.add(CONTENT_SECURITY_POLICY, "sandbox");
        headers.setCacheControl(FILE_CACHE_CONTROL);
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
        return headers;
    }

    /* -------------------------------------------- 播放权限校验 -------------------------------------------- */

    private boolean isVideo(String filename) {
        return FileTypeEnum.getFileType(FileTypeEnum.getFileSuffix(filename)) == FileTypeEnum.VIDEO;
    }

    /**
     * 视频区投稿的视频需要携带服务端下发的播放授权 key，其余文件一律放行
     *
     * @return true 表示当前请求可以读取该文件
     */
    private boolean videoPlayable(String filename, String key, HttpServletRequest request) {
        FileTableEntity fileTableEntity = fileTableService.findFileByFilename(filename);
        if (fileTableEntity == null) {
            return false;
        }
        // 非视频区投稿视频（封面、文章配图等）直接放行
        if (fileTableEntity.getArticleId() == null) {
            return true;
        }
        if (key == null) {
            return false;
        }
        try {
            // 第一个值是用户ID，如果访问时没有登录，则这个值是 -1
            // 第二个值是文件ID，进行文件查找
            // 第三个值是过期时间
            // 第四个是文件名
            String[] msg = AesUtil.getInstance().decrypt(key).split("#");
            if (msg.length < PLAY_KEY_FIELDS) {
                return false;
            }
            long userId = Long.parseLong(msg[0]);
            long fileId = Long.parseLong(msg[1]);
            long expire = Long.parseLong(msg[2]);
            if (System.currentTimeMillis() > expire) {
                log.warn("用户 {} 访问文件 id {} 时，播放 key 已于 {} 过期", userId, fileId, expire);
                return false;
            }
            // 解析出的文件ID不同，说明 key 被挪用来播放别的视频
            return fileTableEntity.getId().equals(fileId)
                    && articleService.hasThisVideoPlayPower(fileTableEntity, userId, request);
        } catch (Exception e) {
            // key 由客户端传入，格式或密文被篡改时解密与解析都会抛异常，一律按无权限处理
            log.warn("校验文件 {} 的播放授权失败: {}", filename, e.toString());
            return false;
        }
    }
}
