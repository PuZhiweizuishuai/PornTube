package com.buguagaoshu.tiktube.repository;


import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.vo.VditorFiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-07-02 17:49
 */
public interface FileRepository {
    /**
     * 使用 Vditor 编辑器上传时返回的数据
     * */
    VditorFiles vditorFileSave(MultipartFile[] files, Long userId);

    /**
     * 图片或视频上传接口
     * */
    List<FileTableEntity> videoAndPhotoSave(MultipartFile[] files, Integer type, Long userId) throws FileNotFoundException;


    /**
     * 加载文件
     * */
    Path load(String filePath) throws FileNotFoundException;

    /**
     * 删除本地文件和数据库文件
     *
     */
    boolean deleteFileWithDatabase(FileTableEntity fileTableEntity);

    /**
     * 删除本地文件
     * */
    boolean deleteFile(FileTableEntity fileTableEntity);
}
