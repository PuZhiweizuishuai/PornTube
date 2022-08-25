package com.buguagaoshu.porntube.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import lombok.Data;

/**
 * 文件表
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
@Data
@TableName("file_table")
public class FileTableEntity {

    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 视频，图片，文章ID， 需要后期更新，没有此项的文件后期需要清楚
     */
    private Long articleId;

    /**
     * 服务器保存的文件地址
     */
    private String fileUrl;

    /**
     * 文件原始名
     */
    private String fileOriginalName;

    /**
     * 新的文件名
     */
    private String fileNewName;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 上传时间
     */
    private Long uploadTime;

    /**
     * 上传人
     */
    private Long uploadUserId;

    /**
     * 文件类型 【0 视频， 1 图片， 2 其它附件 3 头像数据， 4 顶部大图数据】
     */
    private Integer type;

    /**
     * 后缀名
     */
    private String suffixName;

    /**
     * 视频长度
     * */
    private Double duration;

    private Integer height;

    private Integer width;

    /**
     * 像素数
     * */
    private Long pixelsNumber;


    /**
     * 帧率
     * */
    private Double frameRate;


    /**
     * 详细信息 JSON
     * */
    private String info;


    /**
     * 文件存储状态
     *
     * 0  未保存的临时文件，后期删除
     * 1  保存成功并发布的文件
     * */
    private Integer status;


    /**
     * 加载视频所需要的Key
     * */
    @TableField(exist = false)
    private String key;

    private String ip;

    private String ua;

    private String city;

}
