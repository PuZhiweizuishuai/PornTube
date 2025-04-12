package com.buguagaoshu.tiktube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import lombok.Data;

/**
 *
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 15:03:54
 */
@Data
@TableName("user_role")
public class UserRoleEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *
     */
    private Long userid;

    /**
     * 角色
     */
    private String role;

    /**
     *
     */
    private Long createTime;

    /**
     *
     */
    private Long updateTime;

    /**
     *
     */
    private Long vipStartTime;

    /**
     *
     */
    private Long vipStopTime;


    /**
     * 修改人
     * 初始情况为 0，代表系统
     * */
    private Long modified;

}
