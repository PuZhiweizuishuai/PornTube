package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.entity.CommentEntity;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;

import com.buguagaoshu.tiktube.dao.DislikeTableDao;
import com.buguagaoshu.tiktube.entity.DislikeTableEntity;
import com.buguagaoshu.tiktube.service.DislikeTableService;
import org.springframework.transaction.annotation.Transactional;



@Service("dislikeTableService")
public class DislikeTableServiceImpl extends ServiceImpl<DislikeTableDao, DislikeTableEntity> implements DislikeTableService {

    final ArticleService articleService;

    final CommentService commentService;

    @Autowired
    public DislikeTableServiceImpl(ArticleService articleService,
                                   CommentService commentService) {
        this.articleService = articleService;
        this.commentService = commentService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<DislikeTableEntity> page = this.page(
                new Query<DislikeTableEntity>().getPage(params),
                new QueryWrapper<DislikeTableEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggleDislike(Long dislikeObjId, Integer type, Long userId) {
        // 检查点踩目标是否存在
        Map<String, Object> map = new HashMap<>();
        boolean targetExists = false;
        if (type == 0) {
            // 检查文章是否存在且状态正常
            ArticleEntity article = articleService.getById(dislikeObjId);
            targetExists = article != null && article.getStatus() != 1;
        } else if (type == 1) {
            // 检查评论是否存在且状态正常
            CommentEntity comment = commentService.getById(dislikeObjId);
            targetExists = comment != null && comment.getStatus() != 1;
        } else {
            map.put("info", "点踩目标不存在");
            map.put("dislike", false);
            return map;
        }
        if (!targetExists) {
            map.put("info", "点踩目标不存在");
            map.put("dislike", false);
            return map;
        }
        // 检查用户是否已经点踩过

        DislikeTableEntity existingDislike = checkDislike(dislikeObjId, type, userId);
        if (existingDislike != null) {
            // 已点踩，执行取消点踩操作
            this.removeById(existingDislike.getId());

            // 更新点踩计数
            if (type == 0) {
                articleService.addCount("dislike_count", dislikeObjId, -1);
            } else {
                commentService.addCount("dislike_count", dislikeObjId, -1);
            }

            map.put("info", "取消点踩");
        } else {
            // 未点踩，执行点踩操作
            DislikeTableEntity dislikeTableEntity = new DislikeTableEntity();
            dislikeTableEntity.setUserId(userId);
            dislikeTableEntity.setDislikeObjId(dislikeObjId);
            dislikeTableEntity.setType(type);
            dislikeTableEntity.setCreateTime(System.currentTimeMillis());

            this.save(dislikeTableEntity);

            // 更新点踩计数
            if (type == 0) {
                articleService.addCount("dislike_count", dislikeObjId, 1L);
            } else {
                commentService.addCount("dislike_count", dislikeObjId, 1L);
            }

            map.put("info", "点踩成功！");
        }
        map.put("dislike", true);
        return map;
    }

    @Override
    public DislikeTableEntity checkDislike(Long dislikeObjId, Integer type, Long userId) {
        QueryWrapper<DislikeTableEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("dislike_obj_id", dislikeObjId)
                .eq("type", type);

        return this.getOne(queryWrapper);
    }

}