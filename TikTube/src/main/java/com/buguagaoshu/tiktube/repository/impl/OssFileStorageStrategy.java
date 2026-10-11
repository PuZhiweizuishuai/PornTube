package com.buguagaoshu.tiktube.repository.impl;

import com.buguagaoshu.tiktube.config.WebConfig;
import com.buguagaoshu.tiktube.repository.FileStorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 对象存储策略（MinIO / S3 / R2，WebConfig.FILE_SAVE_LOCATION != 0）
 */
@Slf4j
@Component
public class OssFileStorageStrategy implements FileStorageStrategy {

    private final FileRepositoryInOSS fileRepositoryInOSS;

    @Autowired
    public OssFileStorageStrategy(FileRepositoryInOSS fileRepositoryInOSS) {
        this.fileRepositoryInOSS = fileRepositoryInOSS;
    }

    @Override
    public boolean supports(int saveLocation) {
        return saveLocation != 0;
    }

    @Override
    public String store(MultipartFile file, String path, String filename) {
        String objectName = path + "/" + filename;
        String url = fileRepositoryInOSS.saveFile(file, objectName, file.getContentType(), saveLocation());
        if (url == null) {
            log.error("文件上传到对象存储失败: {}", file.getOriginalFilename());
        }
        return url;
    }

    @Override
    public boolean delete(String fileUrl, int saveLocation) {
        // 形如 /api/upload/1/oss/2025-04-30/xxx.JPG
        String objName = fileUrl.replace("/api/upload/" + saveLocation + "/oss/", "");
        return fileRepositoryInOSS.deleteFile(objName, saveLocation);
    }

    private int saveLocation() {
        return WebConfig.FILE_SAVE_LOCATION;
    }
}
