package com.buguagaoshu.porntube;

import com.buguagaoshu.porntube.entity.ArticleEntity;
import com.buguagaoshu.porntube.entity.FileTableEntity;
import com.buguagaoshu.porntube.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class PornTubeApplicationTests {



    @Autowired
    private FileTableService fileTableService;

    @Autowired
    private ArticleService articleService;

    @Test
    void contextLoads() {

        List<ArticleEntity> list = articleService.list();
        int count  = 0;
        for (ArticleEntity a : list) {
            count++;
            a.setUserId(8L);
        }
        articleService.updateBatchById(list);
        System.out.println(count);


    }
}
