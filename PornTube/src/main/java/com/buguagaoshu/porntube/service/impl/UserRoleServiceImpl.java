package com.buguagaoshu.porntube.service.impl;

import com.buguagaoshu.porntube.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.utils.Query;

import com.buguagaoshu.porntube.dao.UserRoleDao;
import com.buguagaoshu.porntube.entity.UserRoleEntity;
import com.buguagaoshu.porntube.service.UserRoleService;


@Service("userRoleService")
public class UserRoleServiceImpl extends ServiceImpl<UserRoleDao, UserRoleEntity> implements UserRoleService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<UserRoleEntity> page = this.page(
                new Query<UserRoleEntity>().getPage(params),
                new QueryWrapper<UserRoleEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public UserRoleEntity findByUserId(Long id) {
        return this.getOne(new QueryWrapper<UserRoleEntity>().eq("userId", id));
    }

    @Override
    public Map<Long, UserRoleEntity> listByUserId(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        QueryWrapper<UserRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("userId", userIds);
        List<UserRoleEntity> list = this.list(queryWrapper);
        Map<Long, UserRoleEntity> map = new HashMap<>();
        for (UserRoleEntity userRole : list) {
            map.put(userRole.getUserid(), userRole);
        }
        return map;
    }

    @Override
    public void saveUserRole(UserEntity userEntity, String role, long modified) {
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setCreateTime(System.currentTimeMillis());
        userRoleEntity.setRole(role);
        userRoleEntity.setUpdateTime(System.currentTimeMillis());
        userRoleEntity.setUserid(userEntity.getId());
        userRoleEntity.setModified(modified);
        save(userRoleEntity);
    }

    @Override
    public void updateRoleByUserId(UserRoleEntity userRoleEntity) {
        UserRoleEntity sys = findByUserId(userRoleEntity.getUserid());
        userRoleEntity.setId(sys.getId());
        this.updateById(userRoleEntity);
    }

}