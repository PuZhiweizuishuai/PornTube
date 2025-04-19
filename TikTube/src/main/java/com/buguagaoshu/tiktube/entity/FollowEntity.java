package com.buguagaoshu.tiktube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-19
 */
@Data
@TableName("follow")
public class FollowEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long followUser;

    private Long createUser;

    private Long createTime;
}
