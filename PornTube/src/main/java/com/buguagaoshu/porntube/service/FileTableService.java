package com.buguagaoshu.porntube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.entity.FileTableEntity;

import java.util.List;
import java.util.Map;

/**
 * 文件表
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface FileTableService extends IService<FileTableEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取当前视频下的视频文件
     * @param id 视频ID
     * @return 视频包含的文件列表
     * */
    List<FileTableEntity> findArticleVideo(long id);


    FileTableEntity findFileByFilename(String fileName);


    /**
     * 更新文件状态
     * @param userId 文件所属用户
     * @param fileId 文件ID
     * @param fileType 文件类型
     * @param fileUrl 文件路径
     * @return 更新结果
     * */
    boolean updateFileStatus(long userId, long fileId, int fileType, String fileUrl);


    /**
     * @param endTime 结束时间
     * @param count 返回数量
     * 已弃用文件列表
     * */
    List<FileTableEntity> deprecatedFileList(long endTime, int count);

}

