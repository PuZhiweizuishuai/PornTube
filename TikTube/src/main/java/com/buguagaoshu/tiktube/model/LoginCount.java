package com.buguagaoshu.tiktube.model;

import com.buguagaoshu.tiktube.config.WebConstant;
import lombok.Getter;

import java.time.LocalDateTime;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-11
 */
@Getter
public class LoginCount {
    private int attempts = 0;
    private LocalDateTime lockExpiryTime = null;
    private LocalDateTime lastAttemptTime = null;

    public synchronized void increment() {
        attempts++;
        lastAttemptTime = LocalDateTime.now();
    }

    public synchronized void lock() {
        if (attempts >= WebConstant.MAX_ATTEMPTS) {
            lockExpiryTime = LocalDateTime.now().plusMinutes(WebConstant.LOCK_DURATION);
        }
    }

    public synchronized void reset() {
        attempts = 0;
        lockExpiryTime = null;
        lastAttemptTime = null;
    }

    public synchronized boolean isLocked() {
        return lockExpiryTime != null && lockExpiryTime.isAfter(LocalDateTime.now());
    }

    /**
     * 记录是否可以丢弃
     * 锁定已到期，或者在锁定时长内没有任何失败尝试
     */
    public synchronized boolean isExpired() {
        if (lockExpiryTime != null) {
            return lockExpiryTime.isBefore(LocalDateTime.now());
        }
        return lastAttemptTime == null
                || lastAttemptTime.plusMinutes(WebConstant.LOCK_DURATION).isBefore(LocalDateTime.now());
    }
}
