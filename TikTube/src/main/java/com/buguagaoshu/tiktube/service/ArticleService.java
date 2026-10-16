package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.buguagaoshu.tiktube.dto.ExamineDto;
import com.buguagaoshu.tiktube.dto.VideoArticleDto;
import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.entity.ArticleEntity;
import com.buguagaoshu.tiktube.vo.ArticleViewData;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 视频，图片，文章 发帖表
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


    /**
     * 视频稿件详情。不返回 null：稿件不存在或当前用户无权查看时返回
     * {@code isShow = false} 的隐藏视图
     */
    ArticleViewData getVideo(long id, HttpServletRequest request);

    /**
     * 增加弹幕数
     * */
    void addDanmakuCount(Long id, long count);

    /**
     * 获取用户视频或其他列表
     * @param params 分页参数
     * @param id 用户ID
     * @param type 稿件类型，传 -1 表示不限类型，非法值按视频处理
     * @return 分页结果
     * */
    PageUtils userArticleList(Map<String, Object> params, Long id, Integer type);


    /**
     * 获取关注用户的稿件
     *
     * @param userIds 关注列表，为空时直接返回空分页（不能拼成 {@code IN ()}）
     * */
    PageUtils followUserArticleList(Map<String, Object> params, Set<Long> userIds);


    /**
     * 获取用户上传的所有稿件
     * 仅登录后可访问
     * 只能查看自己的
     * */
    PageUtils userArticleList(Map<String, Object> params, String type, HttpServletRequest request);


    /**
     * 获取待审核稿件，仅管理员可见，非管理员返回 null
     * */
    PageUtils examineList(Map<String, Object> params, HttpServletRequest request);

    /**
     * 处理审核
     * */
    ReturnCodeEnum examine(ExamineDto examineDto, HttpServletRequest request);

    /**
     * 返回热门帖子
     * @param num 生成热点视频的数量
     * */
    List<ArticleViewData> hotView(int num);


    /**
     * 删除稿件（标记删除，同时把关联文件置为删除态）
     * @return  0 删除成功
     * 1 没权限
     * 2 不存在
     * */
    int deleteArticle(ArticleEntity entity, HttpServletRequest request);

    /**
     * 播放鉴权，顺带记录一次播放历史。
     * 目前站点没有分级播放限制，恒返回 true；真正的播放授权是调用方校验过的 AES key，
     * 这里只承担「记录播放历史」这一副作用。
     * TODO 后期开放 VIP / 分级播放时在此处补判定
     *
     * @param file 视频文件
     * @param userId 播放者，未登录为 -1
     * @param request 获取浏览器 UA
     * */
    boolean hasThisVideoPlayPower(FileTableEntity file, Long userId, HttpServletRequest request);

    /**
     * 返回当前分类下的稿件。不返回 null，分类不存在时返回空分页
     *
     * @param id 分类ID
     * @param params 查询参数
     * @return 视频列表
     * */
    PageUtils nowCategory(Map<String, Object> params, Integer id);


    /**
     * 编辑页回显数据，仅作者本人与管理员可取，其余情况返回 null
     */
    ArticleViewData getEditInfo(Long id, HttpServletRequest request);


    /**
     * 更新视频
     * */
    ReturnCodeEnum updateVideo(VideoArticleDto videoArticleDto, HttpServletRequest request);


    /**
     * 恢复被删除的稿件，仅管理员可操作
     * */
    boolean restore(ArticleEntity articleEntity, HttpServletRequest request);


    /**
     * 增减某个计数列。列名会被拼进 SQL，只接受
     * {@link com.buguagaoshu.tiktube.utils.SQLFilter#COUNT_COLUMNS} 白名单内的列
     */
    void addCount(String col, Long articleId, long count);

    /**
     * 根据文章ID获取相似推荐内容，候选不足时用热门榜补位
     * @param articleId 文章ID
     * @param limit 推荐数量限制
     * @return 推荐的文章列表
     */
    List<ArticleViewData> getRecommendationsByArticleId(Long articleId, int limit);
}
