package com.buguagaoshu.porntube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.entity.CommentEntity;
import com.buguagaoshu.porntube.vo.CommentVo;
import com.buguagaoshu.porntube.vo.CommentWithUserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface CommentService extends IService<CommentEntity> {

    PageUtils queryPage(Map<String, Object> params, HttpServletRequest request);

    /**
     * 保存评论
     * */
    CommentEntity saveComment(CommentVo commentVo, HttpServletRequest request);


    PageUtils commentList(Map<String, Object> params, long articleId, long fatherId, int type, Integer sort);

    void addCount(String col, long commentId, long count);
}

