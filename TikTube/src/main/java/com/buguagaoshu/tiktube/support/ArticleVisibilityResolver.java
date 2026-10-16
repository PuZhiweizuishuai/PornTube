package com.buguagaoshu.tiktube.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.enums.ArticleStatusEnum;
import com.buguagaoshu.tiktube.enums.ExamineTypeEnum;
import com.buguagaoshu.tiktube.vo.ArticleViewData;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 稿件可见性判定
 *
 * 背景：ArticleServiceImpl#getVideo(526-563) 与 ArticleTextService#getText(73-112)
 * 把「查正常状态的稿件 → 判断管理员 → 未过审时仅作者可见 → 否则返回隐藏视图」
 * 这一整套逻辑几乎原样写了两遍，稍有改动就容易只改一处。
 *
 * 规则（与原有实现保持一致）：
 *  1. 稿件不存在 → 不可见
 *  2. 管理员 → 可见
 *  3. 审核通过 → 可见
 *  4. 其它情况 → 仅作者本人可见
 */
@Component
public class ArticleVisibilityResolver {

    /** 构建「指定 id + 类型 + 正常状态」的查询条件 */
    public QueryWrapper<ArticleEntity> normalArticleWrapper(long id, Integer type) {
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("type", type);
        return wrapper;
    }

    /**
     * 当前用户能否查看该稿件
     *
     * @param article 稿件，允许为 null
     * @param user    当前用户（未登录为 {@link CurrentUser#anonymous()}）
     */
    public boolean isVisible(ArticleEntity article, CurrentUser user) {
        if (article == null) {
            return false;
        }
        if (user.isAdmin()) {
            return true;
        }
        if (article.getExamineStatus() != null
                && article.getExamineStatus() == ExamineTypeEnum.SUCCESS.getCode()) {
            return true;
        }
        // 未通过审核：只有作者本人可见
        return user.isOwner(article.getUserId());
    }

    /** 无权查看时统一返回的视图对象 */
    public ArticleViewData hiddenView() {
        ArticleViewData articleViewData = new ArticleViewData();
        articleViewData.setIsShow(false);
        return articleViewData;
    }

    /**
     * 能否出现在公开列表里（首页、分类页、热门榜、推荐位）：
     * 类型对得上、没被删除、且已通过审核。
     *
     * 与 {@link #isVisible} 的区别是这里不给作者和管理员开后门——列表是发给所有人看的，
     * 不能因为「刚好是作者本人在刷列表」就把未过审的稿件漏出去
     */
    public boolean isPubliclyListed(ArticleEntity article, int type) {
        return article != null
                && article.getType() != null && article.getType() == type
                && Objects.equals(article.getStatus(), ArticleStatusEnum.NORMAL.getCode())
                && Objects.equals(article.getExamineStatus(), ExamineTypeEnum.SUCCESS.getCode());
    }
}
