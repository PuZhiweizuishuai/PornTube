package com.buguagaoshu.porntube;


import com.buguagaoshu.porntube.entity.FileTableEntity;
import com.buguagaoshu.porntube.entity.UserEntity;
import com.buguagaoshu.porntube.entity.UserRoleEntity;
import com.buguagaoshu.porntube.enums.RoleTypeEnum;
import com.buguagaoshu.porntube.service.*;
import com.buguagaoshu.porntube.utils.FFmpegUtils;
import com.buguagaoshu.porntube.utils.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;


@SpringBootTest
class PornTubeApplicationTests {

    @Autowired
    private ArticleService articleService;

    @Test
    void contextLoads() {
        //articleService.addCount("comment_count", 8L, 1L);
        List<FileTableEntity> fileTableEntities = FFmpegUtils.randomGrabberFFmpegImage(new File("D:\\Pictures\\视频项目\\SNH48《公主披风》舞蹈版.mp4"), 6, 1);
        System.out.println(fileTableEntities == null);
    }
}
