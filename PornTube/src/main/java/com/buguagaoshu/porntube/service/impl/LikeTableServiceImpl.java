package com.buguagaoshu.porntube.service.impl;

import com.buguagaoshu.porntube.entity.ArticleEntity;
import com.buguagaoshu.porntube.entity.CommentEntity;
import com.buguagaoshu.porntube.enums.ReturnCodeEnum;
import com.buguagaoshu.porntube.service.ArticleService;
import com.buguagaoshu.porntube.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;

import com.buguagaoshu.porntube.dao.LikeTableDao;
import com.buguagaoshu.porntube.entity.LikeTableEntity;
import com.buguagaoshu.porntube.service.LikeTableService;
import org.springframework.transaction.annotation.Transactional;


@Service("likeTableService")
public class LikeTableServiceImpl extends ServiceImpl<LikeTableDao, LikeTableEntity> implements LikeTableService {
    @Autowired
    ArticleService articleService;

    @Autowired
    CommentService commentService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<LikeTableEntity> page = this.page(
                new Query<LikeTableEntity>().getPage(params),
                new QueryWrapper<LikeTableEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggleLike(Long likeObjId, Integer type, Long userId) {
        // 检查点赞目标是否存在
        Map<String, Object> map = new HashMap<>();
        boolean targetExists = false;
        if (type == 0) {
            // 检查文章是否存在且状态正常
            ArticleEntity article = articleService.getById(likeObjId);
            targetExists = article != null && article.getStatus() != 1;
        } else if (type == 1) {
            // 检查评论是否存在且状态正常
            CommentEntity comment = commentService.getById(likeObjId);
            targetExists = comment != null && comment.getStatus() != 1;
        } else {
            map.put("info", "点赞目标不存在");
            map.put("like", false);
            return map;
        }
        if (!targetExists) {
            map.put("info", "点赞目标不存在");
            map.put("like", false);
            return map;
        }
        // 检查用户是否已经点赞过

        LikeTableEntity existingLike = checkLike(likeObjId, type, userId);
        if (existingLike != null) {
            // 已点赞，执行取消点赞操作
            this.removeById(existingLike.getId());

            // 更新点赞计数
            if (type == 0) {
                articleService.addCount("like_count", likeObjId, -1);
            } else {
                commentService.addCount("like_count", likeObjId, -1);
            }

            map.put("info", "已经点过赞了");
        } else {
            // 未点赞，执行点赞操作
            LikeTableEntity likeTableEntity = new LikeTableEntity();
            likeTableEntity.setUserId(userId);
            likeTableEntity.setLikeObjId(likeObjId);
            likeTableEntity.setType(type);
            likeTableEntity.setCreateTime(System.currentTimeMillis());

            this.save(likeTableEntity);

            // 更新点赞计数
            if (type == 0) {
                articleService.addCount("like_count", likeObjId, 1L);
            } else {
                commentService.addCount("like_count", likeObjId, 1L);
            }

            map.put("info", "点赞成功！");
        }
        map.put("like", true);
        return map;
    }

    @Override
    public LikeTableEntity checkLike(Long likeObjId, Integer type, Long userId) {
        QueryWrapper<LikeTableEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("like_obj_id", likeObjId)
                .eq("type", type);

        return this.getOne(queryWrapper);
    }

}