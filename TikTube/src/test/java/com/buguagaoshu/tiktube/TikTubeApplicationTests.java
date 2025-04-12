package com.buguagaoshu.tiktube;


import com.buguagaoshu.tiktube.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class TikTubeApplicationTests {

    @Autowired
    private ArticleService articleService;

    @Test
    void contextLoads() {
        //articleService.addCount("comment_count", 8L, 1L);
        //List<FileTableEntity> fileTableEntities = FFmpegUtils.randomGrabberFFmpegImage(new File("D:\\Pictures\\视频项目\\SNH48《公主披风》舞蹈版.mp4"), 6, 1);
        //System.out.println(fileTableEntities == null);
    }
}
