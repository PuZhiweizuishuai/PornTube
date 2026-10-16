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


    /**
     * 上限与前端一致（6 个）；单个标签限 30 字符，
     * 因为序列化后的 JSON 要写进 article.tag varchar(255)，超长会直接插入失败
     */
    @NotNull(message = "至少要有一个标签")
    @Size(min = 1, max = 6, message = "标签数量必须在 1 到 6 个之间")
    private List<@NotBlank(message = "标签不能为空") @Size(max = 30, message = "单个标签不能超过 30 个字符") String> tag;


    @NotNull(message = "视频文件必须上传")
    private FileTableEntity video;
}
