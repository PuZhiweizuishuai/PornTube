package com.buguagaoshu.porntube.vo;

import com.buguagaoshu.porntube.entity.UserRoleEntity;
import lombok.Data;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-05 15:09
 */
@Data
public class User {
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 提交视频，图片，文章数
     */
    private Long submitCount;

    /**
     * 关注数
     */
    private Long followCount;

    /**
     * 粉丝数
     */
    private Long fansCount;


    /**
     * 简介
     * */
    private String introduction;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 首页大图url
     */
    private String topImgUrl;


    private long expireTime;


    private UserRoleEntity userRoleEntity;
}
