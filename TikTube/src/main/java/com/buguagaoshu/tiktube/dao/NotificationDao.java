package com.buguagaoshu.tiktube.dao;

import com.buguagaoshu.tiktube.entity.NotificationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通知表
 * 
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Mapper
public interface NotificationDao extends BaseMapper<NotificationEntity> {
	
    /**
     * 将用户的所有未读通知标记为已读
     * 
     * @param userId 用户ID
     * @param status 已读状态
     * @param readTime 阅读时间
     * @return 更新的记录数
     */
    int updateAllNotificationStatus(@Param("userId") Long userId, @Param("status") Integer status, @Param("readTime") Long readTime);
}
