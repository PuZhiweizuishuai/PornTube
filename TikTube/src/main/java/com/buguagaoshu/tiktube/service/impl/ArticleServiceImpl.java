package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.cache.*;
import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.dto.ExamineDto;
import com.buguagaoshu.tiktube.dto.VideoArticleDto;
import com.buguagaoshu.tiktube.entity.*;
import com.buguagaoshu.tiktube.enums.*;
import com.buguagaoshu.tiktube.service.*;
import com.buguagaoshu.tiktube.support.ArticleEntityFactory;
import com.buguagaoshu.tiktube.support.ArticleRecommender;
import com.buguagaoshu.tiktube.support.ArticleViewEnricher;
import com.buguagaoshu.tiktube.support.ArticleVisibilityResolver;
import com.buguagaoshu.tiktube.support.CurrentUser;
import com.buguagaoshu.tiktube.support.CurrentUserResolver;
import com.buguagaoshu.tiktube.support.TrendingVideoRanker;
import com.buguagaoshu.tiktube.utils.*;
import com.buguagaoshu.tiktube.vo.ArticleViewData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;

import com.buguagaoshu.tiktube.dao.ArticleDao;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 文章服务实现类
 *
 * @author Pu Zhiwei
 */
@Slf4j
@Service("articleService")
public class ArticleServiceImpl extends ServiceImpl<ArticleDao, ArticleEntity> implements ArticleService {

    /**
     * {@link #deleteArticle} 的返回码，前端按这三个数判断结果，不要改数值
     */
    private static final int DELETE_SUCCESS = 0;
    private static final int DELETE_NO_POWER = 1;
    private static final int DELETE_NOT_FOUND = 2;

    /**
     * 个人主页列表里 type = -1 表示不限稿件类型
     */
    private static final int ALL_TYPES = -1;

    /**
     * 搜索词长度上限，超长关键词对 LIKE 没有任何收益
     */
    private static final int MAX_SEARCH_LENGTH = 100;

    private final CategoryCache categoryCache;
    private final FileTableService fileTableService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final PlayRecordingService playRecordingService;
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

    /** 热门榜计算，定时任务与管理员手动刷新都走它 */
    private final TrendingVideoRanker trendingVideoRanker;

    /** 相关推荐，召回与打分在内存标签索引里完成 */
    private final ArticleRecommender articleRecommender;

    @Autowired
    public ArticleServiceImpl(CategoryCache categoryCache,
                              FileTableService fileTableService,
                              UserService userService,
                              NotificationService notificationService,
                              PlayRecordingService playRecordingService,
                              PlayCountRecorder playCountRecorder,
                              CountRecorder countRecorder,
                              IpUtil ipUtil,
                              ArticleVisibilityResolver visibilityResolver,
                              CurrentUserResolver currentUserResolver,
                              ArticleEntityFactory articleEntityFactory,
                              ArticleViewEnricher articleViewEnricher,
                              TrendingVideoRanker trendingVideoRanker,
                              ArticleRecommender articleRecommender) {
        this.categoryCache = categoryCache;
        this.fileTableService = fileTableService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.playRecordingService = playRecordingService;
        this.playCountRecorder = playCountRecorder;
        this.countRecorder = countRecorder;
        this.ipUtil = ipUtil;
        this.visibilityResolver = visibilityResolver;
        this.currentUserResolver = currentUserResolver;
        this.articleEntityFactory = articleEntityFactory;
        this.articleViewEnricher = articleViewEnricher;
        this.trendingVideoRanker = trendingVideoRanker;
        this.articleRecommender = articleRecommender;
    }

    // ==================== 列表查询 ====================

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<ArticleEntity> wrapper = createNormalArticleWrapper();

        Integer type = parseType(params.get("type"));
        if (type != null) {
            wrapper.eq("type", type);
        }

        String search = boundedText(params.get("search"), MAX_SEARCH_LENGTH);
        if (search != null) {
            // 不转义通配符的话，用户传一个 % 就等于无条件全表匹配
            wrapper.like("title", SQLFilter.escapeLike(search));
        }

        return addUserInfo(this.page(newPage(params), wrapper));
    }

    /**
     * 个人主页稿件列表
     */
    @Override
    public PageUtils userArticleList(Map<String, Object> params, Long id, Integer type) {
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", id);
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());

        int articleType = normalizeType(type);
        if (articleType != ALL_TYPES) {
            wrapper.eq("type", articleType);
        }
        wrapper.orderByDesc("create_time");

        IPage<ArticleEntity> page = this.page(newPage(params), wrapper);
        return new PageUtils(addArticleCategory(page), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public PageUtils followUserArticleList(Map<String, Object> params, Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            // 空集合会拼出 IN ()，MySQL 直接语法报错
            return emptyPage();
        }
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());
        wrapper.in("user_id", userIds);
        wrapper.orderByDesc("update_time");

        return addUserInfo(this.page(newPage(params), wrapper));
    }

    /**
     * 创作者中心 / 管理员后台的稿件列表
     */
    @Override
    public PageUtils userArticleList(Map<String, Object> params, String type, HttpServletRequest request) {
        // 只解析一次 JWT：原先 getUserId 与 isAdmin 各自解析一遍
        CurrentUser currentUser = currentUserResolver.resolve(request);

        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        if ("admin".equals(type) && currentUser.isAdmin()) {
            // 管理员看全库，只有显式要回收站时才加状态过滤
            if ("delete".equals(params.get("active"))) {
                wrapper.eq("status", ArticleStatusEnum.DELETE.getCode());
            }
        } else {
            wrapper.eq("user_id", currentUser.getUserId());
            wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        }
        wrapper.orderByDesc("create_time");

        return new PageUtils(this.page(newPage(params), wrapper));
    }

    @Override
    public PageUtils examineList(Map<String, Object> params, HttpServletRequest request) {
        if (!currentUserResolver.isAdmin(request)) {
            return null;
        }
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("examine_status", ExamineTypeEnum.PENDING_REVIEW.getCode());
        wrapper.orderByDesc("create_time");

        IPage<ArticleEntity> page = this.page(newPage(params), wrapper);
        return new PageUtils(addArticleCategory(page), page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public PageUtils nowCategory(Map<String, Object> params, Integer id) {
        CategoryEntity categoryEntity = id == null ? null : categoryCache.getCategoryEntityMap().get(id);
        if (categoryEntity == null) {
            // 不能返回 null：CategoryServiceImpl 的删除校验会直接读 getList()
            return emptyPage();
        }

        QueryWrapper<ArticleEntity> wrapper = createNormalArticleWrapper();
        applyCategoryFilter(wrapper, categoryEntity);

        Integer type = parseType(params.get("type"));
        if (type != null) {
            wrapper.eq("type", type);
        }

        return addUserInfo(this.page(newPage(params), wrapper));
    }

    /**
     * 创建「已通过审核 + 未删除 + 按发布时间倒序」的查询条件
     */
    private QueryWrapper<ArticleEntity> createNormalArticleWrapper() {
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());
        wrapper.orderByDesc("create_time");
        return wrapper;
    }

    /**
     * 应用分类过滤条件：父分类连带它的子分类一起查
     */
    private void applyCategoryFilter(QueryWrapper<ArticleEntity> wrapper, CategoryEntity categoryEntity) {
        Integer fatherId = categoryEntity.getFatherId();
        boolean isFatherCategory = fatherId != null && fatherId == 0;
        if (!isFatherCategory) {
            wrapper.eq("category", categoryEntity.getId());
            return;
        }

        CategoryEntity withChildren = categoryCache.getCategoryMapWithChildren().get(categoryEntity.getId());
        List<Integer> categories = new ArrayList<>();
        // 子分区还没建出来时前端会把稿件直接挂在父分区上，父分区自己的 ID 也要一起查
        categories.add(categoryEntity.getId());
        if (withChildren != null && withChildren.getChildren() != null) {
            withChildren.getChildren().forEach(child -> categories.add(child.getId()));
        }
        wrapper.in("category", categories);
    }

    /**
     * 空分页。list 必须是非 null 的空列表：调用方（如 CategoryServiceImpl 的删除校验）
     * 会直接读 {@code getList().isEmpty()}
     */
    private static PageUtils emptyPage() {
        return new PageUtils(List.of(), 0, 0, 0);
    }

    private static IPage<ArticleEntity> newPage(Map<String, Object> params) {
        return new Query<ArticleEntity>().getPage(params);
    }

    /**
     * type 列是 int，直接 eq 一个字符串会让 MySQL 做隐式转换（{@code 'abc' = 0} 成立），
     * 既可能把全部视频都匹配上，也用不上索引，所以先解析成已知的类型码
     *
     * @return 合法的稿件类型码，缺失或非法时返回 null 表示不加这个条件
     */
    private static Integer parseType(Object raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        int type;
        try {
            type = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
        return isKnownArticleType(type) ? type : null;
    }

    /**
     * 个人主页的类型参数：-1 不限类型，null 或非法值按视频处理
     */
    private static int normalizeType(Integer type) {
        if (type != null && type == ALL_TYPES) {
            return ALL_TYPES;
        }
        return type != null && isKnownArticleType(type) ? type : FileTypeEnum.VIDEO.getCode();
    }

    private static boolean isKnownArticleType(int type) {
        return type == FileTypeEnum.VIDEO.getCode()
                || type == FileTypeEnum.PHOTO.getCode()
                || type == FileTypeEnum.ARTICLE.getCode();
    }

    /**
     * 去掉首尾空白并按上限截断，空串按「没有这个参数」处理
     */
    private static String boundedText(Object raw, int maxLength) {
        if (raw == null) {
            return null;
        }
        String text = raw.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    // ==================== 列表富化 ====================

    /**
     * 为稿件补作者信息、分类与缓存播放量，具体实现见 {@link ArticleViewEnricher}
     */
    private PageUtils addUserInfo(IPage<ArticleEntity> page) {
        return articleViewEnricher.enrichPage(page);
    }

    private List<ArticleViewData> addUserInfo(List<ArticleEntity> articles) {
        return articleViewEnricher.enrich(articles);
    }

    /**
     * 只补分类信息，不带作者（后台列表用）
     */
    private List<ArticleViewData> addArticleCategory(IPage<ArticleEntity> page) {
        return articleViewEnricher.enrichPageWithCategory(page);
    }

    // ==================== 投稿 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum saveVideo(VideoArticleDto videoArticleDto, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);

        if (!isKnownCategory(videoArticleDto.getCategory())) {
            return ReturnCodeEnum.CATEGORY_NOT_FOUND;
        }

        FileCheck files = checkUploadFiles(videoArticleDto, userId, null);
        if (!files.ok()) {
            return files.code();
        }

        ArticleEntity article = articleEntityFactory.create(videoArticleDto, userId, files.image().getFileUrl());
        applyVideoFields(article, files.video());
        applyRequestTrace(article, request);
        this.save(article);

        userService.updateLastPublishTime(System.currentTimeMillis(), userId);
        // 新稿件还没有关联文件，同一事务里把封面与视频挂上去
        applyFileAssociation(article.getId(), files.image(), files.video(), List.of());
        return ReturnCodeEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum updateVideo(VideoArticleDto videoArticleDto, HttpServletRequest request) {
        Long articleId = videoArticleDto.getId();
        if (articleId == null) {
            return ReturnCodeEnum.LACK_ID;
        }
        if (!isKnownCategory(videoArticleDto.getCategory())) {
            return ReturnCodeEnum.CATEGORY_NOT_FOUND;
        }

        // 只读一次稿件：原先先 getEditInfo（内部 getVideo 会建视图、算 AES key、查作者）再 getById，
        // 同一行读两遍，还多跑一次 findArticle
        CurrentUser currentUser = currentUserResolver.resolve(request);
        ArticleEntity article = this.getOne(
                visibilityResolver.normalArticleWrapper(articleId, FileTypeEnum.VIDEO.getCode()));
        if (article == null) {
            return ReturnCodeEnum.NOO_FOUND;
        }
        if (!currentUser.isOwnerOrAdmin(article.getUserId())) {
            log.warn("用户 {} 试图修改不属于自己的稿件 {}", currentUser.getUserId(), articleId);
            return ReturnCodeEnum.NO_POWER;
        }

        FileCheck files = checkUploadFiles(videoArticleDto, currentUser.getUserId(), articleId);
        if (!files.ok()) {
            return files.code();
        }

        articleEntityFactory.applyUpdate(article, videoArticleDto, files.image().getFileUrl());
        applyVideoFields(article, files.video());
        applyRequestTrace(article, request);
        this.updateById(article);

        applyFileAssociation(articleId, files.image(), files.video(), fileTableService.findArticle(articleId));
        return ReturnCodeEnum.SUCCESS;
    }

    /**
     * 投稿表单里指定的封面与视频文件，校验不通过时 image / video 为 null
     */
    private record FileCheck(ReturnCodeEnum code, FileTableEntity image, FileTableEntity video) {
        boolean ok() {
            return code == ReturnCodeEnum.SUCCESS;
        }
    }

    /**
     * 校验并取回投稿使用的封面与视频文件
     *
     * @param ownerId          文件必须是他上传的
     * @param currentArticleId 更新场景下当前稿件 ID，本稿件已占用的文件可以继续用；新增场景传 null
     */
    private FileCheck checkUploadFiles(VideoArticleDto dto, long ownerId, Long currentArticleId) {
        Long imageId = dto.getImageId();
        Long videoId = dto.getVideo() == null ? null : dto.getVideo().getId();
        if (imageId == null || videoId == null || imageId.equals(videoId)) {
            return new FileCheck(ReturnCodeEnum.DATA_VALID_EXCEPTION, null, null);
        }

        // 一次批量查询取回两个文件，只保留自己上传的
        Map<Long, FileTableEntity> owned = new HashMap<>(4);
        for (FileTableEntity file : fileTableService.listByIds(List.of(imageId, videoId))) {
            if (file.getUploadUserId() != null && file.getUploadUserId() == ownerId) {
                owned.put(file.getId(), file);
            }
        }
        FileTableEntity image = owned.get(imageId);
        FileTableEntity video = owned.get(videoId);
        if (image == null || video == null) {
            log.warn("用户 {} 投稿时引用了不属于自己的文件：imageId={}, videoId={}", ownerId, imageId, videoId);
            return new FileCheck(ReturnCodeEnum.NO_POWER, null, null);
        }
        // 不能只信前端说「这个是封面、这个是视频」，文件类型必须对得上
        if (!isCoverFile(image)) {
            return new FileCheck(ReturnCodeEnum.IMAGE_NO_POWER, null, null);
        }
        if (!isVideoFile(video)) {
            return new FileCheck(ReturnCodeEnum.VIDEO_NO_POWER, null, null);
        }
        if (!isAttachable(image, currentArticleId) || !isAttachable(video, currentArticleId)) {
            log.warn("用户 {} 投稿时引用了已被占用的文件：imageId={}, videoId={}", ownerId, imageId, videoId);
            return new FileCheck(ReturnCodeEnum.VIDEO_NO_POWER, null, null);
        }
        return new FileCheck(ReturnCodeEnum.SUCCESS, image, video);
    }

    private boolean isKnownCategory(Integer category) {
        return category != null && categoryCache.getCategoryEntityMap().containsKey(category);
    }

    private static boolean isCoverFile(FileTableEntity file) {
        Integer type = file.getType();
        return type != null
                && (type == FileTypeEnum.PHOTO.getCode() || type == FileTypeEnum.VIDEO_PHOTO.getCode());
    }

    private static boolean isVideoFile(FileTableEntity file) {
        Integer type = file.getType();
        return type != null && type == FileTypeEnum.VIDEO.getCode();
    }

    /**
     * 文件要么还没被任何稿件占用，要么就是本稿件正在用的。
     * 否则一个已发布的文件可以被反复挂到新稿件上，把原稿件的播放地址挖空
     */
    private static boolean isAttachable(FileTableEntity file, Long currentArticleId) {
        if (Objects.equals(file.getStatus(), FileStatusEnum.NOT_USE_FILE.getCode())) {
            return true;
        }
        return currentArticleId != null && Objects.equals(file.getArticleId(), currentArticleId);
    }

    private static void applyVideoFields(ArticleEntity article, FileTableEntity videoFile) {
        article.setType(FileTypeEnum.VIDEO.getCode());
        article.setDuration(videoFile.getDuration());
        article.setPixelsNumber(videoFile.getPixelsNumber());
        article.setFrameRate(videoFile.getFrameRate());
    }

    /** 记录本次操作的来源 IP / UA / 归属地 */
    private void applyRequestTrace(ArticleEntity article, HttpServletRequest request) {
        String ip = ipUtil.getIpAddr(request);
        article.setIp(ip);
        article.setUa(ipUtil.getUa(request));
        article.setCity(ipUtil.getCity(ip));
    }

    /**
     * 把文件关联调整成本次投稿后的样子：新封面与新视频置为已用并绑定到稿件，
     * 其余原本挂在稿件上的文件释放回未使用，交给清理任务回收。
     *
     * @param currentFiles 稿件当前已使用的文件，{@link FileTableService#findArticle} 只返回 status = USED 的行
     */
    private void applyFileAssociation(Long articleId, FileTableEntity imageFile, FileTableEntity videoFile,
                                      List<FileTableEntity> currentFiles) {
        Map<Long, FileTableEntity> updates = new LinkedHashMap<>();
        markUsed(updates, imageFile, articleId);
        markUsed(updates, videoFile, articleId);

        for (FileTableEntity file : currentFiles) {
            if (updates.containsKey(file.getId())) {
                continue;
            }
            file.setArticleId(articleId);
            file.setStatus(FileStatusEnum.NOT_USE_FILE.getCode());
            updates.put(file.getId(), file);
        }

        if (!updates.isEmpty()) {
            fileTableService.updateBatchById(new ArrayList<>(updates.values()));
        }
    }

    private static void markUsed(Map<Long, FileTableEntity> updates, FileTableEntity file, Long articleId) {
        file.setArticleId(articleId);
        file.setStatus(FileStatusEnum.USED.getCode());
        updates.put(file.getId(), file);
    }

    // ==================== 详情与编辑 ====================

    @Override
    public ArticleViewData getVideo(long id, HttpServletRequest request) {
        return getVideo(id, currentUserResolver.resolve(request));
    }

    /**
     * @param currentUser 一次解析出用户 ID 与角色，未登录为匿名用户（userId = -1）
     */
    private ArticleViewData getVideo(long id, CurrentUser currentUser) {
        ArticleEntity articleEntity = this.getOne(
                visibilityResolver.normalArticleWrapper(id, FileTypeEnum.VIDEO.getCode()));
        // 稿件不存在，或未通过审核且不是作者本人/管理员 → 返回隐藏视图
        if (!visibilityResolver.isVisible(articleEntity, currentUser)) {
            return visibilityResolver.hiddenView();
        }
        return buildArticleViewData(articleEntity, currentUser.getUserId());
    }

    /**
     * 构建稿件详情视图
     */
    private ArticleViewData buildArticleViewData(ArticleEntity article, long viewerId) {
        long articleId = article.getId();
        ArticleCountSnapshot counts = ArticleCountSnapshot.load(
                countRecorder, playCountRecorder, List.of(articleId));
        counts.applyTo(article);

        ArticleViewData viewData = new ArticleViewData();
        BeanUtils.copyProperties(article, viewData);

        articleViewEnricher.applyAuthor(viewData, article.getUserId());
        articleViewEnricher.applyCategory(viewData, article.getCategory());
        viewData.setTag(ArticleEntityFactory.parseTag(article.getTag()));

        List<FileTableEntity> files = fileTableService.findArticle(articleId);
        long expireAt = System.currentTimeMillis() + WebConstant.KEY_EXPIRY_DATE;
        AesUtil aesUtil = AesUtil.getInstance();
        for (FileTableEntity file : files) {
            file.setKey(aesUtil.encrypt(
                    viewerId + "#" + file.getId() + "#" + expireAt + "#" + file.getFileNewName()));
            if (isCoverFile(file)) {
                viewData.setImageId(file.getId());
            }
        }
        viewData.setVideo(files);

        viewData.setIsShow(true);
        viewData.setViewCount(viewData.getViewCount() + counts.playCount(articleId));
        return viewData;
    }

    @Override
    public ArticleViewData getEditInfo(Long id, HttpServletRequest request) {
        if (id == null) {
            return null;
        }
        CurrentUser currentUser = currentUserResolver.resolve(request);
        if (!currentUser.isLogin()) {
            return null;
        }
        ArticleViewData viewData = getVideo(id, currentUser);
        // 隐藏视图没有作者，直接判所有权会 NPE，先按 isShow 挡掉
        if (!Boolean.TRUE.equals(viewData.getIsShow())) {
            return null;
        }
        return currentUser.isOwnerOrAdmin(viewData.getUserId()) ? viewData : null;
    }

    // ==================== 审核 / 删除 / 恢复 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum examine(ExamineDto examineDto, HttpServletRequest request) {
        if (examineDto == null || examineDto.getVideoId() == null
                || examineDto.getResult() == null || examineDto.getType() == null) {
            return ReturnCodeEnum.DATA_VALID_EXCEPTION;
        }

        // 改的是别人稿件的审核状态，不能只依赖拦截器
        CurrentUser examiner = currentUserResolver.resolve(request);
        if (!examiner.isAdmin()) {
            log.warn("非管理员 {} 尝试审核稿件 {}", examiner.getUserId(), examineDto.getVideoId());
            return ReturnCodeEnum.NO_POWER;
        }

        boolean approved = examineDto.getResult();
        // 驳回必须给出真正的驳回理由，否则可以用 type = 1 把稿件改成「通过」而绕过投稿计数，
        // 或者用 type = 0 把稿件悄悄退回待审
        if (!approved && !isRejectType(examineDto.getType())) {
            return ReturnCodeEnum.DATA_VALID_EXCEPTION;
        }

        ArticleEntity article = this.getById(examineDto.getVideoId());
        if (article == null) {
            return ReturnCodeEnum.NOO_FOUND;
        }
        if (Objects.equals(article.getExamineStatus(), ExamineTypeEnum.SUCCESS.getCode())) {
            return ReturnCodeEnum.REVIEWED;
        }

        long examinerId = examiner.getUserId();
        if (approved) {
            article.setExamineStatus(ExamineTypeEnum.SUCCESS.getCode());
            // 只有审核通过才增加投稿数量
            userService.addSubmitCount(article.getUserId(), 1);
            notificationService.sendNotification(
                    examinerId,
                    article.getUserId(),
                    article.getId(),
                    article.getId(),
                    -1,
                    "你的稿件已通过审核，现在所有人都能看见你的稿件了",
                    NotificationType.createExamineLink(article.getTitle(), article.getId()),
                    NotificationType.createExaminePassContent(article.getTitle()),
                    NotificationType.SYSTEM
            );
        } else {
            article.setExamineStatus(examineDto.getType());
            notificationService.sendNotification(
                    examinerId,
                    article.getUserId(),
                    article.getId(),
                    article.getId(),
                    -1,
                    "稿件未通过审核",
                    NotificationType.createExamineLink(article.getTitle(), article.getId()),
                    NotificationType.createExamineContent(article.getTitle(), examineDto.getType(), examineDto.getMessage()),
                    NotificationType.SYSTEM
            );
        }

        article.setUpdateTime(System.currentTimeMillis());
        article.setExamineUser(examinerId);
        article.setExamineMessage(examineDto.getMessage());
        this.updateById(article);
        return ReturnCodeEnum.SUCCESS;
    }

    /**
     * 是否是真正的驳回理由（待审核与审核通过都不算）
     */
    private static boolean isRejectType(int type) {
        return type != ExamineTypeEnum.PENDING_REVIEW.getCode() && type != ExamineTypeEnum.SUCCESS.getCode();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteArticle(ArticleEntity entity, HttpServletRequest request) {
        if (entity == null || entity.getId() == null) {
            return DELETE_NOT_FOUND;
        }
        ArticleEntity article = this.getById(entity.getId());
        if (article == null) {
            return DELETE_NOT_FOUND;
        }
        // 管理员可以删除所有稿件，普通用户只能删除自己的
        CurrentUser currentUser = currentUserResolver.resolve(request);
        if (!currentUser.isOwnerOrAdmin(article.getUserId())) {
            log.warn("用户 {} 试图删除不属于自己的稿件 {}", currentUser.getUserId(), article.getId());
            return DELETE_NO_POWER;
        }

        article.setStatus(ArticleStatusEnum.DELETE.getCode());
        this.updateById(article);
        markAssociatedFilesDeleted(article.getId());
        return DELETE_SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restore(ArticleEntity articleEntity, HttpServletRequest request) {
        if (articleEntity == null || articleEntity.getId() == null) {
            return false;
        }
        // 恢复的是管理员删掉的稿件，不能只依赖拦截器
        CurrentUser currentUser = currentUserResolver.resolve(request);
        if (!currentUser.isAdmin()) {
            log.warn("非管理员 {} 尝试恢复稿件 {}", currentUser.getUserId(), articleEntity.getId());
            return false;
        }
        ArticleEntity article = this.getById(articleEntity.getId());
        if (article == null) {
            return false;
        }

        article.setStatus(ArticleStatusEnum.NORMAL.getCode());
        this.updateById(article);
        restoreAssociatedFiles(article.getId());
        return true;
    }

    /**
     * 把稿件当前已使用的文件一起标记为删除
     */
    private void markAssociatedFilesDeleted(Long articleId) {
        List<FileTableEntity> files = fileTableService.findArticle(articleId);
        if (files.isEmpty()) {
            return;
        }
        files.forEach(file -> file.setStatus(FileStatusEnum.DELETE.getCode()));
        fileTableService.updateBatchById(files);
    }

    /**
     * 把随稿件一起标记为删除的文件翻回已使用。
     * 不能用 {@link FileTableService#findArticle}：它只查 status = USED 的文件，
     * 而删除后这些文件已经是 DELETE 态，查出来永远是空的，恢复出来的稿件会带着一堆已删除文件
     */
    private void restoreAssociatedFiles(Long articleId) {
        QueryWrapper<FileTableEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);
        wrapper.eq("status", FileStatusEnum.DELETE.getCode());
        List<FileTableEntity> files = fileTableService.list(wrapper);
        if (files.isEmpty()) {
            return;
        }
        files.forEach(file -> file.setStatus(FileStatusEnum.USED.getCode()));
        fileTableService.updateBatchById(files);
    }

    // ==================== 播放 ====================

    @Override
    public boolean hasThisVideoPlayPower(FileTableEntity file, Long userId, HttpServletRequest request) {
        // 只记录播放历史，播放量由 /api/article/playrecording/view/{id} 走 PlayCountRecorder 计数
        playRecordingService.saveHistory(file, userId, ipUtil.getUa(request), null);
        return true;
    }

    @Override
    public void addDanmakuCount(Long id, long count) {
        this.baseMapper.addDanmakuCount(id, count);
    }

    @Override
    public void addCount(String col, Long articleId, long count) {
        // col 会被拼进 update 语句，只允许计数列白名单
        this.baseMapper.addCount(SQLFilter.checkedCountColumn(col), articleId, count);
    }

    // ==================== 热门榜与相关推荐 ====================

    @Override
    public List<ArticleViewData> hotView(int num) {
        return trendingVideoRanker.rank(num);
    }

    @Override
    public List<ArticleViewData> getRecommendationsByArticleId(Long articleId, int limit) {
        return articleRecommender.byArticleId(articleId, limit);
    }
}
