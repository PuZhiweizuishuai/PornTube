package com.buguagaoshu.tiktube.repository.impl;

import com.buguagaoshu.tiktube.enums.FileTypeEnum;
import com.buguagaoshu.tiktube.repository.FileStorageStrategy;
import com.buguagaoshu.tiktube.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地磁盘存储策略（WebConfig.FILE_SAVE_LOCATION == 0）
 */
@Slf4j
@Component
public class LocalFileStorageStrategy implements FileStorageStrategy {

    private final Path root = Paths.get(FileTypeEnum.ROOT);

    @Override
    public boolean supports(int saveLocation) {
        return saveLocation == 0;
    }

    @Override
    public String store(MultipartFile file, String path, String filename) {
        File dest = new File(path);
        if (!dest.exists() && !dest.mkdirs()) {
            log.warn("创建目录失败: {}", path);
            return null;
        }
        try {
            Files.copy(file.getInputStream(), Paths.get(path, filename));
            return "/api/upload/" + path + "/" + filename;
        } catch (Exception e) {
            log.error("文件写入本地失败: {}/{}，原因: {}", path, filename, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean delete(String fileUrl, int saveLocation) {
        try {
            String pathStr = fileUrl.replace("/api/upload/" + FileTypeEnum.ROOT + "/", "");
            Path path = FileUtils.resolveSafely(root, pathStr);
            Files.delete(path);
            return true;
        } catch (Exception e) {
            log.warn("删除本地文件失败: {}, 原因: {}", fileUrl, e.getMessage());
            return false;
        }
    }
}
