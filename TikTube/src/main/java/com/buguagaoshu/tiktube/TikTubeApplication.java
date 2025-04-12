package com.buguagaoshu.tiktube;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Pu Zhiwei
 * */
@SpringBootApplication
@EnableScheduling
public class TikTubeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TikTubeApplication.class, args);
    }

}
