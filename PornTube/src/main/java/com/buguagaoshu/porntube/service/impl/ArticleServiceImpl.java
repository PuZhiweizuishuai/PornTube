package com.buguagaoshu.porntube.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.buguagaoshu.porntube.cache.CategoryCache;
import com.buguagaoshu.porntube.cache.WebSettingCache;
import com.buguagaoshu.porntube.config.WebConstant;
import com.buguagaoshu.porntube.dto.ExamineDto;
import com.buguagaoshu.porntube.dto.VideoArticleDto;
import com.buguagaoshu.porntube.entity.*;
import com.buguagaoshu.porntube.enums.*;
import com.buguagaoshu.porntube.service.*;
import com.buguagaoshu.porntube.utils.AesUtil;
import com.buguagaoshu.porntube.utils.JwtUtil;
import com.buguagaoshu.porntube.vo.ArticleViewData;
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
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;

import com.buguagaoshu.porntube.dao.ArticleDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Pu Zhiwei
 */
@Slf4j
@Service("articleService")
public class ArticleServiceImpl extends ServiceImpl<ArticleDao, ArticleEntity> implements ArticleService {

    private final CategoryCache categoryCache;

    private final WebSettingCache webSettingCache;

    private final FileTableService fileTableService;

    private final UserService userService;

    private final UserRoleService userRoleService;

    private PlayRecordingService playRecordingService;

    @Autowired
    public void setPlayRecordingService(PlayRecordingService playRecordingService) {
        this.playRecordingService = playRecordingService;
    }

    @Autowired
    public ArticleServiceImpl(CategoryCache categoryCache, WebSettingCache webSettingCache, FileTableService fileTableService, UserService userService, UserRoleService userRoleService) {
        this.categoryCache = categoryCache;
        this.webSettingCache = webSettingCache;
        this.fileTableService = fileTableService;
        this.userService = userService;
        this.userRoleService = userRoleService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();


        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());
        wrapper.orderByDesc("create_time");
        IPage<ArticleEntity> page = this.page(
                new Query<ArticleEntity>().getPage(params),
                wrapper
        );

        Set<Long> userIdList = page.getRecords().stream().map(ArticleEntity::getUserId).collect(Collectors.toSet());
        if (userIdList.size() == 0) {
            return null;
        }
        Map<Long, UserEntity> userEntityMap = userService.userMapList(userIdList);
        List<ArticleViewData> articleViewData = new ArrayList<>();
        page.getRecords().forEach(a -> {
            ArticleViewData viewData = new ArticleViewData();
            UserEntity userEntity = userEntityMap.get(a.getUserId());
            BeanUtils.copyProperties(a, viewData);
            viewData.setUsername(userEntity.getUsername());
            viewData.setAvatarUrl(userEntity.getAvatarUrl());
            articleViewData.add(viewData);
        });

        IPage<ArticleViewData> viewDataIPage = new IPage<ArticleViewData>() {
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
        return new PageUtils(viewDataIPage);
    }

    @Override
    @Transactional
    public ReturnCodeEnum saveVideo(VideoArticleDto videoArticleDto, HttpServletRequest request) {
        Claims user = JwtUtil.getUser(request);
        long userId = Long.parseLong(user.getId());
        ArticleEntity articleEntity = new ArticleEntity();
        if (videoArticleDto.getTitle().length() > 50) {
            return ReturnCodeEnum.TITLE_TO_LONG;
        }
        articleEntity.setTitle(videoArticleDto.getTitle());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            articleEntity.setTag(objectMapper.writeValueAsString(videoArticleDto.getTag()));
        } catch (JsonProcessingException e) {
            log.warn("用户 {} 添加的视频标签序列化失败", user.getId());
        }
        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(videoArticleDto.getCategory());
        if (categoryEntity == null) {
            return ReturnCodeEnum.CATEGORY_NOT_FOUND;
        }
        articleEntity.setCategory(videoArticleDto.getCategory());
        if (!StringUtils.isEmpty(videoArticleDto.getDescribe()) && videoArticleDto.getDescribe().length() > 200) {
            return ReturnCodeEnum.DESCRIBE_TO_LONG;
        }
        articleEntity.setDescribes(videoArticleDto.getDescribe());
        // 检查图片所有权
        FileTableEntity imageId = fileTableService.getById(videoArticleDto.getImageId());
        if (imageId == null || imageId.getUploadUserId() != userId) {
            return ReturnCodeEnum.IMAGE_NO_POWER;
        }
        articleEntity.setImgUrl(imageId.getFileUrl());
        articleEntity.setUserId(userId);
        long time = System.currentTimeMillis();
        articleEntity.setCreateTime(time);
        articleEntity.setUpdateTime(time);
        // 检查视频权限
        FileTableEntity videoId = fileTableService.getById(videoArticleDto.getVideo().getId());
        if (videoId == null || videoId.getUploadUserId() != userId) {
            return ReturnCodeEnum.VIDEO_NO_POWER;
        }
        // TODO 在审核时将投稿量加 1
        // TODO 并且记得看是否提高普通用户每日观看量
        if (webSettingCache.getWebSettingEntity().getOpenExamine() == 1) {
            articleEntity.setExamineStatus(ExamineTypeEnum.PENDING_REVIEW.getCode());
        } else {
            articleEntity.setExamineStatus(ExamineTypeEnum.SUCCESS.getCode());
        }
        articleEntity.setType(FileTypeEnum.VIDEO.getCode());
        this.save(articleEntity);
        List<FileTableEntity> list = new ArrayList<>();
        videoId.setArticleId(articleEntity.getId());
        imageId.setArticleId(articleEntity.getId());
        list.add(videoId);
        list.add(imageId);
        fileTableService.updateBatchById(list);
        return ReturnCodeEnum.SUCCESS;
    }

    @Override
    public ArticleViewData getVideo(long id, HttpServletRequest request) {
        Claims user = JwtUtil.getUser(request);
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        // 非管理员添加审核条件
        boolean flag = true;
        if (user != null && RoleTypeEnum.ADMIN.getRole().equals(user.get("authorities"))) {
            flag = false;
        }
        long userId = -1;
        if (user != null) {
            userId = Long.parseLong(user.getId());
        }

        if (flag) {
            wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());
        }


        ArticleEntity articleEntity = this.getOne(wrapper);
        if (articleEntity == null) {
            ArticleViewData articleViewData = new ArticleViewData();
            articleViewData.setIsShow(false);
            return articleViewData;
        }
        ArticleViewData articleViewData = new ArticleViewData();
        BeanUtils.copyProperties(articleEntity, articleViewData);
        UserEntity author = userService.getById(articleEntity.getUserId());
        articleViewData.setUsername(author.getUsername());
        articleViewData.setFollowCount(author.getFollowCount());
        articleViewData.setAvatarUrl(author.getAvatarUrl());
        articleViewData.setIntroduction(author.getIntroduction());

        articleViewData.setDanmakuCount(articleEntity.getDanmakuCount());

        List<FileTableEntity> video = fileTableService.findArticleVideo(id);
        long time = System.currentTimeMillis();
        for (FileTableEntity f : video) {
            f.setKey(AesUtil.encrypt(userId + "#" + f.getId() + "#" + (time + WebConstant.KEY_EXPIRY_DATE), WebConstant.AES_KEY));
        }
        articleViewData.setVideo(video);

        ObjectMapper mapper = new ObjectMapper();
        try {
            articleViewData.setTag((List<String>) mapper.readValue(articleEntity.getTag(), List.class));
        } catch (JsonProcessingException e) {
            log.warn("视频id为 {} 的投稿反序列化标签时出现异常，请及时检查！", articleEntity.getId());
        }


        CategoryEntity categoryEntity = categoryCache.getCategoryEntityMap().get(articleEntity.getCategory());
        articleViewData.setChildrenCategory(categoryEntity);
        if (categoryEntity.getFatherId() != 0) {
            CategoryEntity f = categoryCache.getCategoryEntities().get(categoryEntity.getFatherId());
            articleViewData.setFatherCategory(f);
        }
        articleViewData.setIsShow(true);
        return articleViewData;
    }

    @Override
    public void addDanmakuCount(Long id, long count) {
        this.baseMapper.addDanmakuCount(id, count);
    }

    @Override
    public PageUtils userArticleList(Map<String, Object> params, Long id, Integer type) {
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        if (type == null || (type < FileTypeEnum.VIDEO.getCode() && type > FileTypeEnum.ARTICLE.getCode())) {
            type = FileTypeEnum.VIDEO.getCode();
        }
        wrapper.eq("user_id", id);
        wrapper.eq("type", type);
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());
        wrapper.orderByDesc("create_time");
        IPage<ArticleEntity> page = this.page(
                new Query<ArticleEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils examineList(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        Claims user = JwtUtil.getUser(request);
        // Long userID = Long.parseLong(user.getId());
        if (!RoleTypeEnum.ADMIN.getRole().equals(user.get("authorities"))) {
            return null;
        }

        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("examine_status", ExamineTypeEnum.PENDING_REVIEW.getCode());
        wrapper.orderByDesc("create_time");
        IPage<ArticleEntity> page = this.page(
                new Query<ArticleEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public ReturnCodeEnum examine(ExamineDto examineDto, HttpServletRequest request) {
        ArticleEntity articleEntity = this.getById(examineDto.getVideoId());
        long userId = Long.parseLong(JwtUtil.getUser(request).getId());
        if (articleEntity == null) {
            return ReturnCodeEnum.NOO_FOUND;
        }
        if (articleEntity.getExamineStatus() == ExamineTypeEnum.SUCCESS.getCode()) {
            return ReturnCodeEnum.REVIEWED;
        }
        if (examineDto.getResult()) {
            articleEntity.setExamineStatus(ExamineTypeEnum.SUCCESS.getCode());
        } else {
            articleEntity.setExamineStatus(ExamineTypeEnum.getStatus(examineDto.getType()));
        }

        articleEntity.setExamineUser(userId);
        articleEntity.setExamineMessage(examineDto.getMessage());
        this.updateById(articleEntity);
        // TODO 加入缓存处理
        userService.addSubmitCount(articleEntity.getUserId(), 1);
        // TODO 向用户发送处理结果
        return ReturnCodeEnum.SUCCESS;
    }

    @Override
    public Boolean hasThisVideoPlayPower(FileTableEntity file, Long userId, HttpServletRequest request) {
        // TODO 暂时设计为不登陆无法观看
        if (userId == -1) {
            return false;
        }
        UserRoleEntity userRoleEntity = userRoleService.findByUserId(userId);
        String role = userRoleEntity.getRole();
        // 如果是管理员或VIP，则只记录播放信息，不记录播放次数
        if (RoleTypeEnum.ADMIN.getRole().equals(role) || RoleTypeEnum.VIP.getRole().equals(role)) {
            playRecordingService.saveHistory(file, userId, request.getHeader("user-agent"));
            return true;
        }
        // TODO 修改为非会员可以重复观看这几个视频
        if (webSettingCache.getWebSettingEntity().getOpenNoVipLimit() == 1) {
            if (playRecordingService.todayPlayCount(userId) <= webSettingCache.getWebSettingEntity().getNoVipViewCount()) {
                playRecordingService.saveHistory(file, userId, request.getHeader("user-agent"));
                return true;
            }
            return false;
        } else {
            playRecordingService.saveHistory(file, userId, request.getHeader("user-agent"));
            return true;
        }
    }

    @Override
    public void addViewCount(Long articleId, long count) {
        this.baseMapper.addViewCount(articleId, count);
    }

}