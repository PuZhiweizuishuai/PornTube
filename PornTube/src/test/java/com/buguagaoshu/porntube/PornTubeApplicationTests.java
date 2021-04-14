package com.buguagaoshu.porntube;


import com.buguagaoshu.porntube.entity.FileTableEntity;
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

    }
}
