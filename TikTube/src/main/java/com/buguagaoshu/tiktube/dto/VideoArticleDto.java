package com.buguagaoshu.tiktube.dto;

import com.buguagaoshu.tiktube.entity.FileTableEntity;
import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 20:52
 */
@Data
public class VideoArticleDto implements ArticleDraft {
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 101, message = "标签长度不能超过 100 个字符。")
    private String title;

    @NotBlank(message = "封面图不能为空")
    private String imgUrl;


    @NotNull(message = "封面ID不能为空")
    private Long imageId;



    @Size(max = 1001, message = "简介长度不能超过 1000 个字符。")
    private String describe;


    @NotNull(message = "分区不能为空")
    private Integer category;


    @NotNull(message = "至少要有一个标签")
    private List<String> tag;


    @NotNull(message = "视频文件必须上传")
    private FileTableEntity video;
}
