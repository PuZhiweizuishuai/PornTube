package com.buguagaoshu.porntube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.porntube.entity.UserEntity;
import com.buguagaoshu.porntube.utils.PageUtils;
import com.buguagaoshu.porntube.entity.UserRoleEntity;

import java.util.Map;

/**
 * 
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 15:03:54
 */
public interface UserRoleService extends IService<UserRoleEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 通过用户ID获取用户权限
     * @param id 用户ID
     * @return 用户权限
     * */
    UserRoleEntity findByUserId(Long id);

    /**
     * 写入角色
     * */
    void saveUserRole(UserEntity userEntity, String role, long modified);


    /**
     * 通过用户ID更新用户权限
     * */
    void updateRoleByUserId(UserRoleEntity userRoleEntity);
}

