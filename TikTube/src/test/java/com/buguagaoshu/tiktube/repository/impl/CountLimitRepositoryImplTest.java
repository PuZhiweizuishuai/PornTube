package com.buguagaoshu.tiktube.repository.impl;

import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.repository.CountLimitRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CountLimitRepositoryImplTest {

    private final CountLimitRepositoryImpl repository = new CountLimitRepositoryImpl();

    private static final String ACCOUNT = "victim@example.com";

    private void lockOut(String key) {
        for (int i = 0; i < WebConstant.MAX_ATTEMPTS; i++) {
            repository.recordFailedAttempt(key);
        }
    }

    @Test
    void failuresOnlyLockTheAttackerIp() {
        String attacker = CountLimitRepository.loginKey("1.1.1.1", ACCOUNT);
        String victim = CountLimitRepository.loginKey("2.2.2.2", ACCOUNT);

        lockOut(attacker);

        assertFalse(repository.allowLogin(attacker));
        // 同一账号从其他来源登录不受影响
        assertTrue(repository.allowLogin(victim));
    }

    @Test
    void recordSuccessClearsOnlyTheCurrentIp() {
        String key = CountLimitRepository.loginKey("1.1.1.1", ACCOUNT);
        String other = CountLimitRepository.loginKey("2.2.2.2", ACCOUNT);
        lockOut(other);

        lockOut(key);
        repository.recordSuccess(key);

        assertTrue(repository.allowLogin(key));
        assertFalse(repository.allowLogin(other));
    }

    @Test
    void clearAccountUnlocksEveryIp() {
        String key = CountLimitRepository.loginKey("1.1.1.1", ACCOUNT);
        String other = CountLimitRepository.loginKey("2.2.2.2", ACCOUNT);
        lockOut(key);
        lockOut(other);

        repository.clearAccount(ACCOUNT);

        assertTrue(repository.allowLogin(key));
        assertTrue(repository.allowLogin(other));
    }

    @Test
    void loginRecordsAreBounded() throws Exception {
        for (int i = 0; i < 20500; i++) {
            repository.recordFailedAttempt(CountLimitRepository.loginKey("10.0.0." + (i % 255), "user-" + i));
        }

        int size = loginCountSize();
        assertTrue(size < 20000, "登录失败记录未被清理，当前大小：" + size);
    }

    @SuppressWarnings("unchecked")
    private int loginCountSize() throws Exception {
        Field field = CountLimitRepositoryImpl.class.getDeclaredField("loginCount");
        field.setAccessible(true);
        return ((ConcurrentMap<String, ?>) field.get(repository)).size();
    }
}
