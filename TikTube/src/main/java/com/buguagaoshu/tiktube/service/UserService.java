package com.buguagaoshu.tiktube.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buguagaoshu.tiktube.dto.LoginDetails;
import com.buguagaoshu.tiktube.dto.PasswordDto;
import com.buguagaoshu.tiktube.entity.UserRoleEntity;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.utils.PageUtils;
import com.buguagaoshu.tiktube.entity.UserEntity;
import com.buguagaoshu.tiktube.vo.AdminAddUserData;
import com.buguagaoshu.tiktube.vo.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.Set;

/**
 * 
 *
 * @author Pu Zhiwei
 * @email puzhiweipuzhiwei@foxmail.com
 * @date 2020-09-05 14:38:43
 */
public interface UserService extends IService<UserEntity> {

    PageUtils queryPage(Map<String, Object> params);


    PageUtils userList(Map<String, Object> params);

    /**
     * 登录接口
     * @param loginDetails 登录请求数据
     * @param request 请求
     * @param response 将 token 写入 cookie
     * @return 用户登录信息
     * */
    User login(LoginDetails loginDetails, HttpServletRequest request, HttpServletResponse response);

    /**
     * 注册接口
     * @param userEntity 用户信息
     * @param request 请求信息
     * @return 注册结果
     * */
    ReturnCodeEnum register(UserEntity userEntity, HttpServletRequest request);


    Map<Long, UserEntity> userMapList(Set<Long> userIdList);

    /**
     * 返回用户信息
     * */
    User userInfo(Long userId);

    /**
     * 更新头像信息
     * */
    ReturnCodeEnum updateAvatar(User user, HttpServletRequest request);

    /**
     * 更新首页顶部大图
     * */
    ReturnCodeEnum updateTopImage(User user, HttpServletRequest request);

    /**
     * 更新密码
     * */
    ReturnCodeEnum updatePassword(PasswordDto passwordDto,
                                  HttpServletRequest request,
                                  HttpServletResponse response);

    /**
     * 更新用户名和简介
     * */
    ReturnCodeEnum updateInfo(User user, HttpServletRequest request);


    /**
     * 添加视频上传数
     * */
    void addSubmitCount(Long userId,  int count);


    UserRoleEntity updateRole(UserRoleEntity userRole, HttpServletRequest request);

    /**
     * 管理员重置密码
     * */
    String resetPassword(UserEntity userEntity);


    ReturnCodeEnum addUser(AdminAddUserData userEntity,  HttpServletRequest request);
}

