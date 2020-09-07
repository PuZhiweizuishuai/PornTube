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
}

