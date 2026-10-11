package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.cache.CategoryCache;
import com.buguagaoshu.tiktube.cache.CountRecorder;
import com.buguagaoshu.tiktube.cache.PlayCountRecorder;
import com.buguagaoshu.tiktube.cache.WebSettingCache;
import com.buguagaoshu.tiktube.dao.ArticleTextDao;
import com.buguagaoshu.tiktube.dto.TextArticleDto;
import com.buguagaoshu.tiktube.entity.*;
import com.buguagaoshu.tiktube.enums.*;
import com.buguagaoshu.tiktube.support.ArticleEntityFactory;
import com.buguagaoshu.tiktube.support.ArticleViewEnricher;
import com.buguagaoshu.tiktube.support.ArticleVisibilityResolver;
import com.buguagaoshu.tiktube.support.CurrentUser;
import com.buguagaoshu.tiktube.support.CurrentUserResolver;
import com.buguagaoshu.tiktube.utils.IpUtil;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.utils.MarkdownUtils;
import com.buguagaoshu.tiktube.vo.ArticleViewData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-26
 */
@Slf4j
@Service
public class ArticleTextService extends ServiceImpl<ArticleTextDao, ArticleTextEntity> {
    private final ArticleService articleService;
    private final CategoryCache categoryCache;
    private final WebSettingCache webSettingCache;
    private final FileTableService fileTableService;
    private final UserService userService;
    private final IpUtil ipUtil;
    private final CountRecorder countRecorder;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final PlayCountRecorder playCountRecorder;
    private final CommentService commentService;

    /** 稿件可见性判定（与视频稿件共用） */
    private final ArticleVisibilityResolver visibilityResolver;

    /** 当前登录用户解析 */
    private final CurrentUserResolver currentUserResolver;

    /** 稿件实体构建（与视频稿件共用） */
    private final ArticleEntityFactory articleEntityFactory;

    /** 列表富化（作者信息 + 分类） */
    private final ArticleViewEnricher articleViewEnricher;

    @Autowired
    public ArticleTextService(ArticleService articleService,
                              CategoryCache categoryCache,
                              WebSettingCache webSettingCache,
                              FileTableService fileTableService,
                              UserService userService,
                              IpUtil ipUtil,
                              CountRecorder countRecorder,
                              PlayCountRecorder playCountRecorder,
                              CommentService commentService,
                              ArticleVisibilityResolver visibilityResolver,
                              CurrentUserResolver currentUserResolver,
                              ArticleEntityFactory articleEntityFactory,
                              ArticleViewEnricher articleViewEnricher) {
        this.articleService = articleService;
        this.categoryCache = categoryCache;
        this.webSettingCache = webSettingCache;
        this.fileTableService = fileTableService;
        this.userService = userService;
        this.ipUtil = ipUtil;
        this.countRecorder = countRecorder;
        this.playCountRecorder = playCountRecorder;
        this.commentService = commentService;
        this.visibilityResolver = visibilityResolver;
        this.currentUserResolver = currentUserResolver;
        this.articleEntityFactory = articleEntityFactory;
        this.articleViewEnricher = articleViewEnricher;
    }


    public ArticleViewData getText(long id, HttpServletRequest request) {
        // 一次解析出用户 ID 与角色，未登录为匿名用户（userId = -1）
        CurrentUser currentUser = currentUserResolver.resolve(request);

        // 构建查询条件（与视频稿件共用）
        QueryWrapper<ArticleEntity> wrapper =
                visibilityResolver.normalArticleWrapper(id, FileTypeEnum.ARTICLE.getCode());

        ArticleEntity articleEntity = articleService.getOne(wrapper);
        // 稿件不存在，或未通过审核且不是作者本人/管理员 → 返回隐藏视图
        if (!visibilityResolver.isVisible(articleEntity, currentUser)) {
            return visibilityResolver.hiddenView();
        }

        return buildArticleViewData(
                articleEntity,
                currentUser.getUserId(),
                ipUtil.getIpAddr(request),
                currentUser.isAdmin()
        );
    }

    private ArticleViewData buildArticleViewData(ArticleEntity article, long userId, String ip, boolean isAdmin) {
        ArticleViewData viewData = new ArticleViewData();
        countRecorder.syncArticleCount(article);

        // 数据同步
        BeanUtils.copyProperties(article, viewData);
        // 添加作者信息
        articleViewEnricher.applyAuthor(viewData, article.getUserId());
        viewData.setDanmakuCount(article.getDanmakuCount());
        // 获取文章内容
        List<ArticleTextEntity> articleTextByArticleId = findArticleTextByArticleId(article.getId());
        for (ArticleTextEntity articleTextEntity : articleTextByArticleId) {
            if (!isAdmin && article.getUserId() != userId) {
                if (articleTextEntity.getType() == TypeCode.ARTICLE_TEXT_PASSWORD) {
                    articleTextEntity.setContent(null);
                } else if (articleTextEntity.getType() == TypeCode.ARTICLE_TEXT_COMMENT) {
                    boolean b = commentService.hasUserCommentInArticle(article.getId(), userId);
                    if (!b) {
                        articleTextEntity.setContent(null);
                    }
                }
                articleTextEntity.setPassword("");
            }
        }
        // 对 articleTextByArticleId 进行排序，依据sort值，从小到大
        articleTextByArticleId.sort(Comparator.comparing(ArticleTextEntity::getSort));
        
        viewData.setText(articleTextByArticleId);
        // 添加标签
        try {
            if (StringUtils.hasText(article.getTag())) {
                viewData.setTag((List<String>) OBJECT_MAPPER.readValue(article.getTag(), List.class));
            }
        } catch (JsonProcessingException e) {
            log.warn("视频id为 {} 的投稿反序列化标签时出现异常", article.getId());
        }

        // 添加分类信息
        articleViewEnricher.applyCategory(viewData, article.getCategory());

        viewData.setIsShow(true);
        // 增加播放量
        playCountRecorder.recordPlay(viewData.getId(), ip);
        viewData.setViewCount(viewData.getViewCount() + playCountRecorder.getPlayCount(article.getId()));
        return viewData;
    }

    public List<ArticleTextEntity> findArticleTextByArticleId(long id) {
        QueryWrapper<ArticleTextEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", id);
        return this.list(wrapper);
    }


    public ArticleTextEntity getPasswordArticleTextEntity(long id, String password) {
        ArticleTextEntity byId = this.getById(id);
        if (byId.getPassword().equals(password) && byId.getType() == TypeCode.ARTICLE_TEXT_PASSWORD) {
            byId.setPassword("");
            return byId;
        }
        return null;
    }



    /**
     * 保存文章类型的稿件
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum saveText(TextArticleDto textArticleDto,
                                   HttpServletRequest request) {
        // 获取用户ID
        long userId = JwtUtil.getUserId(request);


        // 验证分类是否存在
        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(textArticleDto.getCategory());
        if (categoryEntity == null) {
            return ReturnCodeEnum.CATEGORY_NOT_FOUND;
        }


        // 构建文章实体（公共字段由 ArticleEntityFactory 处理）
        ArticleEntity articleEntity =
                articleEntityFactory.create(textArticleDto, userId, textArticleDto.getImgUrl());
        articleEntity.setType(FileTypeEnum.ARTICLE.getCode());

        // 设置IP和UA信息
        String ip = ipUtil.getIpAddr(request);
        articleEntity.setIp(ip);
        articleEntity.setUa(ipUtil.getUa(request));
        articleEntity.setCity(ipUtil.getCity(ip));

        // 保存文章基本信息
        articleService.save(articleEntity);

        // 更新用户最后发布时间
        userService.updateLastPublishTime(System.currentTimeMillis(), userId);

        // 保存文章内容
        saveArticleTextList(textArticleDto.getTextList(), articleEntity.getId(), userId);

        return ReturnCodeEnum.SUCCESS;
    }




    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum updateText(TextArticleDto textArticleDto,
                                     HttpServletRequest request) {
        // 获取用户ID
        long userId = JwtUtil.getUserId(request);

        // 获取原始文章信息
        ArticleEntity originalArticle = articleService.getById(textArticleDto.getId());
        if (originalArticle == null) {
            return ReturnCodeEnum.NOO_FOUND;
        }

        // 验证权限（只有作者或管理员可以修改）
        if (!hasEditPermission(originalArticle, userId, request)) {
            return ReturnCodeEnum.NO_POWER;
        }

        // 验证分类是否存在
        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(textArticleDto.getCategory());
        if (categoryEntity == null) {
            return ReturnCodeEnum.CATEGORY_NOT_FOUND;
        }

        // 更新文章基本信息
        updateArticleEntity(originalArticle, textArticleDto, request);
        articleService.updateById(originalArticle);

        // 更新文章内容（不删除原有内容，直接更新）
        updateArticleTextList(textArticleDto.getTextList(), originalArticle.getId(), userId);
        return ReturnCodeEnum.SUCCESS;
    }

    /**
     * 更新文章实体
     */
    private void updateArticleEntity(ArticleEntity article, TextArticleDto dto, HttpServletRequest request) {
        // 公共字段（标题/简介/分区/封面/标签/时间）由工厂统一处理；
        // 文本稿件的审核状态沿用站点配置，故 alwaysPending 传 false
        articleEntityFactory.applyUpdate(article, dto, dto.getImgUrl(), false);

        // 设置IP和UA信息
        String ip = ipUtil.getIpAddr(request);
        article.setUa(ipUtil.getUa(request));
        article.setIp(ip);
        article.setCity(ipUtil.getCity(ip));
    }

    /**
     * 检查是否有编辑权限
     */
    private boolean hasEditPermission(ArticleEntity article, long userId, HttpServletRequest request) {
        // 作者本人或管理员可以修改
        return currentUserResolver.isOwnerOrAdmin(article.getUserId(), request);
    }


    /**
     * 保存文章内容列表
     */
    private void saveArticleTextList(List<ArticleTextEntity> textList, Long articleId, Long userId) {
        long currentTime = System.currentTimeMillis();
        List<ArticleTextEntity> saveList = new ArrayList<>(textList.size());

        // 用来收集链接
        List<String> linkList = new ArrayList<>();

        for (ArticleTextEntity text : textList) {
            text.setArticleId(articleId);
            text.setUserId(userId);
            text.setCreateTime(currentTime);
            text.setUpdateTime(currentTime);
            text.setStatus(ArticleStatusEnum.NORMAL.getCode()); // 设置状态为正常
            List<String> strings = MarkdownUtils.extractLinks(text.getContent());
            linkList.addAll(strings);
            // 如果没有设置类型，默认为普通文章
            if (text.getType() == null) {
                text.setType(TypeCode.ARTICLE_TEXT_NORMAL);
            }

            saveList.add(text);
        }

        // 批量保存文章内容
        this.saveBatch(saveList);

        // 分析链接内容
        if (!linkList.isEmpty()) {
            analyzeAndUpdateFileReferencesForUpdate(linkList, userId, articleId);
        }
    }

    /**
     * 更新文章内容列表
     */
    private void updateArticleTextList(List<ArticleTextEntity> textList, Long articleId, Long userId) {
        long currentTime = System.currentTimeMillis();
        
        // 获取原有的文章内容
        QueryWrapper<ArticleTextEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("article_id", articleId);
        List<ArticleTextEntity> existingTextList = this.list(queryWrapper);
        
        // 用来收集链接
        List<String> linkList = new ArrayList<>();
        
        // 处理新的文章内容
        for (int i = 0; i < textList.size(); i++) {
            ArticleTextEntity text = textList.get(i);
            text.setArticleId(articleId);
            text.setUserId(userId);
            text.setUpdateTime(currentTime);
            text.setStatus(ArticleStatusEnum.NORMAL.getCode()); // 设置状态为正常
            text.setSort(i); // 设置排序
            
            // 提取链接
            List<String> strings = MarkdownUtils.extractLinks(text.getContent());
            linkList.addAll(strings);
            
            // 如果没有设置类型，默认为普通文章
            if (text.getType() == null) {
                text.setType(TypeCode.ARTICLE_TEXT_NORMAL);
            }
        }
        
        // 如果原有内容为空，直接保存新内容
        if (existingTextList.isEmpty()) {
            for (ArticleTextEntity text : textList) {
                text.setCreateTime(currentTime);
            }
            this.saveBatch(textList);
        } else {
            // 删除多余的旧内容
            if (existingTextList.size() > textList.size()) {
                List<Long> idsToRemove = new ArrayList<>();
                for (int i = textList.size(); i < existingTextList.size(); i++) {
                    idsToRemove.add(existingTextList.get(i).getId());
                }
                if (!idsToRemove.isEmpty()) {
                    this.removeByIds(idsToRemove);
                }
            }
            
            // 更新或新增内容
            for (int i = 0; i < textList.size(); i++) {
                ArticleTextEntity newText = textList.get(i);
                
                // 如果索引在现有列表范围内，更新现有内容
                if (i < existingTextList.size()) {
                    ArticleTextEntity existingText = existingTextList.get(i);
                    newText.setId(existingText.getId());
                    newText.setCreateTime(existingText.getCreateTime());
                    this.updateById(newText);
                } else {
                    // 否则添加新内容
                    newText.setCreateTime(currentTime);
                    this.save(newText);
                }
            }
        }
        
        // 分析链接内容并更新文件引用（使用专门的更新方法）
        analyzeAndUpdateFileReferencesForUpdate(linkList, userId, articleId);
    }

    /**
     * 分析链接并更新文件引用状态（用于更新文章时）
     *
     * @param newLinkList 新的链接列表
     * @param userId      用户ID
     * @param articleId   文章ID
     */
    private void analyzeAndUpdateFileReferencesForUpdate(List<String> newLinkList, Long userId, Long articleId) {
        // 1. 获取文章当前关联的所有文件
        QueryWrapper<FileTableEntity> currentFilesWrapper = new QueryWrapper<>();
        currentFilesWrapper.eq("article_id", articleId);
        currentFilesWrapper.eq("upload_user_id", userId);
        currentFilesWrapper.eq("status", FileStatusEnum.USED.getCode());
        List<FileTableEntity> currentFiles = fileTableService.list(currentFilesWrapper);
        
        // 2. 从新链接中提取文件名
        List<String> newFileNames = new ArrayList<>();
        for (String link : newLinkList) {
            String fileName = extractFileNameFromLink(link);
            if (fileName != null && !fileName.isEmpty()) {
                newFileNames.add(fileName);
            }
        }
        
        // 3. 找出需要取消引用的文件（在当前文件中但不在新文件名列表中）
        List<FileTableEntity> filesToUnreference = new ArrayList<>();
        for (FileTableEntity file : currentFiles) {
            if (!newFileNames.contains(file.getFileNewName())) {
                file.setStatus(FileStatusEnum.NOT_USE_FILE.getCode());
                file.setArticleId(null); // 取消与文章的关联
                filesToUnreference.add(file);
            }
        }
        
        // 4. 批量更新取消引用的文件
        if (!filesToUnreference.isEmpty()) {
            fileTableService.updateBatchById(filesToUnreference);
        }
        
        // 5. 找出需要新增引用的文件（在新文件名列表中但当前未被引用）
        if (!newFileNames.isEmpty()) {
            // 获取所有匹配的未使用文件
            QueryWrapper<FileTableEntity> newFilesWrapper = new QueryWrapper<>();
            newFilesWrapper.in("file_new_name", newFileNames);
            newFilesWrapper.eq("upload_user_id", userId);
            newFilesWrapper.ne("status", FileStatusEnum.USED.getCode());
            List<FileTableEntity> filesToReference = fileTableService.list(newFilesWrapper);
            
            // 更新这些文件的状态为已使用
            if (!filesToReference.isEmpty()) {
                for (FileTableEntity file : filesToReference) {
                    file.setStatus(FileStatusEnum.USED.getCode());
                    file.setArticleId(articleId);
                }
                fileTableService.updateBatchById(filesToReference);
            }
        }
    }

    /**
     * 从链接中提取文件名
     *
     * @param link 链接地址
     * @return 文件名
     */
    private String extractFileNameFromLink(String link) {
        if (link == null || link.isEmpty()) {
            return null;
        }

        // 处理形如 /ddd/ddd/dddd.mp4 的链接
        int lastSlashIndex = link.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < link.length() - 1) {
            return link.substring(lastSlashIndex + 1);
        }

        return link; // 如果没有斜杠，则返回整个链接作为文件名
    }
}
