package com.buguagaoshu.tiktube.model;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-05-11
 * 邮件发送次数限制
 */
@Getter
public class EmailSendCount {
    // 最大发送次数
    private static final int MAX_EMAIL_SENDS = 3;
    
    // 限制时间（小时）
    private static final int LIMIT_HOURS = 6;
    
    private int attempts = 0;
    private LocalDateTime firstAttemptTime = null;
    
    public synchronized void increment() {
        if (firstAttemptTime == null) {
            firstAttemptTime = LocalDateTime.now();
        }
        attempts++;
    }
    
    public synchronized void reset() {
        attempts = 0;
        firstAttemptTime = null;
    }
    
    public boolean isLimited() {
        // 如果没有发送记录，则不限制
        if (firstAttemptTime == null) {
            return false;
        }
        
        // 检查是否超过限制时间
        if (firstAttemptTime.plusHours(LIMIT_HOURS).isBefore(LocalDateTime.now())) {
            // 超过限制时间，重置计数
            reset();
            return false;
        }
        
        // 检查是否超过最大发送次数
        return attempts >= MAX_EMAIL_SENDS;
    }
    
    public int getRemainingAttempts() {
        if (isLimited()) {
            return 0;
        }
        
        // 如果没有发送记录或已过期，返回最大值
        if (firstAttemptTime == null || 
            firstAttemptTime.plusHours(LIMIT_HOURS).isBefore(LocalDateTime.now())) {
            return MAX_EMAIL_SENDS;
        }
        
        // 返回剩余次数
        return Math.max(0, MAX_EMAIL_SENDS - attempts);
    }

    /**
     * 记录是否可以丢弃（限制窗口已过或没有任何发送记录）
     */
    public synchronized boolean isExpired() {
        return firstAttemptTime == null
                || firstAttemptTime.plusHours(LIMIT_HOURS).isBefore(LocalDateTime.now());
    }
}