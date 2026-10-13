package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.cache.*;
import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.dto.ExamineDto;
import com.buguagaoshu.tiktube.dto.VideoArticleDto;
import com.buguagaoshu.tiktube.entity.*;
import com.buguagaoshu.tiktube.enums.*;
import com.buguagaoshu.tiktube.exception.UserNotLoginException;
import com.buguagaoshu.tiktube.service.*;
import com.buguagaoshu.tiktube.support.ArticleEntityFactory;
import com.buguagaoshu.tiktube.support.ArticleViewEnricher;
import com.buguagaoshu.tiktube.support.ArticleVisibilityResolver;
import com.buguagaoshu.tiktube.support.CurrentUser;
import com.buguagaoshu.tiktube.support.CurrentUserResolver;
import com.buguagaoshu.tiktube.support.VideoRanking;
import com.buguagaoshu.tiktube.utils.*;
import com.buguagaoshu.tiktube.vo.ArticleViewData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;

import com.buguagaoshu.tiktube.dao.ArticleDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 文章服务实现类
 *
 * @author Pu Zhiwei
 */
@Slf4j
@Service("articleService")
public class ArticleServiceImpl extends ServiceImpl<ArticleDao, ArticleEntity> implements ArticleService {

    private static final String TAG = "ArticleServiceImpl";
    private static final int ONE_DAY_MILLIS = 86400000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 热门榜的时间窗：只在这个窗口里召回候选，配合 create_time 索引避免每次刷新扫全库
     */
    private static final long TRENDING_WINDOW_MILLIS = 30L * ONE_DAY_MILLIS;

    /**
     * 一阶段多召回多少候选留给精排
     */
    private static final int TREND_RECALL_FACTOR = 2;

    /**
     * 播放增量键数量超过这个值就不再用它补候选，避免扫描过多 Redis 键
     */
    private static final int MAX_PENDING_PLAY_KEYS = 5000;

    private final CategoryCache categoryCache;
    private final WebConfigCache webConfigCache;
    private final FileTableService fileTableService;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private PlayRecordingService playRecordingService;
    private NotificationService notificationService;

    private final PlayCountRecorder playCountRecorder;

    private final CountRecorder countRecorder;

    private final IpUtil ipUtil;

    /** 稿件可见性判定（与文本稿件共用） */
    private final ArticleVisibilityResolver visibilityResolver;

    /** 当前登录用户解析（收敛 try{getUserId}catch 惯用法） */
    private final CurrentUserResolver currentUserResolver;

    /** 稿件实体构建（与文本稿件共用） */
    private final ArticleEntityFactory articleEntityFactory;

    /** 列表富化（作者信息 + 分类 + 缓存播放量） */
    private final ArticleViewEnricher articleViewEnricher;

    /** 热门榜快照，相关推荐补位用它 */
    private final HotCache hotCache;

    /** 标签倒排索引，相关推荐的召回与打分都在内存里完成 */
    private final VideoCatalogCache videoCatalogCache;

    @Autowired
    public ArticleServiceImpl(CategoryCache categoryCache,
                              WebConfigCache webConfigCache,
                              FileTableService fileTableService,
                              UserService userService,
                              UserRoleService userRoleService,
                              NotificationService notificationService,
                              PlayCountRecorder playCountRecorder,
                              CountRecorder countRecorder,
                              IpUtil ipUtil,
                              ArticleVisibilityResolver visibilityResolver,
                              CurrentUserResolver currentUserResolver,
                              ArticleEntityFactory articleEntityFactory,
                              ArticleViewEnricher articleViewEnricher,
                              HotCache hotCache,
                              VideoCatalogCache videoCatalogCache) {
        this.categoryCache = categoryCache;
        this.webConfigCache = webConfigCache;
        this.fileTableService = fileTableService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.notificationService = notificationService;
        this.playCountRecorder = playCountRecorder;
        this.countRecorder = countRecorder;
        this.ipUtil = ipUtil;
        this.visibilityResolver = visibilityResolver;
        this.currentUserResolver = currentUserResolver;
        this.articleEntityFactory = articleEntityFactory;
        this.articleViewEnricher = articleViewEnricher;
        this.hotCache = hotCache;
        this.videoCatalogCache = videoCatalogCache;
    }

    @Autowired
    public void setPlayRecordingService(PlayRecordingService playRecordingService) {
        this.playRecordingService = playRecordingService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<ArticleEntity> wrapper = createNormalArticleWrapper();
        String search = (String) params.get("search");
        String type = (String) params.get("type");
        if (type != null) {
            wrapper.eq("type", type);
        }
        if (search != null && !search.isEmpty()) {
            wrapper.like("title", search);
            // wrapper.or().like("description", search);
        }
        IPage<ArticleEntity> page = this.page(
                new Query<ArticleEntity>().getPage(params),
                wrapper
        );
        return addUserInfo(page);
    }

    /**
     * 创建普通文章查询条件
     */
    private QueryWrapper<ArticleEntity> createNormalArticleWrapper() {
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());
        wrapper.orderByDesc("create_time");
        return wrapper;
    }

    /**
     * 创建通用查询条件
     *
     * @param conditions  查询条件键值对
     * @param orderByDesc 是否按创建时间降序
     */
    private QueryWrapper<ArticleEntity> createQueryWrapper(Map<String, Object> conditions, boolean orderByDesc) {
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        conditions.forEach(wrapper::eq);

        if (orderByDesc) {
            wrapper.orderByDesc("create_time");
        }

        return wrapper;
    }

    /**
     * 为文章添加用户信息（含分类与缓存播放量）
     *
     * 具体实现已抽取到 {@link ArticleViewEnricher}，与文本稿件共用同一套富化逻辑。
     */
    public PageUtils addUserInfo(IPage<ArticleEntity> page) {
        return articleViewEnricher.enrichPage(page);
    }

    public List<ArticleViewData> addUserInfo(List<ArticleEntity> articleEntityList) {
        return articleViewEnricher.enrich(articleEntityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum saveVideo(VideoArticleDto videoArticleDto, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);

        // 参数验证
        if (videoArticleDto.getImageId() == null) {
            return ReturnCodeEnum.DATA_VALID_EXCEPTION;
        }
        ReturnCodeEnum validationResult = validateVideoArticleDto(videoArticleDto);
        if (validationResult != ReturnCodeEnum.SUCCESS) {
            return validationResult;
        }

        // 批量获取文件实体以减少数据库查询
        List<Long> fileIds = new ArrayList<>(2);
        fileIds.add(videoArticleDto.getImageId());
        fileIds.add(videoArticleDto.getVideo().getId());
        List<FileTableEntity> files = fileTableService.listByIds(fileIds);

        // TODO 暂未实现视频分 P 功能，实现后移除此判断
        if (files.size() != 2) {
            return ReturnCodeEnum.DATA_VALID_EXCEPTION;
        }

        // 将文件按ID分类
        Map<Long, FileTableEntity> fileMap = files.stream()
                .collect(Collectors.toMap(FileTableEntity::getId, file -> file));

        FileTableEntity imageFile = fileMap.get(videoArticleDto.getImageId());
        FileTableEntity videoFile = fileMap.get(videoArticleDto.getVideo().getId());

        // 验证文件所有权
        if (!validateFileOwnership(imageFile, videoFile, userId)) {
            return ReturnCodeEnum.NO_POWER;
        }

        // 构建文章实体
        ArticleEntity articleEntity = buildArticleEntity(videoArticleDto, userId, imageFile, videoFile);
        String ip = ipUtil.getIpAddr(request);
        articleEntity.setIp(ip);
        articleEntity.setUa(ipUtil.getUa(request));
        articleEntity.setCity(ipUtil.getCity(ip));
        // 保存文章
        this.save(articleEntity);
        userService.updateLastPublishTime(System.currentTimeMillis(), userId);
        // 在同一事务中更新文件状态
        updateFileStatus(articleEntity.getId(), imageFile, videoFile);

        return ReturnCodeEnum.SUCCESS;
    }

    /**
     * 验证视频文章DTO
     */
    private ReturnCodeEnum validateVideoArticleDto(VideoArticleDto dto) {
        // 分类验证
        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(dto.getCategory());
        if (categoryEntity == null) {
            return ReturnCodeEnum.CATEGORY_NOT_FOUND;
        }
        return ReturnCodeEnum.SUCCESS;
    }

    /**
     * 验证文件所有权
     */
    private boolean validateFileOwnership(FileTableEntity imageFile, FileTableEntity videoFile, long userId) {
        // 检查图片所有权
        if (imageFile == null || imageFile.getUploadUserId() != userId) {
            return false;
        }

        // 检查视频权限
        if (videoFile == null || videoFile.getUploadUserId() != userId) {
            return false;
        }

        return true;
    }

    @Override
    public ReturnCodeEnum updateVideo(VideoArticleDto videoArticleDto, HttpServletRequest request) {
        // 加载用户 ID
        long userId = JwtUtil.getUserId(request);
        // 读取原始稿件信息
        ArticleViewData video = getEditInfo(videoArticleDto.getId(), request);
        if (video == null) {
            return ReturnCodeEnum.NO_POWER;
        }

        // 参数验证
        ReturnCodeEnum validationResult = validateVideoArticleDto(videoArticleDto);
        if (validationResult != ReturnCodeEnum.SUCCESS) {
            return validationResult;
        }

        // 获取文件实体
        FileTableEntity imageFile = fileTableService.getById(videoArticleDto.getImageId());
        FileTableEntity videoFile = fileTableService.getById(videoArticleDto.getVideo().getId());

        // 验证文件所有权
        if (!validateFileOwnership(imageFile, videoFile, userId)) {
            return ReturnCodeEnum.NO_POWER;
        }

        // 更新文章实体
        ArticleEntity articleEntity = this.getById(videoArticleDto.getId());
        updateArticleEntity(articleEntity, videoArticleDto, imageFile, videoFile, request);

        // 更新数据库
        this.updateById(articleEntity);

        // 处理文件状态更新
        handleFileStatusUpdate(articleEntity.getId(), imageFile, videoFile, video);

        return ReturnCodeEnum.SUCCESS;
    }

    /**
     * 更新文章实体信息
     */
    private void updateArticleEntity(ArticleEntity article, VideoArticleDto dto,
                                     FileTableEntity imageFile, FileTableEntity videoFile,
                                     HttpServletRequest request) {
        // 公共字段（标题/简介/分区/封面/标签/审核状态重置）由工厂统一处理
        articleEntityFactory.applyUpdate(article, dto, imageFile.getFileUrl());

        String ip = ipUtil.getIpAddr(request);
        article.setUa(ipUtil.getUa(request));
        article.setIp(ip);
        article.setCity(ipUtil.getCity(ip));

        article.setType(FileTypeEnum.VIDEO.getCode());

        // 设置视频信息
        article.setDuration(videoFile.getDuration());
        article.setPixelsNumber(videoFile.getPixelsNumber());
        article.setFrameRate(videoFile.getFrameRate());
    }

    /**
     * 处理文件状态更新
     */
    private void handleFileStatusUpdate(Long articleId, FileTableEntity newImageFile,
                                        FileTableEntity newVideoFile, ArticleViewData originalVideo) {
        // 读取原始与稿件相关联的文件
        List<FileTableEntity> fileTableServiceArticle = fileTableService.findArticle(articleId);

        if (fileTableServiceArticle.isEmpty()) {
            // 如果没有找到关联文件，直接设置新文件状态
            List<FileTableEntity> updateFileList = new ArrayList<>(2);
            updateFileAssociation(newImageFile, articleId, FileStatusEnum.USED.getCode(), updateFileList);
            updateFileAssociation(newVideoFile, articleId, FileStatusEnum.USED.getCode(), updateFileList);
            fileTableService.updateBatchById(updateFileList);
            return;
        }

        // 将文件分类并创建ID到文件的映射，提高后续查找效率
        Map<Integer, List<FileTableEntity>> fileTypeMap = fileTableServiceArticle.stream()
                .collect(Collectors.groupingBy(FileTableEntity::getType));

        Map<Long, FileTableEntity> fileIdMap = fileTableServiceArticle.stream()
                .collect(Collectors.toMap(FileTableEntity::getId, file -> file, (f1, f2) -> f1));

        List<FileTableEntity> updateFileList = new ArrayList<>();

        // 处理视频文件变化
        updateVideoFileStatus(originalVideo, newVideoFile, fileIdMap, articleId, updateFileList);

        // 处理图片文件变化
        updateImageFileStatus(originalVideo, newImageFile, fileTypeMap, fileIdMap, articleId, updateFileList);

        // 仅当有更新时才执行批量更新操作
        if (!updateFileList.isEmpty()) {
            log.info("更新文章ID: {}，更新文件数量: {}", articleId, updateFileList.size());
            fileTableService.updateBatchById(updateFileList);
        }
    }

    /**
     * 更新视频文件状态
     */
    private void updateVideoFileStatus(ArticleViewData originalVideo, FileTableEntity newVideoFile,
                                       Map<Long, FileTableEntity> fileIdMap,
                                       Long articleId, List<FileTableEntity> updateFileList) {
        // 检查原始视频信息
        if (originalVideo.getVideo() != null && !originalVideo.getVideo().isEmpty()) {
            // 获取原始视频ID
            Long originalVideoId = originalVideo.getVideo().stream()
                    .filter(v -> v.getType().equals(FileTypeEnum.VIDEO.getCode()))
                    .map(FileTableEntity::getId)
                    .findFirst()
                    .orElse(null);

            // 检查视频是否变化
            if (originalVideoId != null && !originalVideoId.equals(newVideoFile.getId())) {
                // 新视频关联到文章
                updateFileAssociation(newVideoFile, articleId, FileStatusEnum.USED.getCode(), updateFileList);

                // 找到原始视频并修改状态，使用Map直接查找提高效率
                FileTableEntity originalV = fileIdMap.get(originalVideoId);
                if (originalV != null) {
                    updateFileAssociation(originalV, articleId, FileStatusEnum.NOT_USE_FILE.getCode(), updateFileList);
                }
            } else if (originalVideoId == null) {
                // 如果找不到原始视频ID，关联新视频
                updateFileAssociation(newVideoFile, articleId, FileStatusEnum.USED.getCode(), updateFileList);
            }
        } else {
            // 如果原视频列表为空，确保新视频关联到文章
            updateFileAssociation(newVideoFile, articleId, FileStatusEnum.USED.getCode(), updateFileList);
        }
    }

    /**
     * 更新图片文件状态
     */
    private void updateImageFileStatus(ArticleViewData originalVideo, FileTableEntity newImageFile,
                                       Map<Integer, List<FileTableEntity>> fileTypeMap,
                                       Map<Long, FileTableEntity> fileIdMap,
                                       Long articleId, List<FileTableEntity> updateFileList) {
        // 获取原始图片URL
        String originalImageUrl = originalVideo.getImgUrl();

        // 检查新图片是否与原图片不同
        if (!newImageFile.getFileUrl().equals(originalImageUrl)) {
            // 关联新图片
            updateFileAssociation(newImageFile, articleId, FileStatusEnum.USED.getCode(), updateFileList);

            // 查找与原图片URL匹配的文件并更新状态
            fileTypeMap.values().stream()
                    .flatMap(List::stream)
                    .filter(file -> file.getFileUrl().equals(originalImageUrl))
                    .findFirst()
                    .ifPresent(file -> updateFileAssociation(file, articleId, FileStatusEnum.NOT_USE_FILE.getCode(), updateFileList));
        } else if (!Objects.equals(newImageFile.getId(), originalVideo.getImageId())) {
            // 如果URL相同但ID不同，确保新图片被正确关联
            updateFileAssociation(newImageFile, articleId, FileStatusEnum.USED.getCode(), updateFileList);

            // 如果原图片ID存在于映射中，更新其状态
            FileTableEntity oldImage = fileIdMap.get(originalVideo.getImageId());
            if (oldImage != null) {
                updateFileAssociation(oldImage, articleId, FileStatusEnum.NOT_USE_FILE.getCode(), updateFileList);
            }
        }
    }

    /**
     * 更新文件关联和状态
     */
    private void updateFileAssociation(FileTableEntity file, Long articleId, Integer status, List<FileTableEntity> updateList) {
        file.setArticleId(articleId);
        file.setStatus(status);
        updateList.add(file);
    }

    /**
     * 构建文章实体
     *
     * 公共字段（标题/简介/分区/封面/标签/时间/审核状态/计数）由
     * {@link ArticleEntityFactory} 统一处理，这里只补视频专属字段。
     */
    private ArticleEntity buildArticleEntity(VideoArticleDto dto, long userId, FileTableEntity imageFile, FileTableEntity videoFile) {
        ArticleEntity article = articleEntityFactory.create(dto, userId, imageFile.getFileUrl());

        // 设置视频信息
        article.setType(FileTypeEnum.VIDEO.getCode());
        article.setDuration(videoFile.getDuration());
        article.setPixelsNumber(videoFile.getPixelsNumber());
        article.setFrameRate(videoFile.getFrameRate());

        return article;
    }

    /**
     * 更新文件状态
     */
    private void updateFileStatus(Long articleId, FileTableEntity imageFile, FileTableEntity videoFile) {
        // 创建要更新的文件列表
        List<FileTableEntity> files = new ArrayList<>(2);

        // 设置视频文件状态
        videoFile.setArticleId(articleId);
        videoFile.setStatus(FileStatusEnum.USED.getCode());
        files.add(videoFile);

        // 设置图片文件状态
        imageFile.setArticleId(articleId);
        imageFile.setStatus(FileStatusEnum.USED.getCode());
        files.add(imageFile);

        // 批量更新以减少数据库交互次数
        fileTableService.updateBatchById(files);
    }

    @Override
    public ArticleViewData getVideo(long id, HttpServletRequest request) {
        // 一次解析出用户 ID 与角色，未登录为匿名用户（userId = -1）
        CurrentUser currentUser = currentUserResolver.resolve(request);

        // 构建查询条件（与文本稿件共用）
        QueryWrapper<ArticleEntity> wrapper =
                visibilityResolver.normalArticleWrapper(id, FileTypeEnum.VIDEO.getCode());

        ArticleEntity articleEntity = this.getOne(wrapper);
        // 稿件不存在，或未通过审核且不是作者本人/管理员 → 返回隐藏视图
        if (!visibilityResolver.isVisible(articleEntity, currentUser)) {
            return visibilityResolver.hiddenView();
        }

        return buildArticleViewData(articleEntity, currentUser.getUserId());
    }

    /**
     * 构建文章视图数据
     */
    private ArticleViewData buildArticleViewData(ArticleEntity article, long userId) {
        ArticleViewData viewData = new ArticleViewData();

        countRecorder.syncArticleCount(article);

        // 数据同步
        BeanUtils.copyProperties(article, viewData);

        // 添加作者信息
        articleViewEnricher.applyAuthor(viewData, article.getUserId());
        viewData.setDanmakuCount(article.getDanmakuCount());

        // 批量获取视频信息
        List<FileTableEntity> videos = fileTableService.findArticle(article.getId());
        long time = System.currentTimeMillis();

        // 并行处理文件列表以生成加密密钥
        videos.parallelStream().forEach(video -> {
            video.setKey(AesUtil.getInstance().encrypt(
                    userId + "#" + video.getId() + "#" + (time + WebConstant.KEY_EXPIRY_DATE) + "#" + video.getFileNewName()
            ));
            if (video.getType().equals(FileTypeEnum.PHOTO.getCode()) || video.getType().equals(FileTypeEnum.VIDEO_PHOTO.getCode())) {
                viewData.setImageId(video.getId());
            }
        });
        viewData.setVideo(videos);

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
        viewData.setViewCount(viewData.getViewCount() + playCountRecorder.getPlayCount(article.getId()));

        return viewData;
    }

    @Override
    public void addDanmakuCount(Long id, long count) {
        this.baseMapper.addDanmakuCount(id, count);
    }

    /**
     * 个人主页视频列表
     */
    @Override
    public PageUtils userArticleList(Map<String, Object> params, Long id, Integer type) {
        // 参数验证
        if (type == null || (type < FileTypeEnum.VIDEO.getCode() - 1 && type > FileTypeEnum.ARTICLE.getCode())) {
            type = FileTypeEnum.VIDEO.getCode();
        }

        // 构建查询条件

        Map<String, Object> conditions = Map.of(
                "user_id", id,
                "status", ArticleStatusEnum.NORMAL.getCode(),
                "examine_status", ExamineTypeEnum.SUCCESS.getCode(),
                "type", type
        );
        if (type == -1) {
            conditions = Map.of(
                    "user_id", id,
                    "status", ArticleStatusEnum.NORMAL.getCode(),
                    "examine_status", ExamineTypeEnum.SUCCESS.getCode()
            );
        }
        QueryWrapper<ArticleEntity> wrapper = createQueryWrapper(conditions, true);

        IPage<ArticleEntity> page = this.page(
                new Query<ArticleEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(addArticleCategory(page), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public PageUtils fallowUserArticleList(Map<String, Object> params, Set<Long> userIds) {
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());
        wrapper.in("user_id", userIds);
        wrapper.orderByDesc("update_time");
        IPage<ArticleEntity> page = this.page(
                new Query<ArticleEntity>().getPage(params),
                wrapper
        );
        return addUserInfo(page);
    }

    @Override
    public PageUtils userArticleList(Map<String, Object> params, String type, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();

        // 如果是管理员，加载所有数据
        if ("admin".equals(type) && currentUserResolver.isAdmin(request)) {
            String active = (String) params.get("active");
            if ("delete".equals(active)) {
                wrapper.eq("status", ArticleStatusEnum.DELETE.getCode());
            }
        } else {
            wrapper.eq("user_id", userId);
            wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        }

        wrapper.orderByDesc("create_time");

        IPage<ArticleEntity> page = this.page(
                new Query<ArticleEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils examineList(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        // 权限验证
        if (!currentUserResolver.isAdmin(request)) {
            return null;
        }

        // 使用Map简化条件构建
        Map<String, Object> conditions = Map.of(
                "examine_status", ExamineTypeEnum.PENDING_REVIEW.getCode()
        );

        QueryWrapper<ArticleEntity> wrapper = createQueryWrapper(conditions, true);

        IPage<ArticleEntity> page = this.page(
                new Query<ArticleEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(addArticleCategory(page), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum examine(ExamineDto examineDto, HttpServletRequest request) {
        ArticleEntity articleEntity = this.getById(examineDto.getVideoId());
        long userId = JwtUtil.getUserId(request);
        long time = System.currentTimeMillis();
        if (articleEntity == null) {
            return ReturnCodeEnum.NOO_FOUND;
        }

        if (articleEntity.getExamineStatus() == ExamineTypeEnum.SUCCESS.getCode()) {
            return ReturnCodeEnum.REVIEWED;
        }

        // 更新审核状态
        if (examineDto.getResult()) {
            // 审通过
            articleEntity.setExamineStatus(ExamineTypeEnum.SUCCESS.getCode());
            // 只有审核通过才增加投稿数量
            userService.addSubmitCount(articleEntity.getUserId(), 1);
            notificationService.sendNotification(
                    userId,
                    articleEntity.getUserId(),
                    articleEntity.getId(),
                    articleEntity.getId(),
                    -1,
                    "你的稿件已通过审核，现在所有人都能看见你的稿件了",
                    NotificationType.createExamineLink(articleEntity.getTitle(), articleEntity.getId()),
                    "你的稿件 《" + articleEntity.getTitle() + "》 已通过审核，现在所有人都能看见你的稿件了",
                    NotificationType.SYSTEM
            );
        } else {
            // 不通过
            articleEntity.setExamineStatus(examineDto.getType());
            notificationService.sendNotification(
                    userId,
                    articleEntity.getUserId(),
                    articleEntity.getId(),
                    articleEntity.getId(),
                    -1,
                    "稿件未通过审核",
                    NotificationType.createExamineLink(articleEntity.getTitle(), articleEntity.getId()),
                    NotificationType.createExamineContent(articleEntity.getTitle(), examineDto.getType(), examineDto.getMessage()),
                    NotificationType.SYSTEM
            );
        }
        articleEntity.setUpdateTime(time);
        articleEntity.setExamineUser(userId);
        articleEntity.setExamineMessage(examineDto.getMessage());


        this.updateById(articleEntity);
        return ReturnCodeEnum.SUCCESS;
    }


    @Override
    public List<ArticleViewData> hotView(int num) {
        if (num <= 0) {
            return new ArrayList<>();
        }
        long now = System.currentTimeMillis();
        int videoType = FileTypeEnum.VIDEO.getCode();

        // 一阶段：数据库按衰减热度在窗口内召回候选，多取一倍留给精排
        List<ArticleEntity> candidates = new ArrayList<>(baseMapper.findTrendingVideos(
                now, now - TRENDING_WINDOW_MILLIS, num * TREND_RECALL_FACTOR, videoType));
        if (candidates.size() < num) {
            // 站内稿件少的时候窗口内凑不满榜单，退化成不带时间窗的同公式查询
            candidates = new ArrayList<>(baseMapper.findTrendingVideos(
                    now, null, num * TREND_RECALL_FACTOR, videoType));
        } else {
            // 带时间窗的召回只看得见新稿件，老稿件今天突然火起来的情况靠播放增量补一路候选
            candidates.addAll(recentSurgeCandidates(num, videoType));
        }

        // 二阶段：叠加还没同步回数据库的计数增量，用同一套公式精排
        Map<Long, ArticleEntity> uniqueCandidates = new LinkedHashMap<>();
        for (ArticleEntity candidate : candidates) {
            if (candidate.getId() != null) {
                uniqueCandidates.putIfAbsent(candidate.getId(), candidate);
            }
        }
        List<ArticleEntity> ranked = new ArrayList<>(uniqueCandidates.values());
        for (ArticleEntity candidate : ranked) {
            candidate.setSort(liveTrendScore(candidate, now));
        }
        ranked.sort(Comparator.comparingDouble(ArticleEntity::getSort).reversed());

        // 为获取的列表增加用户信息
        return addUserInfo(ranked.size() > num ? ranked.subList(0, num) : ranked);
    }

    /**
     * 播放增量缓存里存的是「上一次落库以来」的播放数，正是当下最活跃的信号，
     * 取增量最多的一批稿件作为第二路候选
     */
    private List<ArticleEntity> recentSurgeCandidates(int limit, int videoType) {
        Map<Long, Long> pendingPlays = playCountRecorder.getPlayMap();
        if (pendingPlays.isEmpty() || pendingPlays.size() > MAX_PENDING_PLAY_KEYS) {
            return List.of();
        }
        List<Long> hottestIds = pendingPlays.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
        if (hottestIds.isEmpty()) {
            return List.of();
        }
        List<ArticleEntity> surge = new ArrayList<>(hottestIds.size());
        for (ArticleEntity video : this.listByIds(hottestIds)) {
            if (isPublicVideo(video, videoType)) {
                surge.add(video);
            }
        }
        return surge;
    }

    /**
     * 叠加缓存计数增量后的实时热度分，口径与 SQL 里的衰减公式一致
     */
    private double liveTrendScore(ArticleEntity article, long now) {
        Long id = article.getId();
        return VideoRanking.trendScore(article, new VideoRanking.PendingCounts(
                playCountRecorder.getPlayCount(id),
                countRecorder.getArticleLikeCount(id),
                countRecorder.getArticleFavoriteCount(id),
                countRecorder.getArticleCommentCount(id),
                countRecorder.getDanmakuCount(id),
                countRecorder.getArticleDislikeCount(id)), now);
    }

    @Override
    public int deleteArticle(ArticleEntity entity, HttpServletRequest request) {
        // 获取文章实体
        ArticleEntity articleToDelete = getById(entity.getId());
        if (articleToDelete == null) {
            return 2; // 不存在
        }

        // 检查权限
        if (hasDeletePermission(articleToDelete, request)) {
            return deleteArticleInDatabase(articleToDelete);
        } else {
            return 1; // 没权限
        }
    }

    /**
     * 检查是否有删除权限
     */
    private boolean hasDeletePermission(ArticleEntity article, HttpServletRequest request) {
        // 管理员可以删除所有文章，普通用户只能删除自己的
        return currentUserResolver.isOwnerOrAdmin(article.getUserId(), request);
    }

    /**
     * 在数据库中标记文章为删除状态
     */
    public int deleteArticleInDatabase(ArticleEntity article) {
        // 将状态设置为删除
        article.setStatus(ArticleStatusEnum.DELETE.getCode());

        // 更新相关文件状态
        updateAssociatedFilesStatus(article.getId(), FileStatusEnum.DELETE.getCode());

        // 更新文章状态
        this.updateById(article);

        // 删除成功
        return 0;
    }

    /**
     * 更新与文章关联的所有文件状态
     */
    private void updateAssociatedFilesStatus(Long articleId, Integer status) {
        List<FileTableEntity> files = fileTableService.findArticle(articleId);

        if (files != null && !files.isEmpty()) {
            files.forEach(file -> file.setStatus(status));
            fileTableService.updateBatchById(files);
        }
    }

    @Override
    public Boolean hasThisVideoPlayPower(FileTableEntity file, Long userId, HttpServletRequest request) {
        // 记录播放历史并更新播放计数
        playRecordingService.saveHistory(file, userId, ipUtil.getUa(request));

        return true;
    }

    @Override
    public PageUtils nowCategory(Map<String, Object> params, Integer id) {
        // 检查分类是否存在
        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(id);
        if (categoryEntity == null) {
            return new PageUtils(null);
        }

        // 构建查询条件
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());

        // 根据父分类或子分类筛选
        applyCategoryFilter(wrapper, categoryEntity);

        String type = (String) params.get("type");
        if (type != null) {
            wrapper.eq("type", type);
        }

        // 使用排序和分页优化数据库查询
        wrapper.orderByDesc("create_time");

        // 执行分页查询
        IPage<ArticleEntity> page = this.page(
                new Query<ArticleEntity>().getPage(params),
                wrapper
        );

        // 处理查询结果，添加用户信息
        if (page.getRecords().isEmpty()) {
            return new PageUtils(page);
        }

        return addUserInfo(page);
    }

    /**
     * 应用分类过滤条件
     */
    private void applyCategoryFilter(QueryWrapper<ArticleEntity> wrapper, CategoryEntity categoryEntity) {
        if (categoryEntity.getFatherId() == 0) {
            // 获取所有子分类ID
            CategoryEntity categoryWithChildren = categoryCache.getCategoryMapWithChildren().get(categoryEntity.getId());
            if (categoryWithChildren != null && categoryWithChildren.getChildren() != null && !categoryWithChildren.getChildren().isEmpty()) {
                // 使用流操作直接收集所有子分类ID
                List<Integer> childCategoryIds = categoryWithChildren.getChildren().stream()
                        .map(CategoryEntity::getId)
                        .collect(Collectors.toList());
                wrapper.in("category", childCategoryIds);
            } else {
                // 如果没有子分类，直接使用当前分类ID
                wrapper.eq("category", categoryEntity.getId());
            }
        } else {
            // 如果是子分类，直接使用当前分类ID
            wrapper.eq("category", categoryEntity.getId());
        }
    }

    @Override
    public ArticleViewData getEditInfo(Long id, HttpServletRequest request) {
        Claims user = JwtUtil.getUser(request);
        ArticleViewData articleViewData = getVideo(id, request);

        // 检查权限
        if (articleViewData != null && hasEditPermission(articleViewData, user)) {
            return articleViewData;
        }

        return null;
    }

    /**
     * 检查是否有编辑权限
     */
    private boolean hasEditPermission(ArticleViewData article, Claims user) {
        Long userId = Long.parseLong(user.getId());
        String role = (String) user.get(WebConstant.ROLE_KEY);

        return article.getUserId().equals(userId) || RoleTypeEnum.ADMIN.getRole().equals(role);
    }

    @Override
    public boolean restore(ArticleEntity articleEntity, HttpServletRequest request) {
        ArticleEntity article = getById(articleEntity.getId());
        if (article == null) {
            return false;
        }

        // 更新文章状态
        article.setStatus(ArticleStatusEnum.NORMAL.getCode());
        this.updateById(article);

        // 更新关联文件状态
        updateAssociatedFilesStatus(article.getId(), FileStatusEnum.USED.getCode());

        return true;
    }


    @Override
    public void addCount(String col, Long articleId, long count) {
        // col 会被拼进 update 语句，只允许计数列白名单
        this.baseMapper.addCount(SQLFilter.checkedCountColumn(col), articleId, count);
    }

    @Override
    public List<ArticleViewData> getRecommendationsByArticleId(Long articleId, int limit) {
        if (articleId == null || articleId <= 0) {
            return new ArrayList<>();
        }

        ArticleEntity article = this.getById(articleId);
        if (article == null) {
            return new ArrayList<>();
        }
        return recommend(ArticleEntityFactory.parseTag(article.getTag()), article.getCategory(), articleId, limit);
    }

    @Override
    public List<ArticleViewData> getRecommendationsByTags(List<String> tags, Long excludeArticleId, int limit) {
        return recommend(tags, null, excludeArticleId, limit);
    }

    /**
     * 相关推荐：先在内存里的标签倒排索引召回并打分，请求路径不扫 article 表；
     * 候选不足时用热门榜补位，补位条目带 sort 热度分，前端据此区分「相似」与「热门补位」
     *
     * @param sourceCategory 源稿件分区，为 null 时跳过同分区加分这一路信号
     */
    private List<ArticleViewData> recommend(List<String> tags, Integer sourceCategory,
                                            Long excludeArticleId, int limit) {
        if (limit <= 0) {
            return new ArrayList<>();
        }
        Set<Long> picked = new HashSet<>();
        if (excludeArticleId != null) {
            picked.add(excludeArticleId);
        }

        List<ArticleViewData> result = new ArrayList<>(limit);
        List<String> sourceTags = tags == null ? List.of() : tags;
        if (!sourceTags.isEmpty()) {
            result.addAll(loadRankedVideos(
                    videoCatalogCache.recommend(sourceTags, sourceCategory, excludeArticleId, limit), picked));

            if (result.size() < limit && !videoCatalogCache.isReady()) {
                // 只在进程刚启动、索引还没建好时走这条扫表路径
                result.addAll(loadVideos(baseMapper.findSimilarArticlesByTags(
                        buildTagLikePatterns(sourceTags), excludeArticleId,
                        limit - result.size(), FileTypeEnum.VIDEO.getCode()), picked));
            }
        }

        if (result.size() < limit) {
            result.addAll(hotPadding(hotCache.getHotList(), limit - result.size(), picked));
        }
        return result;
    }

    /**
     * 按召回顺序取回展示字段，一次主键批量查询
     */
    private List<ArticleViewData> loadRankedVideos(List<Long> rankedIds, Set<Long> picked) {
        if (rankedIds.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, ArticleEntity> videosById = new HashMap<>(rankedIds.size() * 2);
        for (ArticleEntity video : this.listByIds(rankedIds)) {
            videosById.put(video.getId(), video);
        }
        List<ArticleEntity> ordered = new ArrayList<>(rankedIds.size());
        for (Long id : rankedIds) {
            ArticleEntity video = videosById.get(id);
            if (video != null && isPublicVideo(video, FileTypeEnum.VIDEO.getCode()) && picked.add(id)) {
                ordered.add(video);
            }
        }
        return addUserInfo(ordered);
    }

    private List<ArticleViewData> loadVideos(List<ArticleEntity> videos, Set<Long> picked) {
        List<ArticleEntity> kept = new ArrayList<>(videos.size());
        for (ArticleEntity video : videos) {
            if (isPublicVideo(video, FileTypeEnum.VIDEO.getCode()) && picked.add(video.getId())) {
                kept.add(video);
            }
        }
        return addUserInfo(kept);
    }

    /**
     * 热门榜补位，条目本身的 sort 就是热度分
     */
    private List<ArticleViewData> hotPadding(List<ArticleViewData> hotList, int need, Set<Long> picked) {
        List<ArticleViewData> padding = new ArrayList<>(need);
        for (ArticleViewData hot : hotList) {
            if (padding.size() >= need) {
                break;
            }
            if (hot.getId() != null && picked.add(hot.getId())) {
                padding.add(hot);
            }
        }
        return padding;
    }

    /**
     * 标签在库里存成 {@code ["a","b"]} 形式的 JSON 数组，匹配模式带上引号，
     * 这样「小猫」不会命中「猫」；转义 LIKE 的通配符，避免标签里的 % 把条件变成全匹配
     */
    private List<String> buildTagLikePatterns(List<String> tags) {
        List<String> patterns = new ArrayList<>(tags.size());
        for (String tag : tags) {
            if (!StringUtils.hasText(tag)) {
                continue;
            }
            String escaped = tag.trim()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            patterns.add("%\"" + escaped + "\"%");
        }
        return patterns;
    }

    /**
     * 是否能出现在公开列表里
     */
    private boolean isPublicVideo(ArticleEntity article, int videoType) {
        return article != null
                && article.getType() != null && article.getType() == videoType
                && Objects.equals(article.getStatus(), ArticleStatusEnum.NORMAL.getCode())
                && Objects.equals(article.getExamineStatus(), ExamineTypeEnum.SUCCESS.getCode());
    }

    /**
     * 为文章添加分类信息（不含作者信息）
     */
    public List<ArticleViewData> addArticleCategory(IPage<ArticleEntity> page) {
        return articleViewEnricher.enrichPageWithCategory(page);
    }
}