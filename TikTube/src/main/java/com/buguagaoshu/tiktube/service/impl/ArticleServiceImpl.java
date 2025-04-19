package com.buguagaoshu.tiktube.service.impl;

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

import java.util.*;
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
    private static final int ONE_DAY_MILLIS = 86400000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CategoryCache categoryCache;
    private final WebSettingCache webSettingCache;
    private final FileTableService fileTableService;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private PlayRecordingService playRecordingService;
    private NotificationService notificationService;

    @Autowired
    public ArticleServiceImpl(CategoryCache categoryCache,
                              WebSettingCache webSettingCache,
                              FileTableService fileTableService,
                              UserService userService,
                              UserRoleService userRoleService,
                              NotificationService notificationService) {
        this.categoryCache = categoryCache;
        this.webSettingCache = webSettingCache;
        this.fileTableService = fileTableService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.notificationService = notificationService;
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
        conditions.forEach(wrapper::eq);
        
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
        return new PageUtils(articleViewData, page.getTotal(), page.getSize(), page.getCurrent());
    }

    public List<ArticleViewData> addUserInfo(List<ArticleEntity> articleEntityList) {
        if (articleEntityList == null || articleEntityList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 收集所有用户ID - 使用HashSet提高去重效率
        Set<Long> userIdList = articleEntityList.stream()
            .map(ArticleEntity::getUserId)
            .collect(Collectors.toCollection(HashSet::new));
            
        if (userIdList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量获取用户信息 - 一次性查询所有用户数据
        Map<Long, UserEntity> userEntityMap = userService.userMapList(userIdList);
        
        // 获取所有分类的缓存以减少重复查询
        Map<Integer, CategoryEntity> categoryMap = categoryCache.getCategoryEntityMap();
        
        // 使用并行流处理提高性能，适合数据量大的情况
        return articleEntityList.parallelStream().map(a -> {
            ArticleViewData viewData = new ArticleViewData();
            UserEntity userEntity = userEntityMap.get(a.getUserId());
            
            // 拷贝基本属性
            BeanUtils.copyProperties(a, viewData);
            
            // 设置用户信息
            if (userEntity != null) {
                viewData.setUsername(userEntity.getUsername());
                viewData.setAvatarUrl(userEntity.getAvatarUrl());
            }

            // 添加分类信息，使用缓存减少对CategoryCache的重复访问
            Integer categoryId = a.getCategory();
            CategoryEntity categoryEntity = categoryMap.get(categoryId);
            if (categoryEntity != null) {
                viewData.setChildrenCategory(categoryEntity);
                if (categoryEntity.getFatherId() != 0) {
                    CategoryEntity f = categoryMap.get(categoryEntity.getFatherId());
                    viewData.setFatherCategory(f);
                }
            }
            
            return viewData;
        }).collect(Collectors.toList());
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
        articleEntity.setIp(IpUtil.getIpAddr(request));
        articleEntity.setUa(IpUtil.getUa(request));

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
        if (!isAdmin && articleEntity.getExamineStatus() != ExamineTypeEnum.SUCCESS.getCode() && !articleEntity.getUserId().equals(articleEntity.getUserId())) {
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
        if (author != null) {
            viewData.setUsername(author.getUsername());
            viewData.setFollowCount(author.getFollowCount());
            viewData.setFansCount(author.getFansCount());
            viewData.setAvatarUrl(author.getAvatarUrl());
            viewData.setIntroduction(author.getIntroduction());
        }
        viewData.setDanmakuCount(article.getDanmakuCount());

        // 批量获取视频信息
        List<FileTableEntity> videos = fileTableService.findArticle(article.getId());
        long time = System.currentTimeMillis();
        
        // 并行处理文件列表以生成加密密钥
        videos.parallelStream().forEach(video -> {
            video.setKey(AesUtil.encrypt(
                    userId + "#" + video.getId() + "#" + (time + WebConstant.KEY_EXPIRY_DATE) + "#" + video.getFileNewName(),
                    WebConstant.AES_KEY
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
        // 获取当前时间
        long currentTime = System.currentTimeMillis();
        // 24小时前的时间戳
        long ago = currentTime - ONE_DAY_MILLIS;

        // 直接从数据库获取已经计算好热度值并排序的文章列表
        List<ArticleEntity> list = this.baseMapper.findHotArticlesWithScore(ago, num);

        // 如果24小时内的文章不足，获取最新发布的num条内容计算热度
        if (list.size() < num) {
            // 构造查询条件
            Map<String, Object> conditions = Map.of(
                    "status", ArticleStatusEnum.NORMAL.getCode(),
                    "examine_status", ExamineTypeEnum.SUCCESS.getCode()
            );
            QueryWrapper<ArticleEntity> wrapper = createQueryWrapper(conditions, true);
            wrapper.last("LIMIT " + num);
            list = this.list(wrapper);
            // 计算 sort 值, 播放量加权 1， 评论 2， 收藏 4， 弹幕 1.5, 点赞 2， 不喜欢 -2
            for (ArticleEntity vd : list) {
                double sort = vd.getViewCount()
                        + vd.getCommentCount() * 2
                        + vd.getFavoriteCount() * 4
                        + vd.getDanmakuCount() * 1.5
                        + vd.getLikeCount() * 2
                        - vd.getDislikeCount() * 2;
                vd.setSort(sort);
            }

            // 按照 sort 排序
            list.sort((vd1, vd2) -> Double.compare(vd2.getSort(), vd1.getSort()));
        }

        // 为获取的列表增加用户信息
        return addUserInfo(list);
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

        // 构建查询条件
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());
        
        // 根据父分类或子分类筛选
        applyCategoryFilter(wrapper, categoryEntity);
        
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
    public void addViewCount(Long articleId, long count) {
        this.baseMapper.addViewCount(articleId, count);
    }

    @Override
    public void addCount(String col, Long articleId, long count) {
        this.baseMapper.addCount(col, articleId, count);
    }


    /**
     * 为文章添加分类信息
     */
    public List<ArticleViewData> addArticleCategory(IPage<ArticleEntity> page) {
        if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取分类缓存，避免重复查询
        Map<Integer, CategoryEntity> categoryMap = categoryCache.getCategoryEntityMap();
        
        // 使用并行流处理转换，提高大数据量处理效率
        return page.getRecords().parallelStream()
            .map(article -> {
                ArticleViewData viewData = new ArticleViewData();
                BeanUtils.copyProperties(article, viewData);
                
                // 添加分类信息
                Integer categoryId = article.getCategory();
                CategoryEntity categoryEntity = categoryMap.get(categoryId);
                if (categoryEntity != null) {
                    viewData.setChildrenCategory(categoryEntity);
                    if (categoryEntity.getFatherId() != 0) {
                        CategoryEntity f = categoryMap.get(categoryEntity.getFatherId());
                        viewData.setFatherCategory(f);
                    }
                }
                
                return viewData;
            })
            .collect(Collectors.toList());
    }

    /**
     * 添加分类信息到视图数据
     */
    private void addCategoryInfo(ArticleViewData viewData, Integer categoryId) {
        // 由于addUserInfo方法已集成类别信息的添加，此方法仅用于其他地方的单独调用
        Map<Integer, CategoryEntity> categoryMap = categoryCache.getCategoryEntityMap();
        CategoryEntity categoryEntity = categoryMap.get(categoryId);
        if (categoryEntity != null) {
            viewData.setChildrenCategory(categoryEntity);
            if (categoryEntity.getFatherId() != 0) {
                CategoryEntity f = categoryMap.get(categoryEntity.getFatherId());
                viewData.setFatherCategory(f);
            }
        }
    }
}