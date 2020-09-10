package com.buguagaoshu.porntube;


import com.buguagaoshu.porntube.entity.UserEntity;
import com.buguagaoshu.porntube.entity.UserRoleEntity;
import com.buguagaoshu.porntube.enums.RoleTypeEnum;
import com.buguagaoshu.porntube.service.*;
import com.buguagaoshu.porntube.utils.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;



@SpringBootTest
class PornTubeApplicationTests {



    @Autowired
    private FileTableService fileTableService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    @Test
    void contextLoads() {
//        UserEntity userEntity = new UserEntity();
//        userEntity.setUsername("看片");
//        userEntity.setMail("123456@qq.com");
//        userEntity.setPassword(PasswordUtil.encode("123456"));
//        userEntity.setTopImgUrl("/images/top.png");
//        userEntity.setAvatarUrl("/images/head.png");
//        userEntity.setCreateTime(System.currentTimeMillis());
//
//        userService.save(userEntity);
//        UserRoleEntity userRoleEntity = new UserRoleEntity();
//        userRoleEntity.setUserid(userEntity.getId());
//        userRoleEntity.setCreateTime(System.currentTimeMillis());
//        userRoleEntity.setModified(0L);
//        userRoleEntity.setUpdateTime(System.currentTimeMillis());
//        userRoleEntity.setRole(RoleTypeEnum.VIP.getRole());
//        userRoleEntity.setVipStartTime(System.currentTimeMillis());
//        userRoleEntity.setVipStopTime(System.currentTimeMillis() + 300000);
//        userRoleService.save(userRoleEntity);
    }
}
