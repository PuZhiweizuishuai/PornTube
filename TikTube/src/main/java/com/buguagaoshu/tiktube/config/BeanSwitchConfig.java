package com.buguagaoshu.tiktube.config;

import com.buguagaoshu.tiktube.repository.APICurrentLimitingRepository;
import com.buguagaoshu.tiktube.repository.CountLimitRepository;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import com.buguagaoshu.tiktube.repository.VerifyCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-11
 * 接口实现切换类
 */
@Slf4j
@Configuration
public class BeanSwitchConfig {
    
    @Bean
    @Primary
    public CountLimitRepository countLimitRepository(
            @Qualifier("countLimitRedisRepository") CountLimitRepository redisRepository,
            @Qualifier("countLimitRepositoryImpl") CountLimitRepository memoryRepository,
            MyConfigProperties configProperties,
            RedisRepository redisRepositoryService) {
        
        // 根据配置决定使用哪种存储实现，Redis 连不上时回退内存，不要让缓存在静默失败中失效
        if (configProperties.getOpenRedis() && redisRepositoryService.isReachable()) {
            return redisRepository;
        } else {
            return memoryRepository;
        }
    }
    
    @Bean
    @Primary
    public VerifyCodeRepository verifyCodeRepository(
            @Qualifier("verifyCodeRedisRepository") VerifyCodeRepository redisRepository,
            @Qualifier("sessionVerifyCodeRepositoryImpl") VerifyCodeRepository sessionRepository,
            MyConfigProperties configProperties,
            RedisRepository redisRepositoryService) {
        
        // 根据配置决定使用哪种存储实现
        if (configProperties.getOpenRedis() && redisRepositoryService.isReachable()) {
            return redisRepository;
        } else {
            return sessionRepository;
        }
    }
    
    @Bean
    @Primary
    public APICurrentLimitingRepository currentLimitingRepository(
            @Qualifier("currentLimitingRedisRepository") APICurrentLimitingRepository redisRepository,
            @Qualifier("currentLimitingMemoryRepository") APICurrentLimitingRepository memoryRepository,
            MyConfigProperties configProperties,
            RedisRepository redisRepositoryService) {
        
        // 根据配置决定使用哪种存储实现
        if (configProperties.getOpenRedis() && redisRepositoryService.isReachable()) {
            log.info("使用 Redis 接口限流配置！");
            return redisRepository;
        } else {
            log.info("使用 内存 接口限流配置！");
            return memoryRepository;
        }
    }
}