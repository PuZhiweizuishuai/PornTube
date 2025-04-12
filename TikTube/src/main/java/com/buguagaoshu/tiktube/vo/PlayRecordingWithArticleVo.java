package com.buguagaoshu.tiktube.vo;

import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.entity.PlayRecordingEntity;
import lombok.Data;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-09 18:06
 */
@Data
public class PlayRecordingWithArticleVo {
    private PlayRecordingEntity playRecordingEntity;


    private ArticleEntity articleEntity;
}
