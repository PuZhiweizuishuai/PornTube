package com.buguagaoshu.porntube;

import com.buguagaoshu.porntube.service.ArticleService;
import com.buguagaoshu.porntube.service.CategoryService;
import com.buguagaoshu.porntube.service.InvitationCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PornTubeApplicationTests {

    @Autowired
    private InvitationCodeService invitationCodeService;


    @Autowired
    private ArticleService articleService;

    @Test
    void contextLoads() {
        articleService.addDanmakuCount(1L, 1L);
        //invitationCodeService.check("132456");
    }

}
