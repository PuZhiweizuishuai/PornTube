package com.buguagaoshu.porntube.file;

import com.buguagaoshu.porntube.entity.FileTableEntity;
import com.buguagaoshu.porntube.enums.FileTypeEnum;
import com.buguagaoshu.porntube.repository.FileRepository;
import com.buguagaoshu.porntube.service.ArticleService;
import com.buguagaoshu.porntube.service.FileTableService;
import com.buguagaoshu.porntube.utils.JwtUtil;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import com.buguagaoshu.porntube.vo.VditorFiles;
import lombok.NonNull;
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
                                      @RequestParam(value = "id", required = false) Long id,
                                      HttpServletRequest request) {
        try {
            Path path = fileRepository.load(date + "/" + filename);
            Resource resource = new UrlResource(path.toUri());
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
            // TODO 写入播放次数，限制非会员播放
            // 判断是否是视频
            if (FileTypeEnum.getFileType(FileTypeEnum.getFileSuffix(filename)).equals(FileTypeEnum.VIDEO)) {

                FileTableEntity tableEntity = fileTableService.findFileByFilename(filename);
                if (tableEntity == null) {
                    return null;
                }
                if (tableEntity.getArticleId() == null) {
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .headers(httpHeaders)
                            .body(resource);
                }
                if (id == null) {
                    return null;
                }
                if (articleService.hasThisVideoPlayPower(tableEntity, request) && id.equals(tableEntity.getId())) {
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .headers(httpHeaders)
                            .body(resource);
                }
                return null;
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
