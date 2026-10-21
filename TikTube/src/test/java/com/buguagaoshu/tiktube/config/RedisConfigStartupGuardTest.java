package com.buguagaoshu.tiktube.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * open-redis=true 是「这次部署依赖 Redis」的声明：连不上时必须启动失败，
 * 而不是静默回退内存把限流、验证码、两步验证变成每实例各自为政
 */
class RedisConfigStartupGuardTest {

    @Test
    void startupAbortsWhenRedisUnreachable() {
        // 本机这个端口没有监听，连接会被立刻拒绝，等价于 Redis 没起
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration("127.0.0.1", 6399);
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration);
        connectionFactory.afterPropertiesSet();
        try {
            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> new RedisConfig().redisTemplate(connectionFactory));
            assertTrue(error.getMessage().contains("open-redis"), "报错里要写清楚是哪个开关、怎么退回去");
            assertTrue(error.getMessage().contains("改回 false"));
        } finally {
            connectionFactory.destroy();
        }
    }
}
