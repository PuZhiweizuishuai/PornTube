package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.tiktube.entity.FollowEntity;
import com.buguagaoshu.tiktube.utils.PageUtils;

import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-19
 */
public interface FollowService extends IService<FollowEntity> {

    /**
     * 关注
     * */
    boolean addFollow(FollowEntity follow, Long userId);

    /**
     * 取关
     * */
    boolean deleteFollow(FollowEntity follow, Long userId);


    /**
     * 移除粉丝
     * */
    boolean removeFans(FollowEntity follow, Long userId);

    /**
     * 检查是否关注
     * */
    boolean checkFollow(Long followUser, Long userId);

    /**
     * 查询关注列表
     * */
    PageUtils queryFollow(Map<String, Object> params, Long userId);


    /**
     * 查询粉丝列表
     * */
    PageUtils queryFansList(Map<String, Object> params, Long userId);


    /**
     * 查找关注用户发布的稿件
     * */
    PageUtils queryFollowArticle(Map<String, Object> params, Long userId);
}
