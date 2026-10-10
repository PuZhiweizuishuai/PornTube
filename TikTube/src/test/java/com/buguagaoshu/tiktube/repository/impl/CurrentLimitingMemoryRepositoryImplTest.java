package com.buguagaoshu.tiktube.repository.impl;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrentLimitingMemoryRepositoryImplTest {

    private final CurrentLimitingMemoryRepositoryImpl repository = new CurrentLimitingMemoryRepositoryImpl();

    @Test
    void secondVisitWithinWindowIsRejected() {
        assertTrue(repository.hasVisit(1, 100L));
        assertFalse(repository.hasVisit(1, 100L));
        assertTrue(repository.getRemainingTime(1, 100L) > 0);
        // 不同 key 互不影响
        assertTrue(repository.hasVisit(2, 100L));
    }

    @Test
    void concurrentVisitsAllowOnlyOne() throws Exception {
        int threads = 64;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger allowed = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    if (repository.hasVisit(3, 200L)) {
                        allowed.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(1, allowed.get());
    }

    @Test
    void recordCountIsBounded() throws Exception {
        for (int i = 0; i < 20500; i++) {
            repository.hasVisit(4, "ip-" + i);
        }

        int size = visitRecordSize();
        assertTrue(size <= 20001, "限流记录未被清理，当前大小：" + size);
    }

    @SuppressWarnings("unchecked")
    private int visitRecordSize() throws Exception {
        Field field = CurrentLimitingMemoryRepositoryImpl.class.getDeclaredField("visitRecords");
        field.setAccessible(true);
        return ((ConcurrentMap<String, Long>) field.get(repository)).size();
    }
}
