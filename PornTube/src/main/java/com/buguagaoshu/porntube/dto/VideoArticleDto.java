package com.buguagaoshu.porntube.dto;

import com.buguagaoshu.porntube.entity.FileTableEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 20:52
 */
@Data
public class VideoArticleDto {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "封面图不能为空")
    private String imgUrl;

    @NotNull(message = "封面ID不能为空")
    private Long imageId;


    private String describe;


    @NotNull(message = "分区不能为空")
    private Integer category;


    @NotNull(message = "至少要有一个标签")
    private List<String> tag;


    @NotNull(message = "视频文件必须上传")
    private FileTableEntity video;
}
