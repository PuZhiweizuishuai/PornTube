package com.buguagaoshu.tiktube.utils;

import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.enums.FileStatusEnum;
import com.buguagaoshu.tiktube.enums.FileTypeEnum;
import org.springframework.context.annotation.FilterType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-06-07 16:31
 */
public class FileUtils {

    /**
     * 单个路径片段（文件名、日期目录名）是否安全：不含路径分隔符、上级目录与控制字符
     */
    public static boolean isSafeSegment(String segment) {
        if (segment == null || segment.isEmpty() || segment.length() > 255) {
            return false;
        }
        if (segment.contains("..")) {
            return false;
        }
        for (int i = 0; i < segment.length(); i++) {
            char c = segment.charAt(i);
            if (c == '/' || c == '\\' || c < 0x20 || c == 0x7f) {
                return false;
            }
        }
        return true;
    }

    /**
     * 以 / 分隔的相对路径是否安全，每一段都必须是安全片段
     */
    public static boolean isSafeRelativePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty() || relativePath.length() > 512) {
            return false;
        }
        if (relativePath.indexOf('\\') >= 0 || relativePath.startsWith("/") || relativePath.endsWith("/")
                || relativePath.contains("//")) {
            return false;
        }
        for (String segment : relativePath.split("/")) {
            if (!isSafeSegment(segment)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 在根目录内解析相对路径，越界（包括软链接指向根目录之外）时拒绝。
     * 使用真实路径比较，因此文件不存在时同样抛 FileNotFoundException。
     */
    public static Path resolveSafely(Path root, String relativePath) throws FileNotFoundException {
        if (!isSafeRelativePath(relativePath)) {
            throw new FileNotFoundException("非法的文件路径！");
        }
        Path rootDir;
        try {
            rootDir = root.toAbsolutePath().normalize().toRealPath();
        } catch (IOException e) {
            throw new FileNotFoundException("文件存储根目录不存在！");
        }
        Path target;
        try {
            target = rootDir.resolve(relativePath).toRealPath();
        } catch (IOException e) {
            throw new FileNotFoundException("无法加载文件！文件 " + relativePath + " 不存在！");
        }
        if (!target.startsWith(rootDir) || !Files.isRegularFile(target)) {
            throw new FileNotFoundException("非法的文件路径！");
        }
        return target;
    }

    public static FileTableEntity createFileTableEntity(String filename, String suffix,
                                                 String path, long size,
                                                 String originalFilename,
                                                 long userId, int type, int saveLocation) {
        FileTableEntity fileTableEntity = new FileTableEntity();
        fileTableEntity.setUploadTime(System.currentTimeMillis());
        if (saveLocation == 0) {
            fileTableEntity.setFileUrl("/api/upload/" + path + "/" + filename);
        } else {
            fileTableEntity.setFileUrl(FileTypeEnum.ossFileURL(saveLocation, path, filename));
        }

        fileTableEntity.setFileNewName(filename);
        fileTableEntity.setSize(size);
        fileTableEntity.setFileOriginalName(originalFilename);

        fileTableEntity.setType(type);
        fileTableEntity.setSuffixName(suffix);
        fileTableEntity.setUploadUserId(userId);
        fileTableEntity.setStatus(FileStatusEnum.NOT_USE_FILE.getCode());
        fileTableEntity.setSaveLocation(saveLocation);
        return fileTableEntity;
    }
}
