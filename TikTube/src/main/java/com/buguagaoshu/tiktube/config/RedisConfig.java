package com.buguagaoshu.tiktube.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

/**
 * Redis配置类
 * 只有当tiktube.open-redis=true时才启用，未启用时 RedisRepository.isAvailable() 为 false，
 * 各缓存自动落回内存实现
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "tiktube", name = "open-redis", havingValue = "true")
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * key 用 String 序列化，方便直接 redis-cli 排查；value 用 JSON，与计数 Hash 的原始字节读写互不干扰
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        GenericJacksonJsonRedisSerializer valueSerializer = new GenericJacksonJsonRedisSerializer(new ObjectMapper());

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        // Hash 的 key 也采用 String 序列化，计数类 Hash 靠 HINCRBY 读写原始字节，序列化方式必须一致
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        pingOrAbort(template);
        return template;
    }

    /**
     * open-redis=true 表示这次部署依赖 Redis：连不上时静默回退内存会让限流与播放去重变成每实例一份
     * （等效放宽 N 倍）、验证码与两步验证退回单实例语义，多实例下是没人看得见的数据分叉，
     * 所以直接在启动阶段失败，把「单机跑请把 open-redis 改回 false」写进报错里
     */
    private void pingOrAbort(RedisTemplate<String, Object> template) {
        try {
            template.execute((RedisCallback<String>) connection -> connection.ping());
            log.info("Redis 连接校验通过");
        } catch (Exception e) {
            throw new IllegalStateException(
                    "tiktube.open-redis=true 但 Redis 连不上（" + e.getMessage()
                            + "）。多实例部署下静默回退内存会造成限流、验证码、两步验证各自为政，已中止启动；"
                            + "单机运行请把 tiktube.open-redis 改回 false", e);
        }
    }
}
