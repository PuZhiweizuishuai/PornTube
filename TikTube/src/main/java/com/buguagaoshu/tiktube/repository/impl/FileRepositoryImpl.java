package com.buguagaoshu.tiktube.repository.impl;

import com.buguagaoshu.tiktube.config.WebConfig;
import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.enums.FileStatusEnum;
import com.buguagaoshu.tiktube.enums.FileTypeEnum;
import com.buguagaoshu.tiktube.repository.FileRepository;
import com.buguagaoshu.tiktube.service.FileTableService;
import com.buguagaoshu.tiktube.utils.FFmpegUtils;
import com.buguagaoshu.tiktube.utils.FileUtils;
import com.buguagaoshu.tiktube.vo.VditorFiles;
import com.buguagaoshu.tiktube.vo.VideoInfo;
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
public class FileRepositoryImpl implements FileRepository {
    // 定义最大文件大小为20MB（字节单位）
    private static final long MAX_SIZE = 20 * 1024 * 1024; // 20MB

    private final Path location;
    private final FileTableService fileTableService;

    private final FileRepositoryInOSS fileRepositoryInOSS;

    @Autowired
    public FileRepositoryImpl(FileTableService fileTableService,
                              FileRepositoryInOSS fileRepositoryInOSS) throws IOException {
        this.fileRepositoryInOSS = fileRepositoryInOSS;
        this.location = Paths.get(FileTypeEnum.ROOT);
        this.fileTableService = fileTableService;
        if (Files.notExists(this.location)) {
            Files.createDirectories(this.location);
        }
    }

    @Override
    public VditorFiles vditorFileSave(MultipartFile[] files, Long userId) {
        VditorFiles vditorFiles = new VditorFiles();
        Map<String, Object> succMap = new HashMap<>(files.length);
        List<String> errFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.getSize() > MAX_SIZE) {
                errFiles.add(file.getOriginalFilename());
                Map<String, Object> data = new HashMap<>(2);
                vditorFiles.setCode(0);
                vditorFiles.setMsg("上传失败，文件大小超过 20M");
                data.put("succMap", succMap);
                data.put("errFiles", errFiles);
                vditorFiles.setData(data);
                return vditorFiles;
            }
        }

        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                errFiles.add(file.getOriginalFilename());
                continue;
            }
            
            // 根据存储位置决定使用的路径
            String path = WebConfig.FILE_SAVE_LOCATION == 0 ? 
                FileTypeEnum.filePath() : FileTypeEnum.ossFilePath();
            String suffix = FileTypeEnum.getFileSuffix(file.getOriginalFilename());
            String filename = FileTypeEnum.newFilename(suffix);
            String fileUrl = null;
            
            try {
                // 根据存储位置配置决定存储方式
                if (WebConfig.FILE_SAVE_LOCATION == 0) {
                    // 本地存储
                    File dest = new File(path);
                    if (!dest.exists() && !dest.mkdirs()) {
                        errFiles.add(file.getOriginalFilename());
                        continue;
                    }
                    
                    Files.copy(file.getInputStream(), Paths.get(path, filename));
                    fileUrl = "/api/upload/" + path + "/" + filename;
                } else {
                    // 对象存储
                    fileUrl = fileRepositoryInOSS.saveFile(
                            file,
                            path + "/" + filename,
                            file.getContentType(),
                            WebConfig.FILE_SAVE_LOCATION
                    );
                    if (fileUrl == null) {
                        log.error("文件上传到对象存储失败: {}", file.getOriginalFilename());
                        errFiles.add(file.getOriginalFilename());
                        continue;
                    }
                }
                
                FileTableEntity fileTableEntity = FileUtils.createFileTableEntity(
                        filename, 
                        suffix, 
                        path, 
                        file.getSize(), 
                        file.getOriginalFilename(), 
                        userId, 
                        FileTypeEnum.ARTICLE.getCode(),
                        WebConfig.FILE_SAVE_LOCATION
                );
                succMap.put(file.getOriginalFilename(), fileTableEntity.getFileUrl());
                fileTableService.save(fileTableEntity);
            } catch (Exception e) {
                log.error("文件保存失败: {}", e.getMessage());
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
        // 头像和首页大图文件大小检查
        if ((FileTypeEnum.AVATAR.getCode() == type || FileTypeEnum.TOP_IMAGE.getCode() == type) 
                && files[0].getSize() > 2048000) {
            return null;
        }
        
        List<FileTableEntity> list = new ArrayList<>(files.length);
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            
            // 根据存储位置决定使用的路径
            String path = WebConfig.FILE_SAVE_LOCATION == 0 ? 
                FileTypeEnum.filePath() : FileTypeEnum.ossFilePath();
            
            String suffix = FileTypeEnum.getFileSuffix(file.getOriginalFilename());
            String filename = FileTypeEnum.newFilename(suffix);
            
            // 文件类型检查
            validateFileType(type, suffix);
            
            String fileUrl = null;
            
            try {
                // 视频文件特殊处理
                if (FileTypeEnum.VIDEO.getCode() == type) {
                    // 创建文件实体
                    FileTableEntity fileTableEntity = FileUtils.createFileTableEntity(
                            filename, 
                            suffix, 
                            path, 
                            file.getSize(), 
                            file.getOriginalFilename(), 
                            userId, 
                            type,
                            WebConfig.FILE_SAVE_LOCATION
                    );
                    
                    // 直接从 MultipartFile 处理视频文件，提取信息
                    boolean info = processVideoFile(file, fileTableEntity);
                    if(!info) {
                        throw new Exception("文件格式错误，系统无法读取，请检查后重新上传！");
                    }
                    // 根据存储位置配置决定存储方式
                    if (WebConfig.FILE_SAVE_LOCATION == 0) {
                        // 本地存储
                        File dest = new File(path);
                        if (!dest.exists() && !dest.mkdirs()) {
                            log.warn("创建目录失败: {}", path);
                            continue;
                        }
                        
                        Files.copy(file.getInputStream(), Paths.get(path, filename));
                        fileUrl = "/api/upload/" + path + "/" + filename;
                    } else {
                        // 对象存储 - 直接上传 MultipartFile
                        fileUrl = fileRepositoryInOSS.saveFile(
                                file,
                                path + "/" + filename,
                                file.getContentType(),
                                WebConfig.FILE_SAVE_LOCATION);
                        if (fileUrl == null) {
                            log.error("视频文件上传到对象存储失败: {}", file.getOriginalFilename());
                            continue;
                        }
                    }
                    
                    //fileTableEntity.setFileUrl(fileUrl);
                    fileTableEntity.setStatus(FileStatusEnum.NOT_USE_FILE.getCode());
                    fileTableService.save(fileTableEntity);
                    list.add(fileTableEntity);
                } else {
                    // 非视频文件的处理逻辑保持不变
                    if (WebConfig.FILE_SAVE_LOCATION == 0) {
                        // 本地存储
                        File dest = new File(path);
                        if (!dest.exists() && !dest.mkdirs()) {
                            log.warn("创建目录失败: {}", path);
                            continue;
                        }
                        
                        Files.copy(file.getInputStream(), Paths.get(path, filename));
                        fileUrl = "/api/upload/" + path + "/" + filename;
                    } else {
                        // 对象存储
                        fileUrl = fileRepositoryInOSS.saveFile(
                                file,
                                path + "/" + filename,
                                file.getContentType(),
                                WebConfig.FILE_SAVE_LOCATION
                        );
                        if (fileUrl == null) {
                            log.error("文件上传到对象存储失败: {}", file.getOriginalFilename());
                            continue;
                        }
                    }
                    
                    FileTableEntity fileTableEntity = FileUtils.createFileTableEntity(
                            filename, 
                            suffix, 
                            path, 
                            file.getSize(), 
                            file.getOriginalFilename(), 
                            userId, 
                            type,
                            WebConfig.FILE_SAVE_LOCATION
                    );
                    //fileTableEntity.setFileUrl(fileUrl);
                    fileTableEntity.setStatus(FileStatusEnum.NOT_USE_FILE.getCode());
                    fileTableService.save(fileTableEntity);
                    list.add(fileTableEntity);
                }
            } catch (Exception e) {
                log.error("文件上传失败，用户ID：{}，文件名：{}，错误：{}", userId, file.getOriginalFilename(), e.getMessage());
            }
        }
        
        return list;
    }

    /**
     * 验证文件类型是否符合要求
     * 
     * @param type 文件类型代码
     * @param suffix 文件后缀
     * @throws FileNotFoundException 如果文件类型不符合要求
     */
    private void validateFileType(Integer type, String suffix) throws FileNotFoundException {
        if (FileTypeEnum.checkPhotoType(type)) {
            if (!FileTypeEnum.getFileType(suffix).equals(FileTypeEnum.PHOTO)) {
                throw new FileNotFoundException("上传文件格式不正确，需要图片格式");
            }
        } else {
            if (!FileTypeEnum.getFileType(suffix).equals(FileTypeEnum.VIDEO)) {
                throw new FileNotFoundException("上传文件格式不正确，需要视频格式");
            }
        }
    }
    
    /**
     * 处理视频文件，提取视频信息
     * 
     * @param videoFile 视频文件
     * @param fileTableEntity 文件实体
     * @return 处理是否成功
     */
    private boolean processVideoFile(MultipartFile videoFile, FileTableEntity fileTableEntity) {
        VideoInfo videoInfo = FFmpegUtils.getVideoInfoFromMultipartFile(videoFile);
        if (videoInfo == null) {
            log.warn("视频文件无法解析: {}", fileTableEntity.getFileNewName());
            return false;
        }
        
        fileTableEntity.setDuration(videoInfo.getDuration());
        fileTableEntity.setHeight(videoInfo.getHeight());
        fileTableEntity.setWidth(videoInfo.getWidth());
        fileTableEntity.setPixelsNumber((long) videoInfo.getWidth() * videoInfo.getHeight());
        fileTableEntity.setFrameRate(videoInfo.getFrameRate());
        fileTableEntity.setInfo(videoInfo.toJson());
        return true;
    }

    @Override
    public Path load(String filePath) throws FileNotFoundException {
        return FileUtils.resolveSafely(location, filePath);
    }

    @Override
    public boolean deleteFileWithDatabase(FileTableEntity fileTableEntity) {
        if (fileTableEntity == null || fileTableEntity.getId() == null) {
            return false;
        }
        
        FileTableEntity file = fileTableService.getById(fileTableEntity.getId());
        if (file == null || file.getArticleId() != null) {
            return false;
        }

        boolean fileDeleted = deleteFile(file);
        if (fileDeleted) {
            fileTableService.removeById(file.getId());
            return true;
        } else {
            // TODO 优化返回消息提醒
            if (file.getStatus().equals(FileStatusEnum.DELETE.getCode())) {
                log.info("文件无法在文件系统中删除，只会在数据库中删除！: {}", fileTableEntity.getFileUrl());
                fileTableService.removeById(file.getId());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteFile(FileTableEntity fileTableEntity) {
        if (fileTableEntity == null || fileTableEntity.getFileUrl() == null) {
            return false;
        }
        
        try {
            // 根据存储位置决定删除方式
            if (fileTableEntity.getSaveLocation() == 0) {
                // 本地存储
                String pathStr = fileTableEntity.getFileUrl().replace("/api/upload/" + FileTypeEnum.ROOT + "/", "");
                Path path = load(pathStr);
                Files.delete(path);
            } else {
                // 对象存储
                // /api/upload/1/oss/2025-04-30/66e7779e7ff242959414161c2d8af639.JPG
                String objName = fileTableEntity.getFileUrl().replace("/api/upload/" + fileTableEntity.getSaveLocation() + "/oss/", "");
                fileRepositoryInOSS.deleteFile(objName, fileTableEntity.getSaveLocation());
            }
            return true;
        } catch (Exception e) {
            log.warn("删除文件失败: {}, 原因: {}", fileTableEntity.getFileUrl(), e.getMessage());
            return false;
        }
    }
}
