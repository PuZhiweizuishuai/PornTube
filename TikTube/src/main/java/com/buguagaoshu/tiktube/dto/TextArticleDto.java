package com.buguagaoshu.tiktube.dto;

import com.buguagaoshu.tiktube.entity.ArticleTextEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-26
 */
@Data
public class TextArticleDto implements ArticleDraft {
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 101, message = "标签长度不能超过 100 个字符。")
    private String title;


    private String imgUrl;


    private Long imageId;



    @Size(max = 1001, message = "简介长度不能超过 1000 个字符。")
    private String describe;


    @NotNull(message = "分区不能为空")
    private Integer category;


    @NotNull(message = "至少要有一个标签")
    private List<String> tag;

    @Size(max = 3)
    @NotNull(message = "正文不能为空")
    private List<ArticleTextEntity> textList;
}
