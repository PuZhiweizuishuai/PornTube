package com.buguagaoshu.porntube.file;

import com.buguagaoshu.porntube.config.WebConstant;
import com.buguagaoshu.porntube.entity.FileTableEntity;
import com.buguagaoshu.porntube.enums.FileTypeEnum;
import com.buguagaoshu.porntube.repository.FileRepository;
import com.buguagaoshu.porntube.service.ArticleService;
import com.buguagaoshu.porntube.service.FileTableService;
import com.buguagaoshu.porntube.utils.AesUtil;
import com.buguagaoshu.porntube.utils.JwtUtil;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import com.buguagaoshu.porntube.vo.VditorFiles;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 14:52
 * 在获取文件十回报
 *
 * org.springframework.http.converter.HttpMessageNotWritableException: No converter for [class com.buguagaoshu.porntube.vo.ResponseDetails] with preset Content-Type 'video/mp4'
 *
 * 异常， 目前还没有找到解决方案
 * 只能先把这个 FileController 从 controller 包中拿出来了。
 */
@Slf4j
@Controller
public class FileController {

    private final FileRepository fileRepository;

    private final ArticleService articleService;

    private final FileTableService fileTableService;

    private final ResourceLoader resourceLoader;


    @Autowired
    public FileController(FileRepository fileRepository, ArticleService articleService, FileTableService fileTableService, ResourceLoader resourceLoader) {
        this.fileRepository = fileRepository;
        this.articleService = articleService;
        this.fileTableService = fileTableService;
        this.resourceLoader = resourceLoader;
    }

    @PostMapping("/api/upload/article")
    @ResponseBody
    public VditorFiles save(@RequestParam(value = "file[]") MultipartFile[] files,
                            HttpServletRequest request) {
        long userId = Long.parseLong(JwtUtil.getUser(request).getId());
        return fileRepository.vditorFileSave(files, userId);
    }

    @PostMapping("/api/upload/video")
    @ResponseBody
    public ResponseDetails saveVideo(@RequestParam(value = "file[]") MultipartFile[] files,
                                HttpServletRequest request) throws FileNotFoundException {
        long userId = Long.parseLong(JwtUtil.getUser(request).getId());
        return ResponseDetails.ok()
                .put("data",
                        fileRepository.videoAndPhotoSave(files, FileTypeEnum.VIDEO.getCode(), userId));
    }

    @PostMapping("/api/upload/photo")
    @ResponseBody
    public ResponseDetails savePhoto(@RequestParam(value = "file[]") MultipartFile[] files,
                                     HttpServletRequest request) throws FileNotFoundException {
        long userId = Long.parseLong(JwtUtil.getUser(request).getId());
        return ResponseDetails.ok()
                .put("data",
                        fileRepository.videoAndPhotoSave(files, FileTypeEnum.PHOTO.getCode(), userId));
    }


    @PostMapping("/api/upload/avatar")
    @ResponseBody
    public ResponseDetails saveAvatar(@RequestParam(value = "file[]") MultipartFile[] files,
                                 HttpServletRequest request) throws FileNotFoundException {
        long userId = Long.parseLong(JwtUtil.getUser(request).getId());
        return ResponseDetails.ok()
                .put("data",
                        fileRepository.videoAndPhotoSave(files, FileTypeEnum.AVATAR.getCode(), userId));
    }


    @PostMapping("/api/upload/top")
    @ResponseBody
    public ResponseDetails saveTop(@RequestParam(value = "file[]") MultipartFile[] files,
                                      HttpServletRequest request) throws FileNotFoundException {
        long userId = Long.parseLong(JwtUtil.getUser(request).getId());
        return ResponseDetails.ok()
                .put("data",
                        fileRepository.videoAndPhotoSave(files, FileTypeEnum.TOP_IMAGE.getCode(),  userId));
    }


    @GetMapping("/api/upload/file/{date}/{filename:.+}")
    public ResponseEntity get(@PathVariable(value = "date") String date,
                                      @PathVariable(value = "filename") String filename,
                                      @RequestParam(value = "key", required = false) String key,
                                      HttpServletRequest request) {
        try {
            Path path = fileRepository.load(date + "/" + filename);
            //Path path = fileRepository.load("2020-09-09/e8d2690873ed45eea2822654b4a80f34.mp4");
            Resource resource = new UrlResource(path.toUri());
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
            // TODO 优化在使用安卓版 Edge 在播放视频时，多次循环请求接口，导致多次查询数据库的的问题
            // TODO 优化判断逻辑
            // TODO 拆分函数
            // 判断是否是视频
            if (FileTypeEnum.getFileType(FileTypeEnum.getFileSuffix(filename)).equals(FileTypeEnum.VIDEO)) {
                FileTableEntity fileTableEntity = fileTableService.findFileByFilename(filename);
                if (fileTableEntity == null) {
                    return null;
                }
                // 非视频区投稿视频直接放行
                if (fileTableEntity.getArticleId() == null) {
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .headers(httpHeaders)
                            .body(resource);
                }
                // 视频文件 key 错误直接返回 null
                if (key == null) {
                    return null;
                }
                String originalText = AesUtil.decrypt(key, WebConstant.AES_KEY);
                if (originalText == null) {
                    return null;
                }
                // 第一个值是用户ID，如果访问时没有登录，则这个值是 -1
                // 第二个值是文件ID，进行文件查找
                // 第三个值是过期时间
                // 第四个是文件名
                String[] msg = originalText.split("#");
                Long userId = Long.parseLong(msg[0]);
                Long fileId = Long.parseLong(msg[1]);
                Long expire = Long.parseLong(msg[2]);
                // 先进行过期时间的判断
                if (System.currentTimeMillis() >expire) {
                    log.warn("用户 {} 访问文件id为 {} 的的文件时，使用的 key {} 已过期!", msg[0], msg[1], key);
                    return null;
                }
                // 解析出的文件ID不同，则返回null
                if (!fileTableEntity.getId().equals(fileId)) {
                    return null;
                }
                if (articleService.hasThisVideoPlayPower(fileTableEntity, userId, request)) {
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .headers(httpHeaders)
                            .body(resource);
                } else {
                    return null;
                }
            }
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(httpHeaders)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
