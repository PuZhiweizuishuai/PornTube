package com.buguagaoshu.tiktube.repository.impl;

import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.repository.CountLimitRepository;
import com.buguagaoshu.tiktube.repository.RedisRepository;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-11
 * CountLimitRepository 的 Redis 实现
 */
@Repository("countLimitRedisRepository")
public class CountLimitRedisRepositoryImpl implements CountLimitRepository {

    private final RedisRepository redisRepository;
    
    // Redis 键前缀
    private static final String LOGIN_ATTEMPTS_KEY_PREFIX = "login:attempts:";
    private static final String LOGIN_LOCK_TIME_KEY_PREFIX = "login:locktime:";
    private static final String EMAIL_ATTEMPTS_KEY_PREFIX = "email:attempts:";
    private static final String EMAIL_FIRST_TIME_KEY_PREFIX = "email:firsttime:";
    
    // 邮件发送限制参数
    private static final int MAX_EMAIL_SENDS = 3;
    private static final int LIMIT_HOURS = 6;

    public CountLimitRedisRepositoryImpl(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public boolean allowLogin(String key) {
        // 检查是否被锁定
        String lockTimeKey = LOGIN_LOCK_TIME_KEY_PREFIX + key;
        Object lockTimeObj = redisRepository.get(lockTimeKey);
        
        if (lockTimeObj != null) {
            long lockTime = Long.parseLong(lockTimeObj.toString());
            // 检查锁定是否过期
            if (lockTime > System.currentTimeMillis()) {
                return false;
            } else {
                // 锁定已过期，删除锁定记录和尝试次数
                redisRepository.del(lockTimeKey);
                redisRepository.del(LOGIN_ATTEMPTS_KEY_PREFIX + key);
                return true;
            }
        }
        return true;
    }

    @Override
    public void recordFailedAttempt(String key) {
        String attemptsKey = LOGIN_ATTEMPTS_KEY_PREFIX + key;
        // 增加失败次数
        long attempts = redisRepository.incr(attemptsKey, 1);

        // 仅在首次失败（或丢失 TTL）时设置过期时间，避免每次失败都把窗口往后推
        if (attempts == 1 || redisRepository.getExpire(attemptsKey) < 0) {
            redisRepository.expire(attemptsKey, WebConstant.LOCK_DURATION * 60);
        }

        // 如果达到最大尝试次数，设置锁定
        if (attempts >= WebConstant.MAX_ATTEMPTS) {
            String lockTimeKey = LOGIN_LOCK_TIME_KEY_PREFIX + key;
            long lockExpiryTime = System.currentTimeMillis() + WebConstant.LOCK_DURATION * 60 * 1000;
            redisRepository.set(lockTimeKey, String.valueOf(lockExpiryTime), WebConstant.LOCK_DURATION * 60);
        }
    }

    @Override
    public void recordSuccess(String key) {
        // 登录成功，删除尝试记录和锁定记录
        redisRepository.del(LOGIN_ATTEMPTS_KEY_PREFIX + key);
        redisRepository.del(LOGIN_LOCK_TIME_KEY_PREFIX + key);
    }

    @Override
    public void clearAccount(String account) {
        // key 形如 前缀 + ip|账号，按账号后缀匹配所有来源 IP
        String suffix = "*" + LOGIN_KEY_SEPARATOR + escapeGlob(account);
        Set<String> keysToDelete = new HashSet<>(redisRepository.scan(LOGIN_ATTEMPTS_KEY_PREFIX + suffix));
        keysToDelete.addAll(redisRepository.scan(LOGIN_LOCK_TIME_KEY_PREFIX + suffix));
        if (!keysToDelete.isEmpty()) {
            redisRepository.del(keysToDelete.toArray(new String[0]));
        }
    }

    /**
     * 转义 Redis key 匹配语法中的通配字符
     */
    private static String escapeGlob(String value) {
        StringBuilder builder = new StringBuilder(value.length() + 8);
        for (char c : value.toCharArray()) {
            if (c == '*' || c == '?' || c == '[' || c == '\\') {
                builder.append('\\');
            }
            builder.append(c);
        }
        return builder.toString();
    }

    @Override
    public int getRemainingAttempts(String key) {
        String attemptsKey = LOGIN_ATTEMPTS_KEY_PREFIX + key;
        Object attemptsObj = redisRepository.get(attemptsKey);
        
        if (attemptsObj != null) {
            int attempts = Integer.parseInt(attemptsObj.toString());
            return Math.max(0, WebConstant.MAX_ATTEMPTS - attempts);
        }
        return WebConstant.MAX_ATTEMPTS;
    }

    @Override
    public boolean allowSendEmail(String key) {
        return !isEmailLimited(key);
    }

    @Override
    public void recordEmailSent(String key) {
        String attemptsKey = EMAIL_ATTEMPTS_KEY_PREFIX + key;
        String firstTimeKey = EMAIL_FIRST_TIME_KEY_PREFIX + key;
        
        // 检查是否是第一次发送
        if (redisRepository.get(firstTimeKey) == null) {
            // 记录第一次发送时间
            long currentTimeMillis = System.currentTimeMillis();
            redisRepository.set(firstTimeKey, String.valueOf(currentTimeMillis));
            // 设置过期时间
            redisRepository.expire(firstTimeKey, LIMIT_HOURS * 60 * 60);
        }
        
        // 增加发送次数
        redisRepository.incr(attemptsKey, 1);
        // 设置过期时间（如果没有设置过）
        redisRepository.expire(attemptsKey, LIMIT_HOURS * 60 * 60);
    }

    @Override
    public int getRemainingEmailSendAttempts(String key) {
        // 如果已经限制，返回0
        if (isEmailLimited(key)) {
            return 0;
        }
        
        String attemptsKey = EMAIL_ATTEMPTS_KEY_PREFIX + key;
        Object attemptsObj = redisRepository.get(attemptsKey);
        
        if (attemptsObj != null) {
            int attempts = Integer.parseInt(attemptsObj.toString());
            return Math.max(0, MAX_EMAIL_SENDS - attempts);
        }
        
        return MAX_EMAIL_SENDS;
    }
    
    /**
     * 检查邮件发送是否受限
     * @param key 邮箱或IP地址
     * @return 是否受限
     */
    private boolean isEmailLimited(String key) {
        String attemptsKey = EMAIL_ATTEMPTS_KEY_PREFIX + key;
        String firstTimeKey = EMAIL_FIRST_TIME_KEY_PREFIX + key;
        
        Object firstTimeObj = redisRepository.get(firstTimeKey);
        Object attemptsObj = redisRepository.get(attemptsKey);
        
        // 如果没有记录，则不限制
        if (firstTimeObj == null || attemptsObj == null) {
            return false;
        }
        
        int attempts = Integer.parseInt(attemptsObj.toString());
        
        // 检查是否超过最大发送次数
        return attempts >= MAX_EMAIL_SENDS;
    }
}