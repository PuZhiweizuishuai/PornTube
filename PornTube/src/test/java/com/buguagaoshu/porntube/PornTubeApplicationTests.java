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
        fileTableService.findFileByFilename("d6e87553df594e25b90b6d583d0828ef.MP4");

    }
}
