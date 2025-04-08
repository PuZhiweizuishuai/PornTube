package com.buguagaoshu.porntube.vo;

import com.buguagaoshu.porntube.entity.ArticleEntity;
import com.buguagaoshu.porntube.entity.FavoritesTableEntity;
import lombok.Data;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-08
 */
@Data
public class FavoritesAndArticle {
    private FavoritesTableEntity favorite;

    private ArticleEntity articleEntity;
}
