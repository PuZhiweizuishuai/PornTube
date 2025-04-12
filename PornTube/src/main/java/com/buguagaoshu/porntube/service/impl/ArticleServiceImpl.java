package com.buguagaoshu.porntube.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.buguagaoshu.porntube.cache.CategoryCache;
import com.buguagaoshu.porntube.cache.WebSettingCache;
import com.buguagaoshu.porntube.config.WebConstant;
import com.buguagaoshu.porntube.dto.ExamineDto;
import com.buguagaoshu.porntube.dto.VideoArticleDto;
import com.buguagaoshu.porntube.entity.*;
import com.buguagaoshu.porntube.enums.*;
import com.buguagaoshu.porntube.exception.UserNotLoginException;
import com.buguagaoshu.porntube.service.*;
import com.buguagaoshu.porntube.utils.*;
import com.buguagaoshu.porntube.vo.ArticleViewData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import com.buguagaoshu.porntube.dao.ArticleDao;
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
     * 为文章添加用户信息
     */
    public PageUtils addUserInfo(IPage<ArticleEntity> page) {
        List<ArticleViewData> articleViewData = addUserInfo(page.getRecords());
        return new PageUtils(createArticleViewData(articleViewData, page));
    }

    public List<ArticleViewData> addUserInfo(List<ArticleEntity> articleEntityList) {
        Set<Long> userIdList = articleEntityList.stream().map(ArticleEntity::getUserId).collect(Collectors.toSet());
        if (userIdList.isEmpty()) {
            return null;
        }
        Map<Long, UserEntity> userEntityMap = userService.userMapList(userIdList);
        List<ArticleViewData> articleViewData = new ArrayList<>();
        articleEntityList.forEach(a -> {
            ArticleViewData viewData = new ArticleViewData();
            UserEntity userEntity = userEntityMap.get(a.getUserId());
            BeanUtils.copyProperties(a, viewData);
            viewData.setUsername(userEntity.getUsername());
            viewData.setAvatarUrl(userEntity.getAvatarUrl());

            // 添加分类信息
            CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(a.getCategory());
            viewData.setChildrenCategory(categoryEntity);
            if (categoryEntity.getFatherId() != 0) {
                CategoryEntity f = categoryCache.getCategoryEntityMap().get(categoryEntity.getFatherId());
                viewData.setFatherCategory(f);
            }
            articleViewData.add(viewData);
        });
        return articleViewData;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnCodeEnum saveVideo(VideoArticleDto videoArticleDto, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        // 标题验证
        if (videoArticleDto.getTitle().length() > MAX_TITLE_LENGTH) {
            return ReturnCodeEnum.TITLE_TO_LONG;
        }
        
        // 描述验证
        if (!StringUtils.hasText(videoArticleDto.getDescribe()) && videoArticleDto.getDescribe().length() > MAX_DESCRIBE_LENGTH) {
            return ReturnCodeEnum.DESCRIBE_TO_LONG;
        }

        // 分类验证
        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(videoArticleDto.getCategory());
        if (categoryEntity == null) {
            return ReturnCodeEnum.CATEGORY_NOT_FOUND;
        }

        // 检查图片所有权
        FileTableEntity imageFile = fileTableService.getById(videoArticleDto.getImageId());
        if (imageFile == null || imageFile.getUploadUserId() != userId) {
            return ReturnCodeEnum.IMAGE_NO_POWER;
        }

        // 检查视频权限
        FileTableEntity videoFile = fileTableService.getById(videoArticleDto.getVideo().getId());
        if (videoFile == null || videoFile.getUploadUserId() != userId) {
            return ReturnCodeEnum.VIDEO_NO_POWER;
        }

        ArticleEntity articleEntity = buildArticleEntity(videoArticleDto, userId, imageFile, videoFile);
        
        // 保存文章
        this.save(articleEntity);
        
        // 更新文件状态
        updateFileStatus(articleEntity.getId(), imageFile, videoFile);
        
        return ReturnCodeEnum.SUCCESS;
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
        
        videoFile.setArticleId(articleId);
        imageFile.setArticleId(articleId);
        
        // 改写文件状态
        videoFile.setStatus(FileStatusEnum.USED.getCode());
        imageFile.setStatus(FileStatusEnum.USED.getCode());
        
        files.add(videoFile);
        files.add(imageFile);
        
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
        List<FileTableEntity> videos = fileTableService.findArticleVideo(article.getId());
        long time = System.currentTimeMillis();
        for (FileTableEntity video : videos) {
            video.setKey(AesUtil.encrypt(
                userId + "#" + video.getId() + "#" + (time + WebConstant.KEY_EXPIRY_DATE) + "#" + video.getFileNewName(), 
                WebConstant.AES_KEY
            ));
        }
        viewData.setVideo(videos);

        // 添加标签
        try {
            viewData.setTag((List<String>) OBJECT_MAPPER.readValue(article.getTag(), List.class));
        } catch (JsonProcessingException e) {
            log.warn("视频id为 {} 的投稿反序列化标签时出现异常", article.getId());
        }

        // 添加分类信息
        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(article.getCategory());
        viewData.setChildrenCategory(categoryEntity);
        if (categoryEntity.getFatherId() != 0) {
            CategoryEntity f = categoryCache.getCategoryEntityMap().get(categoryEntity.getFatherId());
            viewData.setFatherCategory(f);
        }
        
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
        
        QueryWrapper<ArticleEntity> wrapper = createNormalArticleWrapper();
        wrapper.eq("user_id", id);
        wrapper.eq("type", type);
        
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
            //
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

        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("examine_status", ExamineTypeEnum.PENDING_REVIEW.getCode());
        wrapper.orderByDesc("create_time");
        
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
        if (examineDto.getResult()) {
            articleEntity.setExamineStatus(ExamineTypeEnum.SUCCESS.getCode());
        } else {
            articleEntity.setExamineStatus(ExamineTypeEnum.getStatus(examineDto.getType()));
        }
        articleEntity.setUpdateTime(System.currentTimeMillis());
        articleEntity.setExamineUser(userId);
        articleEntity.setExamineMessage(examineDto.getMessage());

        this.updateById(articleEntity);
        
        // 更新用户提交计数
        userService.addSubmitCount(articleEntity.getUserId(), 1);
        
        return ReturnCodeEnum.SUCCESS;
    }

    @Override
    public List<ArticleViewData> hotView(int num) {
        // 获取当前时间
        long currentTime = System.currentTimeMillis();
        // 查询24小时内发布的帖子
        long ago = currentTime - 86400000;
        // 构造查询条件
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());
        //
        wrapper.ge("create_time", ago);
        wrapper.orderByDesc("create_time");
        // 查询24小时内发布的内容
        List<ArticleEntity> list = this.list(wrapper);
        // 如果 24 小时内发布的内容数量不够，则查询过去发布的 num 个内容
        if (list.size() < num) {
            QueryWrapper<ArticleEntity> wrapper2 = new QueryWrapper<>();
            wrapper2.eq("status", ArticleStatusEnum.NORMAL.getCode());
            wrapper2.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());
            wrapper.orderByDesc("create_time");
            wrapper2.last("LIMIT " + num);
            list = this.list(wrapper2);
        }
        // 为获取的 list 列表增加用户信息
        List<ArticleViewData> viewData = addUserInfo(list);
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
        return viewData;
    }

    @Override
    public int deleteArticle(ArticleEntity entity, HttpServletRequest request) {
        // 如果是管理员，可以删除所有视频
        ArticleEntity sys = getById(entity.getId());
        if (sys == null) {
            return 2;
        }
        if (RoleTypeEnum.ADMIN.getRole().equals(JwtUtil.getRole(request))) {
            return deleteArticleInDatabase(sys);
        } else {
           long userId = JwtUtil.getUserId(request);
           if (sys.getUserId().equals(userId)) {
               return deleteArticleInDatabase(sys);
           } else {
               return 1;
           }
        }
    }

    public int deleteArticleInDatabase(ArticleEntity sys) {
        // 将状态设置为删除
        sys.setStatus(ArticleStatusEnum.DELETE.getCode());
        List<FileTableEntity> article = fileTableService.findArticle(sys.getId());
        for (FileTableEntity f : article) {
            f.setStatus(FileStatusEnum.DELETE.getCode());
        }
        // 更新文件状态
        fileTableService.updateBatchById(article);

        this.updateById(sys);
        // 删除成功
        return 0;
    }

    @Override
    public Boolean hasThisVideoPlayPower(FileTableEntity file, Long userId, HttpServletRequest request) {
        // 所有用户都有观看权限
        if (userId == -1) {
            return true;
        }
        
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
        if (categoryEntity.getFatherId() == 0) {
            // 获取所有子分类ID
            categoryEntity = categoryCache.getCategoryMapWithChildren().get(id);
            List<Integer> childCategoryIds = categoryEntity.getChildren().stream()
                .map(CategoryEntity::getId)
                .collect(Collectors.toList());
            wrapper.in("category", childCategoryIds);
        } else {
            wrapper.eq("category", id);
        }
        
        wrapper.orderByDesc("create_time");
        IPage<ArticleEntity> page = this.page(
                new Query<ArticleEntity>().getPage(params),
                wrapper
        );
        
        return addUserInfo(page);
    }

    @Override
    public boolean restore(ArticleEntity articleEntity, HttpServletRequest request) {
        ArticleEntity sys = getById(articleEntity.getId());
        if (sys == null) {
            return false;
        }
        sys.setStatus(ArticleStatusEnum.NORMAL.getCode());
        List<FileTableEntity> article = fileTableService.findArticle(sys.getId());
        for (FileTableEntity f : article) {
            f.setStatus(FileStatusEnum.USED.getCode());
        }
        // 更新文件状态
        fileTableService.updateBatchById(article);
        this.updateById(sys);
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
        List<ArticleViewData> articleViewData = new ArrayList<>();
        page.getRecords().forEach(a -> {
            ArticleViewData viewData = new ArticleViewData();
            BeanUtils.copyProperties(a, viewData);
            
            CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(a.getCategory());
            viewData.setChildrenCategory(categoryEntity);
            if (categoryEntity.getFatherId() != 0) {
                CategoryEntity f = categoryCache.getCategoryEntityMap().get(categoryEntity.getFatherId());
                viewData.setFatherCategory(f);
            }
            
            articleViewData.add(viewData);
        });
        
        return articleViewData;
    }
}