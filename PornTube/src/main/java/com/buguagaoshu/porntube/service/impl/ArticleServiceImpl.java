package com.buguagaoshu.porntube.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.buguagaoshu.porntube.cache.CategoryCache;
import com.buguagaoshu.porntube.cache.WebSettingCache;
import com.buguagaoshu.porntube.dto.VideoArticleDto;
import com.buguagaoshu.porntube.entity.CategoryEntity;
import com.buguagaoshu.porntube.entity.FileTableEntity;
import com.buguagaoshu.porntube.entity.UserEntity;
import com.buguagaoshu.porntube.enums.ArticleStatusEnum;
import com.buguagaoshu.porntube.enums.ExamineTypeEnum;
import com.buguagaoshu.porntube.enums.FileTypeEnum;
import com.buguagaoshu.porntube.enums.ReturnCodeEnum;
import com.buguagaoshu.porntube.service.FileTableService;
import com.buguagaoshu.porntube.service.UserService;
import com.buguagaoshu.porntube.utils.Constant;
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
import com.buguagaoshu.porntube.entity.ArticleEntity;
import com.buguagaoshu.porntube.service.ArticleService;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Pu Zhiwei
 * */
@Slf4j
@Service("articleService")
public class ArticleServiceImpl extends ServiceImpl<ArticleDao, ArticleEntity> implements ArticleService {

    private final CategoryCache categoryCache;

    private final WebSettingCache webSettingCache;

    private final FileTableService fileTableService;

    private final UserService userService;


    @Autowired
    public ArticleServiceImpl(CategoryCache categoryCache, WebSettingCache webSettingCache, FileTableService fileTableService, UserService userService) {
        this.categoryCache = categoryCache;
        this.webSettingCache = webSettingCache;
        this.fileTableService = fileTableService;
        this.userService = userService;
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
        Map<Long, UserEntity> userEntityMap = userService.userMapList(userIdList);
        List<ArticleViewData> articleViewData = new ArrayList<>();
        page.getRecords().forEach(a->{
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
    public ReturnCodeEnum saveVideo(VideoArticleDto videoArticleDto, HttpServletRequest request)  {
        Claims user = JwtUtil.getUser(request);
        long userId = Long.parseLong(user.getId());
        ArticleEntity articleEntity = new ArticleEntity();
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
        fileTableService.saveBatch(list);
        return ReturnCodeEnum.SUCCESS;
    }

    @Override
    public ArticleViewData getVideo(long id) {
        // TODO 播放记录，播放次数限制
        QueryWrapper<ArticleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        wrapper.eq("status", ArticleStatusEnum.NORMAL.getCode());
        wrapper.eq("examine_status", ExamineTypeEnum.SUCCESS.getCode());
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

}