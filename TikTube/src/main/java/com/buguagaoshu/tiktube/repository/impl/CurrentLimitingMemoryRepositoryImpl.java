package com.buguagaoshu.tiktube.repository.impl;

import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.repository.APICurrentLimitingRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-21
 * 接口限流的内存实现
 */
@Repository("currentLimitingMemoryRepository")
public class CurrentLimitingMemoryRepositoryImpl implements APICurrentLimitingRepository {

    /**
     * 访问时间限制
     * 单位秒
     * 10 秒内只能访问一次
     * */
    public final static int TIME_LIMIT = 10;
    
    /**
     * 用户访问记录
     * key: 用户ID_类型
     * value: 上次访问时间
     */
    private final Map<String, Long> visitRecords = new ConcurrentHashMap<>();

    /**
     * 记录条数上限，匿名接口以 IP 为 key 时可被伪造头无限撑大内存
     */
    private static final int MAX_RECORDS = 20000;
    
    @Override
    public boolean hasVisit(int type, long userId) {
        String key = userId + "_" + type;
        return hasVisitByKey(key);
    }

    @Override
    public boolean hasVisit(int type, String key) {
        String s = key + "_" + type;
        return hasVisitByKey(s);
    }

    public boolean hasVisitByKey(String key) {
        long currentTime = System.currentTimeMillis() / 1000;
        // compute 保证「读取-判断-写入」对同一个 key 是原子操作，避免并发请求同时放行
        boolean[] allowed = new boolean[1];
        visitRecords.compute(key, (recordKey, lastVisitTime) -> {
            boolean allow = lastVisitTime == null || (currentTime - lastVisitTime) >= TIME_LIMIT;
            allowed[0] = allow;
            // 被拒绝时不刷新上次访问时间，否则不断重试会把限制窗口一直往后推
            return allow ? currentTime : lastVisitTime;
        });
        if (visitRecords.size() > MAX_RECORDS) {
            purgeExpired(currentTime);
        }
        return allowed[0];
    }

    /**
     * 清理已过期的记录，清完仍然超限就整体清空——宁可短暂放宽限流也不能耗尽内存
     */
    private void purgeExpired(long currentTime) {
        visitRecords.entrySet().removeIf(record -> (currentTime - record.getValue()) >= TIME_LIMIT);
        if (visitRecords.size() > MAX_RECORDS) {
            visitRecords.clear();
        }
    }

    @Override
    public int getRemainingTime(int type, long userId) {
        String key = userId + "_" + type;
        long currentTime = System.currentTimeMillis() / 1000;
        
        // 获取上次访问时间
        Long lastVisitTime = visitRecords.get(key);
        
        // 如果是首次访问或者已经超过限制时间，则剩余时间为0
        if (lastVisitTime == null || (currentTime - lastVisitTime) >= TIME_LIMIT) {
            return 0;
        }
        
        // 计算剩余时间
        return (int) (TIME_LIMIT - (currentTime - lastVisitTime));
    }
    
    @Override
    public void clearUserRecord(long userId) {
        String articleKey = userId + "_" + WebConstant.SIMPLE_CURRENT_LIMITING_TYPE_ARTICLE;
        String commentKey = userId + "_" + WebConstant.SIMPLE_CURRENT_LIMITING_TYPE_COMMENT;
        String danmakuKey = userId + "_" + WebConstant.SIMPLE_CURRENT_LIMITING_TYPE_DANMAKU;
        
        visitRecords.remove(articleKey);
        visitRecords.remove(commentKey);
        visitRecords.remove(danmakuKey);
    }

    @Override
    public void clearAllRecords() {
        visitRecords.clear();
    }
}