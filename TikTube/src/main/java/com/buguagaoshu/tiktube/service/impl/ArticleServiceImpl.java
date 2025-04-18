package com.buguagaoshu.tiktube.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.buguagaoshu.tiktube.cache.CategoryCache;
import com.buguagaoshu.tiktube.cache.WebSettingCache;
import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.dto.ExamineDto;
import com.buguagaoshu.tiktube.dto.VideoArticleDto;
import com.buguagaoshu.tiktube.entity.*;
import com.buguagaoshu.tiktube.enums.*;
import com.buguagaoshu.tiktube.exception.UserNotLoginException;
import com.buguagaoshu.tiktube.service.*;
import com.buguagaoshu.tiktube.utils.*;
import com.buguagaoshu.tiktube.vo.ArticleViewData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.buguagaoshu.tiktube.dao.ArticleDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 文章服务实现类
 * @author Pu Zhiwei
 */
@Slf4j
@Service("articleService")
public class ArticleServiceImpl extends ServiceImpl<ArticleDao, ArticleEntity> implements ArticleService {

    private static final String TAG = "ArticleServiceImpl";
    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MAX_DESCRIBE_LENGTH = 200;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CategoryCache categoryCache;
    private final WebSettingCache webSettingCache;
    private final FileTableService fileTableService;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private PlayRecordingService playRecordingService;

    @Autowired
    public ArticleServiceImpl(CategoryCache categoryCache, 
                             WebSettingCache webSettingCache, 
                             FileTableService fileTableService, 
                             UserService userService, 
                             UserRoleService userRoleService) {
        this.categoryCache = categoryCache;
        this.webSettingCache = webSettingCache;
        this.fileTableService = fileTableService;
        this.userService = userService;
        this.userRoleService = userRoleService;
    }

    @Autowired
    public void setPlayRecordingService(PlayRecordingService playRecordingService) {
        this.playRecordingService = playRecordingService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<ArticleEntity> wrapper = createNormalArticleWrapper();
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
     * @param conditions 查询条件键值对
     * @param orderByDesc 是否按创建时间降序
     */
    private QueryWrapper<ArticleEntity> createQueryWrapper(Map<String, Object> conditions, boolean orderByDesc) {
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        conditions.forEach((key, value) -> wrapper.eq(key, value));
        
        if (orderByDesc) {
            wrapper.orderByDesc("create_time");
        }
        
        return wrapper;
    }

    /**
     * 为文章添加用户信息
     */
    public PageUtils addUserInfo(IPage<ArticleEntity> page) {
        List<ArticleViewData> articleViewData = addUserInfo(page.getRecords());
        return new PageUtils(createArticleViewData(articleViewData, page));
    }

    public List<ArticleViewData> addUserInfo(List<ArticleEntity> articleEntityList) {
        if (articleEntityList == null || articleEntityList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 收集所有用户ID
        Set<Long> userIdList = articleEntityList.stream()
            .map(ArticleEntity::getUserId)
            .collect(Collectors.toSet());
            
        if (userIdList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量获取用户信息
        Map<Long, UserEntity> userEntityMap = userService.userMapList(userIdList);
        
        // 构建视图数据
        return articleEntityList.stream().map(a -> {
            ArticleViewData viewData = new ArticleViewData();
            UserEntity userEntity = userEntityMap.get(a.getUserId());
            
            // 拷贝基本属性
            BeanUtils.copyProperties(a, viewData);
            
            // 设置用户信息
            if (userEntity != null) {
                viewData.setUsername(userEntity.getUsername());
                viewData.setAvatarUrl(userEntity.getAvatarUrl());
            }

            // 添加分类信息
            addCategoryInfo(viewData, a.getCategory());
            
            return viewData;
        }).collect(Collectors.toList());
    }

    /**
     * 添加分类信息到视图数据
     */
    private void addCategoryInfo(ArticleViewData viewData, Integer categoryId) {
        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(categoryId);
        if (categoryEntity != null) {
            viewData.setChildrenCategory(categoryEntity);
            if (categoryEntity.getFatherId() != 0) {
                CategoryEntity f = categoryCache.getCategoryEntityMap().get(categoryEntity.getFatherId());
                viewData.setFatherCategory(f);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum saveVideo(VideoArticleDto videoArticleDto, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        
        // 参数验证
        // 单独验证封面ID
        if (videoArticleDto.getImageId() == null) {
            return ReturnCodeEnum.DATA_VALID_EXCEPTION;
        }
        ReturnCodeEnum validationResult = validateVideoArticleDto(videoArticleDto, userId);
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

        // 构建文章实体
        ArticleEntity articleEntity = buildArticleEntity(videoArticleDto, userId, imageFile, videoFile);
        articleEntity.setIp(IpUtil.getIpAddr(request));
        articleEntity.setUa(IpUtil.getUa(request));
        
        // 保存文章
        this.save(articleEntity);
        
        // 更新文件状态
        updateFileStatus(articleEntity.getId(), imageFile, videoFile);
        
        return ReturnCodeEnum.SUCCESS;
    }

    /**
     * 验证视频文章DTO
     */
    private ReturnCodeEnum validateVideoArticleDto(VideoArticleDto dto, long userId) {
        // 标题验证
        if (dto.getTitle().length() > MAX_TITLE_LENGTH) {
            return ReturnCodeEnum.TITLE_TO_LONG;
        }

        // 描述验证
        if (!StringUtils.hasText(dto.getDescribe()) && dto.getDescribe().length() > MAX_DESCRIBE_LENGTH) {
            return ReturnCodeEnum.DESCRIBE_TO_LONG;
        }

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
        ReturnCodeEnum validationResult = validateVideoArticleDto(videoArticleDto, userId);
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
        // 更新基本信息
        article.setTitle(dto.getTitle());
        article.setDescribes(dto.getDescribe());
        article.setCategory(dto.getCategory());
        article.setImgUrl(imageFile.getFileUrl());
        article.setUpdateTime(System.currentTimeMillis());
        article.setUa(IpUtil.getUa(request));
        article.setIp(IpUtil.getIpAddr(request));
        
        // 设置标签
        try {
            article.setTag(OBJECT_MAPPER.writeValueAsString(dto.getTag()));
        } catch (JsonProcessingException e) {
            log.warn("用户 {} 更新的视频标签序列化失败", article.getUserId());
        }

        // 设置审核状态为待审核
        article.setExamineStatus(ExamineTypeEnum.PENDING_REVIEW.getCode());
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
        
        // 将文件分类，便于处理
        Map<Integer, List<FileTableEntity>> fileTypeMap = fileTableServiceArticle.stream()
                .collect(Collectors.groupingBy(FileTableEntity::getType));
        
        List<FileTableEntity> updateFileList = new ArrayList<>();
        
        // 处理视频文件变化
        updateVideoFileStatus(originalVideo, newVideoFile, fileTypeMap, articleId, updateFileList);
        
        // 处理图片文件变化
        updateImageFileStatus(originalVideo, newImageFile, fileTypeMap, articleId, updateFileList);

        // 更新修改的文件
        if (!updateFileList.isEmpty()) {
            log.info("更新文章ID: {}，更新文件数量: {}", articleId, updateFileList.size());
            fileTableService.updateBatchById(updateFileList);
        }
    }
    
    /**
     * 更新视频文件状态
     */
    private void updateVideoFileStatus(ArticleViewData originalVideo, FileTableEntity newVideoFile,
                                      Map<Integer, List<FileTableEntity>> fileTypeMap, 
                                      Long articleId, List<FileTableEntity> updateFileList) {
        // 获取原始视频文件列表
        List<FileTableEntity> originalVideoFiles = fileTypeMap.getOrDefault(FileTypeEnum.VIDEO.getCode(), new ArrayList<>());
        
        if (originalVideo.getVideo() != null && !originalVideo.getVideo().isEmpty()) {
            Long originalVideoId = originalVideo.getVideo().get(0).getId();
            
            // 检查视频是否变化
            if (!originalVideoId.equals(newVideoFile.getId())) {
                // 新视频关联到文章
                updateFileAssociation(newVideoFile, articleId, FileStatusEnum.USED.getCode(), updateFileList);
                
                // 找到原始视频并修改状态
                originalVideoFiles.stream()
                    .filter(video -> video.getId().equals(originalVideoId))
                    .findFirst()
                    .ifPresent(video -> updateFileAssociation(
                        video, articleId, FileStatusEnum.NOT_USE_FILE.getCode(), updateFileList));
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
                                     Long articleId, List<FileTableEntity> updateFileList) {
        // 获取原始图片文件列表 - 需要包含普通图片(PHOTO)和视频封面图片(VIDEO_PHOTO)
        List<FileTableEntity> originalImageFiles = new ArrayList<>();
        originalImageFiles.addAll(fileTypeMap.getOrDefault(FileTypeEnum.PHOTO.getCode(), new ArrayList<>()));
        originalImageFiles.addAll(fileTypeMap.getOrDefault(FileTypeEnum.VIDEO_PHOTO.getCode(), new ArrayList<>()));
        
        // 查找与当前文章URL匹配的原始图片文件
        FileTableEntity oldImageFile = originalImageFiles.stream()
            .filter(img -> img.getFileUrl().equals(originalVideo.getImgUrl()))
            .findFirst()
            .orElse(null);
        
        // 检查图片是否变化
        boolean imageChanged = oldImageFile == null || !oldImageFile.getId().equals(newImageFile.getId());
        
        if (imageChanged) {
            // 新图片关联到文章
            updateFileAssociation(newImageFile, articleId, FileStatusEnum.USED.getCode(), updateFileList);
            
            // 如果找到原始图片，将其状态修改为未使用
            if (oldImageFile != null) {
                updateFileAssociation(oldImageFile, articleId, FileStatusEnum.NOT_USE_FILE.getCode(), updateFileList);
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
     */
    private ArticleEntity buildArticleEntity(VideoArticleDto dto, long userId, FileTableEntity imageFile, FileTableEntity videoFile) {
        ArticleEntity article = new ArticleEntity();
        article.setTitle(dto.getTitle());
        article.setDescribes(dto.getDescribe());
        article.setCategory(dto.getCategory());
        article.setImgUrl(imageFile.getFileUrl());
        article.setUserId(userId);
        
        long time = System.currentTimeMillis();
        article.setCreateTime(time);
        article.setUpdateTime(time);
        
        // 设置标签
        try {
            article.setTag(OBJECT_MAPPER.writeValueAsString(dto.getTag()));
        } catch (JsonProcessingException e) {
            log.warn("用户 {} 添加的视频标签序列化失败", userId);
        }
        
        // 设置审核状态
        if (webSettingCache.getWebSettingEntity().getOpenExamine() == 1) {
            article.setExamineStatus(ExamineTypeEnum.PENDING_REVIEW.getCode());
        } else {
            article.setExamineStatus(ExamineTypeEnum.SUCCESS.getCode());
        }
        
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
        List<FileTableEntity> files = new ArrayList<>(2);
        
        // 设置文件关联和状态
        updateFileAssociation(videoFile, articleId, FileStatusEnum.USED.getCode(), files);
        updateFileAssociation(imageFile, articleId, FileStatusEnum.USED.getCode(), files);
        
        // 批量更新
        fileTableService.updateBatchById(files);
    }

    @Override
    public ArticleViewData getVideo(long id, HttpServletRequest request) {
        // 获取用户ID，未登录用户为-1
        long userId = -1;
        try {
            userId = JwtUtil.getUserId(request);
        } catch (UserNotLoginException ignored) {}

        // 构建查询条件
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        
        // 判断是否为管理员
        boolean isAdmin = false;
        try {
            if (RoleTypeEnum.ADMIN.getRole().equals(JwtUtil.getRole(request))) {
                isAdmin = true;
            }
        } catch (Exception e) {
            log.error("获取用户角色失败: {}", e.getMessage());
        }

        ArticleEntity articleEntity = this.getOne(wrapper);
        if (articleEntity == null) {
            return createHiddenArticleView();
        }
        
        // 如果不是管理员，且视频未通过审核
        if (!isAdmin && articleEntity.getExamineStatus() != ExamineTypeEnum.SUCCESS.getCode()) {
            return createHiddenArticleView();
        }
        
        return buildArticleViewData(articleEntity, userId);
    }

    /**
     * 创建隐藏的文章视图（无权查看时返回）
     */
    private ArticleViewData createHiddenArticleView() {
        ArticleViewData articleViewData = new ArticleViewData();
        articleViewData.setIsShow(false);
        return articleViewData;
    }

    /**
     * 构建文章视图数据
     */
    private ArticleViewData buildArticleViewData(ArticleEntity article, long userId) {
        ArticleViewData viewData = new ArticleViewData();
        BeanUtils.copyProperties(article, viewData);
        
        // 添加作者信息
        UserEntity author = userService.getById(article.getUserId());
        viewData.setUsername(author.getUsername());
        viewData.setFollowCount(author.getFollowCount());
        viewData.setAvatarUrl(author.getAvatarUrl());
        viewData.setIntroduction(author.getIntroduction());
        viewData.setDanmakuCount(article.getDanmakuCount());

        // 添加视频信息
        List<FileTableEntity> videos = fileTableService.findArticle(article.getId());
        long time = System.currentTimeMillis();
        for (FileTableEntity video : videos) {
            video.setKey(AesUtil.encrypt(
                    userId + "#" + video.getId() + "#" + (time + WebConstant.KEY_EXPIRY_DATE) + "#" + video.getFileNewName(),
                    WebConstant.AES_KEY
            ));
            if (video.getType().equals(FileTypeEnum.PHOTO.getCode()) || video.getType().equals(FileTypeEnum.VIDEO_PHOTO.getCode())) {
                viewData.setImageId(video.getId());
            }
        }
        viewData.setVideo(videos);

        // 添加标签
        try {
            viewData.setTag((List<String>) OBJECT_MAPPER.readValue(article.getTag(), List.class));
        } catch (JsonProcessingException e) {
            log.warn("视频id为 {} 的投稿反序列化标签时出现异常", article.getId());
        }

        // 添加分类信息
        addCategoryInfo(viewData, article.getCategory());
        
        viewData.setIsShow(true);
        return viewData;
    }

    @Override
    public void addDanmakuCount(Long id, long count) {
        this.baseMapper.addDanmakuCount(id, count);
    }

    /**
     * 个人主页视频列表
     * */
    @Override
    public PageUtils userArticleList(Map<String, Object> params, Long id, Integer type) {
        // 参数验证
        if (type == null || (type < FileTypeEnum.VIDEO.getCode() && type > FileTypeEnum.ARTICLE.getCode())) {
            type = FileTypeEnum.VIDEO.getCode();
        }
        
        // 构建查询条件
        Map<String, Object> conditions = Map.of(
            "user_id", id,
            "status", ArticleStatusEnum.NORMAL.getCode(),
            "examine_status", ExamineTypeEnum.SUCCESS.getCode(),
            "type", type
        );
        
        QueryWrapper<ArticleEntity> wrapper = createQueryWrapper(conditions, true);
        
        IPage<ArticleEntity> page = this.page(
                new Query<ArticleEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(createArticleViewData(addArticleCategory(page), page));
    }

    @Override
    public PageUtils userArticleList(Map<String, Object> params, String type, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        
        // 如果是管理员，加载所有数据
        if ("admin".equals(type) && RoleTypeEnum.ADMIN.getRole().equals(JwtUtil.getRole(request))) {
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
        if (!RoleTypeEnum.ADMIN.getRole().equals(JwtUtil.getRole(request))) {
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
        
        return new PageUtils(createArticleViewData(addArticleCategory(page), page));
    }

    @Override
    public ReturnCodeEnum examine(ExamineDto examineDto, HttpServletRequest request) {
        ArticleEntity articleEntity = this.getById(examineDto.getVideoId());
        long userId = JwtUtil.getUserId(request);
        
        if (articleEntity == null) {
            return ReturnCodeEnum.NOO_FOUND;
        }
        
        if (articleEntity.getExamineStatus() == ExamineTypeEnum.SUCCESS.getCode()) {
            return ReturnCodeEnum.REVIEWED;
        }
        
        // 更新审核状态
        updateExamineStatus(articleEntity, examineDto, userId);
        this.updateById(articleEntity);
        
        // 更新用户提交计数
        userService.addSubmitCount(articleEntity.getUserId(), 1);
        
        return ReturnCodeEnum.SUCCESS;
    }
    
    /**
     * 更新文章审核状态
     */
    private void updateExamineStatus(ArticleEntity article, ExamineDto examineDto, long userId) {
        if (examineDto.getResult()) {
            article.setExamineStatus(ExamineTypeEnum.SUCCESS.getCode());
        } else {
            article.setExamineStatus(ExamineTypeEnum.getStatus(examineDto.getType()));
        }
        article.setUpdateTime(System.currentTimeMillis());
        article.setExamineUser(userId);
        article.setExamineMessage(examineDto.getMessage());
    }

    @Override
    public List<ArticleViewData> hotView(int num) {
        List<ArticleEntity> list = findHotArticles(num);
        
        // 为获取的 list 列表增加用户信息
        List<ArticleViewData> viewData = addUserInfo(list);
        
        // 计算热度值并排序
        calculateHotScore(viewData);
        
        return viewData;
    }
    
    /**
     * 查找热门文章
     */
    private List<ArticleEntity> findHotArticles(int num) {
        // 获取当前时间
        long currentTime = System.currentTimeMillis();
        // 查询24小时内发布的帖子
        long ago = currentTime - 86400000;
        
        // 构造查询条件
        Map<String, Object> conditions = Map.of(
            "status", ArticleStatusEnum.NORMAL.getCode(),
            "examine_status", ExamineTypeEnum.SUCCESS.getCode()
        );
        
        QueryWrapper<ArticleEntity> wrapper = createQueryWrapper(conditions, true);
        wrapper.ge("create_time", ago);
        
        // 查询24小时内发布的内容
        List<ArticleEntity> list = this.list(wrapper);
        
        // 如果 24 小时内发布的内容数量不够，则查询过去发布的 num 个内容
        if (list.size() < num) {
            wrapper = createQueryWrapper(conditions, true);
            wrapper.last("LIMIT " + num);
            list = this.list(wrapper);
        }
        
        return list;
    }
    
    /**
     * 计算热度分数并排序
     */
    private void calculateHotScore(List<ArticleViewData> viewData) {
        // 计算 sort 值, 播放量加权 1， 评论 2， 收藏 4， 弹幕 1.5, 点赞 2， 不喜欢 -2
        for (ArticleViewData vd : viewData) {
            double sort = vd.getViewCount()
                    + vd.getCommentCount() * 2
                    + vd.getFavoriteCount() * 4
                    + vd.getDanmakuCount() * 1.5
                    + vd.getLikeCount() * 2
                    - vd.getDislikeCount() * 2;
            vd.setSort(sort);
        }
        
        // 按照 sort 排序
        viewData.sort((vd1, vd2) -> Double.compare(vd2.getSort(), vd1.getSort()));
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
        // 管理员可以删除所有文章
        if (RoleTypeEnum.ADMIN.getRole().equals(JwtUtil.getRole(request))) {
            return true;
        }
        
        // 用户只能删除自己的文章
        long userId = JwtUtil.getUserId(request);
        return article.getUserId().equals(userId);
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
        long result = playRecordingService.saveHistory(file, userId, IpUtil.getUa(request));
        if (result != 0) {
            this.addViewCount(result, 1L);
        }
        
        return true;
                /*
        // 原有会员限制代码，已注释
        if (userId == -1) {
            return false;
        }
        UserRoleEntity userRoleEntity = userRoleService.findByUserId(userId);
        String role = userRoleEntity.getRole();
        // 如果是管理员或VIP，则只记录播放信息，不记录播放次数
        if (RoleTypeEnum.ADMIN.getRole().equals(role) || RoleTypeEnum.VIP.getRole().equals(role)) {
            playRecordingService.saveHistory(file, userId, IpUtil.getUa(request));
            return true;
        }
        // TODO 修改为非会员可以重复观看这几个视频
        if (webSettingCache.getWebSettingEntity().getOpenNoVipLimit() == 1) {
            if (playRecordingService.todayPlayCount(userId) <= webSettingCache.getWebSettingEntity().getNoVipViewCount()) {
                playRecordingService.saveHistory(file, userId, IpUtil.getUa(request));
                return true;
            }
            return false;
        } else {
            playRecordingService.saveHistory(file, userId, IpUtil.getUa(request));
            return true;
        }
        */
    }

    @Override
    public PageUtils nowCategory(Map<String, Object> params, Integer id) {
        // 检查分类是否存在
        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(id);
        if (categoryEntity == null) {
            return new PageUtils(null);
        }

        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());
        
        // 根据父分类或子分类筛选
        applyCategoryFilter(wrapper, categoryEntity);
        
        wrapper.orderByDesc("create_time");
        IPage<ArticleEntity> page = this.page(
                new Query<ArticleEntity>().getPage(params),
                wrapper
        );
        
        return addUserInfo(page);
    }
    
    /**
     * 应用分类过滤条件
     */
    private void applyCategoryFilter(QueryWrapper<ArticleEntity> wrapper, CategoryEntity categoryEntity) {
        if (categoryEntity.getFatherId() == 0) {
            // 获取所有子分类ID
            categoryEntity = categoryCache.getCategoryMapWithChildren().get(categoryEntity.getId());
            List<Integer> childCategoryIds = categoryEntity.getChildren().stream()
                .map(CategoryEntity::getId)
                .collect(Collectors.toList());
            
            if (childCategoryIds.isEmpty()) {
                childCategoryIds.add(categoryEntity.getId());
            }
            wrapper.in("category", childCategoryIds);
        } else {
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
    public void addViewCount(Long articleId, long count) {
        this.baseMapper.addViewCount(articleId, count);
    }

    @Override
    public void addCount(String col, Long articleId, long count) {
        this.baseMapper.addCount(col, articleId, count);
    }

    /**
     * 创建文章视图数据分页对象
     */
    public IPage<ArticleViewData> createArticleViewData(List<ArticleViewData> articleViewData,
                                                        IPage<ArticleEntity> page) {
        return new IPage<ArticleViewData>() {
            @Override
            public List<OrderItem> orders() {
                return null;
            }

            @Override
            public List<ArticleViewData> getRecords() {
                return articleViewData;
            }

            @Override
            public IPage<ArticleViewData> setRecords(List<ArticleViewData> records) {
                return null;
            }

            @Override
            public long getTotal() {
                return page.getTotal();
            }

            @Override
            public IPage<ArticleViewData> setTotal(long total) {
                return null;
            }

            @Override
            public long getSize() {
                return page.getSize();
            }

            @Override
            public IPage<ArticleViewData> setSize(long size) {
                return null;
            }

            @Override
            public long getCurrent() {
                return page.getCurrent();
            }

            @Override
            public IPage<ArticleViewData> setCurrent(long current) {
                return null;
            }
        };
    }

    /**
     * 为文章添加分类信息
     */
    public List<ArticleViewData> addArticleCategory(IPage<ArticleEntity> page) {
        if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ArticleViewData> articleViewData = new ArrayList<>();
        for (ArticleEntity article : page.getRecords()) {
            ArticleViewData viewData = new ArticleViewData();
            BeanUtils.copyProperties(article, viewData);
            
            // 添加分类信息
            addCategoryInfo(viewData, article.getCategory());
            
            articleViewData.add(viewData);
        }
        
        return articleViewData;
    }
}