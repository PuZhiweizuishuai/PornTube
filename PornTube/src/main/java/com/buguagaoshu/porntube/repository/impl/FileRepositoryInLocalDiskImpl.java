package com.buguagaoshu.porntube.repository.impl;

import com.buguagaoshu.porntube.entity.FileTableEntity;
import com.buguagaoshu.porntube.enums.FileStatusEnum;
import com.buguagaoshu.porntube.enums.FileTypeEnum;
import com.buguagaoshu.porntube.repository.FileRepository;
import com.buguagaoshu.porntube.service.FileTableService;
import com.buguagaoshu.porntube.utils.FFmpegUtils;
import com.buguagaoshu.porntube.utils.FileUtils;
import com.buguagaoshu.porntube.vo.VditorFiles;
import com.buguagaoshu.porntube.vo.VideoInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 14:24
 */
@Repository
@Slf4j
public class FileRepositoryInLocalDiskImpl implements FileRepository {

    private final Path location;

    private final FileTableService fileTableService;

    @Autowired
    public FileRepositoryInLocalDiskImpl(FileTableService fileTableService) throws IOException {
        this.location = Paths.get(FileTypeEnum.ROOT);
        this.fileTableService = fileTableService;
        if (Files.notExists(this.location)) {
            Files.createDirectories(this.location);
        }
    }


    @Override
    public VditorFiles vditorFileSave(MultipartFile[] files, Long userId) {
        VditorFiles vditorFiles = new VditorFiles();
        Map<String, Object> succMap = new HashMap<>(2);
        List<String> errFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            String path = FileTypeEnum.filePath();
            String suffix = FileTypeEnum.getFileSuffix(file.getOriginalFilename());
            String filename = FileTypeEnum.newFilename(suffix);
            File dest = new File(path);
            if (!dest.exists() && !dest.mkdirs()) {
                errFiles.add(file.getOriginalFilename());
                continue;
            }
            try {

                Files.copy(file.getInputStream(), Paths.get(path, filename));
                FileTableEntity fileTableEntity = FileUtils.createFileTableEntity(filename, suffix, path, file.getSize(), file.getOriginalFilename(), userId, FileTypeEnum.getFileType(suffix).getCode());
                succMap.put(file.getOriginalFilename(), "/api/upload/" + path + "/" + filename);
                // TODO 文件夹大小控制
                // TODO 写入视频长度信息
                fileTableService.save(fileTableEntity);
            } catch (Exception e) {
                errFiles.add(file.getOriginalFilename());
            }
        }

        Map<String, Object> data = new HashMap<>(2);
        vditorFiles.setCode(0);
        vditorFiles.setMsg("上传成功");
        data.put("succMap", succMap);
        data.put("errFiles", errFiles);
        vditorFiles.setData(data);
        return vditorFiles;
    }

    @Override
    public List<FileTableEntity> videoAndPhotoSave(MultipartFile[] files, Integer type, Long userId) throws FileNotFoundException {
        // TODO 格式检查
        List<FileTableEntity> list = new ArrayList<>();
        for (MultipartFile file : files) {
            String path = FileTypeEnum.filePath();
            String suffix = FileTypeEnum.getFileSuffix(file.getOriginalFilename());
            String filename = FileTypeEnum.newFilename(suffix);
            // 图片类型
            if (FileTypeEnum.checkPhotoType(type)) {
                //
                if (!FileTypeEnum.getFileType(suffix).equals(FileTypeEnum.PHOTO)) {
                    throw new FileNotFoundException("上传文件格式不正确");
                }
            } else {
                // 视频
                if (!FileTypeEnum.getFileType(suffix).equals(FileTypeEnum.VIDEO)) {
                    throw new FileNotFoundException("上传文件格式不正确");
                }
            }
            File dest = new File(path);

            if (!dest.exists() && !dest.mkdirs()) {
                continue;
            }
            try {
                Files.copy(file.getInputStream(), Paths.get(path, filename));
            } catch (Exception e) {
                log.info("文件上传失败，上传文件的用户ID：{}， 上传的文件名： {}", userId, file.getOriginalFilename());
            }
            FileTableEntity fileTableEntity = FileUtils.createFileTableEntity(filename, suffix, path, file.getSize(), file.getOriginalFilename(), userId, type);
            // TODO 文件夹大小控制

            // 分析视频数据并保存
            if (FileTypeEnum.VIDEO.getCode() == type) {
                VideoInfo videoInfo = FFmpegUtils.getVideoInfo(Paths.get(path, filename).toFile());
                if (videoInfo == null) {
                    // 删除已上传文件
                    try {
                        Files.delete(Paths.get(path, filename));
                    } catch (Exception ignored) {
                    }
                    throw new FileNotFoundException("上传的视频文件数据错误无法解析，请检查后重新上传！");
                }
                fileTableEntity.setDuration(videoInfo.getDuration());
                fileTableEntity.setHeight(videoInfo.getHeight());
                fileTableEntity.setWidth(videoInfo.getWidth());
                fileTableEntity.setPixelsNumber((long) videoInfo.getWidth() * videoInfo.getHeight());
                fileTableEntity.setFrameRate(videoInfo.getFrameRate());
                fileTableEntity.setInfo(videoInfo.toJson());
                // 自动截图
                List<FileTableEntity> fileTableEntities = FFmpegUtils.randomGrabberFFmpegImage(Paths.get(path, filename).toFile(), 6, userId);
                if (fileTableEntities != null) {
                    // 保存截图
                    fileTableService.saveBatch(fileTableEntities);
                    list.addAll(fileTableEntities);
                }
            }

            fileTableEntity.setStatus(FileStatusEnum.NOT_USE_FILE.getCode());
            fileTableService.save(fileTableEntity);
            list.add(fileTableEntity);
        }
        return list;
    }


    @Override
    public Path load(String filePath) throws FileNotFoundException {
        Path path = location.resolve(filePath);
        if (Files.notExists(path)) {
            throw new FileNotFoundException("Cannot load file! File " + filePath + " not exists!");
        }
        return path;
    }

    @Override
    public boolean deleteFile(FileTableEntity fileTableEntity) {
        String pathStr = fileTableEntity.getFileUrl().replace("/api/upload/" + FileTypeEnum.ROOT + "/", "");
        try {
            Path path = load(pathStr);
            // 删除本地文件
            Files.delete(path);
            return true;
        } catch (Exception e) {
            log.warn(e.getMessage());
            return false;
        }
    }
}
