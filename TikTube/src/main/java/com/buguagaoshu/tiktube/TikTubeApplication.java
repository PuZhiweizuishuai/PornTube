package com.buguagaoshu.tiktube;

import com.buguagaoshu.tiktube.config.MyConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Pu Zhiwei
 * */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(MyConfigProperties.class)
public class TikTubeApplication {

    public final static String VERSION = "v1.4.0 2026-10-12";

    public static void main(String[] args) {
        SpringApplication.run(TikTubeApplication.class, args);
    }

}
