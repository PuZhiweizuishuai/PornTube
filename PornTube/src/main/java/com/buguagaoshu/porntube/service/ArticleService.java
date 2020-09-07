package com.buguagaoshu.porntube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.porntube.dto.VideoArticleDto;
import com.buguagaoshu.porntube.enums.ReturnCodeEnum;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.entity.ArticleEntity;
import com.buguagaoshu.porntube.vo.ArticleViewData;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 视频，图片，文章 发帖表

TODO 回复消息可见，加密帖子，视频等
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface ArticleService extends IService<ArticleEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 发布视频
     * @return 发布结果
     * */
    ReturnCodeEnum saveVideo(VideoArticleDto videoArticleDto, HttpServletRequest request);


    ArticleViewData getVideo(long id);

    /**
     * 增加弹幕数
     * */
    void addDanmakuCount(Long id, long count);
}

