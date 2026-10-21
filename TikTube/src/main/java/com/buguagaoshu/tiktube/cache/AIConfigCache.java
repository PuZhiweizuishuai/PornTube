package com.buguagaoshu.tiktube.cache;

import com.buguagaoshu.tiktube.entity.AiConfigEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-17
 */
@Slf4j
public class AIConfigCache {
    public static volatile Map<Long, AiConfigEntity> aiConfigCache = new HashMap<>();


    public static volatile Map<Long, List<AiConfigEntity>> typeAICache = new HashMap<>();


    public static AiConfigEntity getTypeConfig(long type, Map<Long, Long> cacheUseToken) {
        List<AiConfigEntity> list = typeAICache.get(type);
        if (list == null) {
            log.error("无法获取类型为 {} 的 AI 配置，请检查配置。", type);
            return null;
        }
        
        // 遍历列表中的所有配置
        for (AiConfigEntity config : list) {
            // 不限量 TOKEN
            if (config.getMaxTokens() == null || config.getMaxTokens() == 0) {
                return config;
            }
            Long l = cacheUseToken.get(config.getId());
            if (l == null) {
                l = 0L;
            }
            if ((config.getUseTokens() + l) < config.getMaxTokens()) {
                // 如果useTokens为null或maxTokens为null或useTokens小于maxTokens，则返回该配置
                return config;
            }
        }
        log.error("无法获取类型为 {} 的 AI 配置，请检查配置。 可能原因为 Token 已用尽！", type);
        // 如果所有配置的useTokens都超过了maxTokens，则返回null
        return null;
    }
}
