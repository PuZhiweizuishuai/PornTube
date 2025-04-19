package com.buguagaoshu.tiktube.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.dao.FollowDao;
import com.buguagaoshu.tiktube.entity.FollowEntity;
import com.buguagaoshu.tiktube.entity.UserEntity;
import com.buguagaoshu.tiktube.service.ArticleService;
import com.buguagaoshu.tiktube.service.FollowService;
import com.buguagaoshu.tiktube.service.UserService;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.utils.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-19
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowDao, FollowEntity> implements FollowService {
    private final UserService userService;

    private final ArticleService articleService;

    @Autowired
    public FollowServiceImpl(UserService userService,
                             ArticleService articleService) {
        this.userService = userService;
        this.articleService = articleService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFollow(FollowEntity follow, Long userId) {
        UserEntity user = userService.getById(follow.getFollowUser());
        if (user == null) {
            return false;
        }
        // 增加关注数
        userService.addCount("fans_count", user.getId(), 1);
        // 增加粉丝数
        userService.addCount("follow_count", userId, 1);
        follow.setId(null);
        follow.setCreateTime(System.currentTimeMillis());
        follow.setCreateUser(userId);
        this.save(follow);
        return true;
    }

    @Override
    public boolean deleteFollow(FollowEntity follow, Long userId) {
        QueryWrapper<FollowEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("create_user", userId);
        wrapper.eq("follow_user", follow.getFollowUser());

        return remove(wrapper, userId, 0);
    }

    @Override
    public boolean removeFans(FollowEntity follow, Long userId) {
        QueryWrapper<FollowEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("create_user", follow.getCreateUser());
        wrapper.eq("follow_user", userId);
        return remove(wrapper, userId, 1);
    }

    /**
     * @param type 0 粉丝主动取关
     *             1 移除关注
     * */
    private boolean remove(QueryWrapper<FollowEntity> wrapper, Long userId, Integer type) {
        FollowEntity sys = this.getOne(wrapper);
        if (sys == null) {
            return false;
        }
        if (type == 0) {
            // 增加关注数
            userService.addCount("fans_count", sys.getFollowUser(), -1);
            // 增加粉丝数

        } else {
            userService.addCount("fans_count", userId, -1);
            userService.addCount("follow_count", sys.getCreateUser(), -1);
        }
        this.removeById(sys.getId());
        return true;
    }

    @Override
    public boolean checkFollow(Long followUser, Long userId) {
        QueryWrapper<FollowEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("create_user", userId);
        wrapper.eq("follow_user", followUser);
        FollowEntity sys = this.getOne(wrapper);
        return sys != null;
    }

    @Override
    public PageUtils queryFollow(Map<String, Object> params, Long userId) {
        QueryWrapper<FollowEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("create_user", userId);
        // TODO 修改为按投稿时间排序
        wrapper.orderByDesc("create_time");
        return getUserInfo(params, wrapper, userId, 0);
    }

    /**
     * @param type 0 获取关注列表
     *             1 获取粉丝列表
     * */
    private PageUtils getUserInfo(Map<String, Object> params, QueryWrapper<FollowEntity> wrapper, Long userId, int type) {
        IPage<FollowEntity> page = this.page(
                new Query<FollowEntity>().getPage(params),
                wrapper
        );
        if (page.getRecords().isEmpty()) {
            return new PageUtils(page);
        }
        
        // 保存关注关系的创建时间，用于后续排序
        Map<Long, Long> followCreateTimeMap = new HashMap<>();
        if (type == 0) {
            page.getRecords().forEach(follow -> 
                followCreateTimeMap.put(follow.getFollowUser(), follow.getCreateTime()));
        } else {
            page.getRecords().forEach(follow -> 
                followCreateTimeMap.put(follow.getCreateUser(), follow.getCreateTime()));
        }
        
        // 拼凑用户信息 - 提取用户ID集合
        Set<Long> userIdList = (type == 0) 
            ? page.getRecords().stream().map(FollowEntity::getFollowUser).collect(Collectors.toSet())
            : page.getRecords().stream().map(FollowEntity::getCreateUser).collect(Collectors.toSet());
            
        // 获取用户信息
        List<UserEntity> userEntities = userService.listByIds(userIdList);
        
        // 批量查询互粉关系
        if (userId != null && !userIdList.isEmpty()) {
            // 查询互粉关系（单次批量查询）
            List<FollowEntity> reverseFollows = (type == 0)
                // 关注列表：查询这些用户是否也关注了当前用户
                ? this.list(new QueryWrapper<FollowEntity>()
                        .in("create_user", userIdList)
                        .eq("follow_user", userId))
                // 粉丝列表：查询当前用户是否也关注了这些用户
                : this.list(new QueryWrapper<FollowEntity>()
                        .eq("create_user", userId)
                        .in("follow_user", userIdList));
            
            // 构建互粉用户ID集合
            Set<Long> mutualFollowIds = reverseFollows.stream()
                .map(follow -> type == 0 ? follow.getCreateUser() : follow.getFollowUser())
                .collect(Collectors.toSet());
            
            // 设置互粉状态
            userEntities.forEach(user -> {
                user.setFriend(mutualFollowIds.contains(user.getId()));
                user.clean();
            });
        } else {
            userEntities.forEach(UserEntity::clean);
        }
        
        // 自定义排序: 优先按lastPublishTime排序，其次按关注createTime排序
        userEntities.sort((u1, u2) -> {
            // 首先按lastPublishTime降序排序（优先展示最近发布的）
            if (u1.getLastPublishTime() != null && u2.getLastPublishTime() != null) {
                int timeCompare = u2.getLastPublishTime().compareTo(u1.getLastPublishTime());
                if (timeCompare != 0) {
                    return timeCompare;
                }
            } else if (u1.getLastPublishTime() == null && u2.getLastPublishTime() != null) {
                return 1; // u2优先
            } else if (u1.getLastPublishTime() != null && u2.getLastPublishTime() == null) {
                return -1; // u1优先
            }
            
            // 如果lastPublishTime相同或都为null，则按照关注时间排序
            Long u1CreateTime = followCreateTimeMap.get(u1.getId());
            Long u2CreateTime = followCreateTimeMap.get(u2.getId());
            if (u1CreateTime != null && u2CreateTime != null) {
                return u2CreateTime.compareTo(u1CreateTime); // 降序排列，最近关注的优先
            }
            return 0;
        });
        
        return new PageUtils(userEntities, page.getTotal(), page.getSize(), page.getCurrent());
    }

    @Override
    public PageUtils queryFansList(Map<String, Object> params, Long userId) {
        QueryWrapper<FollowEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("follow_user", userId);
        wrapper.orderByDesc("create_time");
        return getUserInfo(params, wrapper, userId, 1);
    }

    @Override
    public PageUtils queryFollowArticle(Map<String, Object> params, Long userId) {
        QueryWrapper<FollowEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("create_user", userId);
        List<FollowEntity> followEntities = this.list(wrapper);
        if (followEntities.isEmpty()) {
            return new PageUtils(null, 0,0,0);
        }
        // 提取关注用户ID
        Set<Long> userIdList = followEntities.stream().map(FollowEntity::getFollowUser).collect(Collectors.toSet());
        // 查找相关稿件
        return articleService.fallowUserArticleList(params, userIdList);
    }
}
