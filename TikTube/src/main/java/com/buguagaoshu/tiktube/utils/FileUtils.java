package com.buguagaoshu.tiktube.utils;

import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.enums.FileStatusEnum;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-06-07 16:31
 */
public class FileUtils {
    public static FileTableEntity createFileTableEntity(String filename, String suffix,
                                                 String path, long size,
                                                 String originalFilename,
                                                 long userId, int type) {
        FileTableEntity fileTableEntity = new FileTableEntity();
        fileTableEntity.setUploadTime(System.currentTimeMillis());
        fileTableEntity.setFileUrl("/api/upload/" + path + "/" + filename);
        fileTableEntity.setFileNewName(filename);
        fileTableEntity.setSize(size);
        fileTableEntity.setFileOriginalName(originalFilename);

        fileTableEntity.setType(type);
        fileTableEntity.setSuffixName(suffix);
        fileTableEntity.setUploadUserId(userId);
        fileTableEntity.setStatus(FileStatusEnum.NOT_USE_FILE.getCode());
        return fileTableEntity;
    }
}
