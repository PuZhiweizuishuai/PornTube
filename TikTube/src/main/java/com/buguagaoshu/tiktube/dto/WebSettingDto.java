package com.buguagaoshu.tiktube.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-08 20:25
 */
@Data
public class WebSettingDto {
    /**
     * 网站名
     */
    private String name;

    /**
     * 是否开启非vip每日观看次数限制 [0 关闭， 1 开启]
     */
    private Boolean openNoVipLimit;

    /**
     * 非vip 每日观看次数
     */
    private Integer noVipViewCount;

    /**
     * 网页logo地址
     */
    private String logoUrl;

    /**
     * 是否开启邀请码注册 【0 关闭， 1开启】
     */
    private Boolean openInvitationRegister;

    /**
     * 网页简短的描述
     */
    private String describe;

    /**
     * 是否开启每日上传视频增加非会员观看次数 【0 关闭， 1开启】
     */
    private Boolean openUploadVideoAddViewCount;

    /**
     * 是否开启视频，文章，图片盛和 【0 关闭， 1 开启】
     */
    private Boolean openExamine;

    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    private Long createTime;


    private Long homeMaxVideoCount;
}
