package com.example.api.service;

import com.example.api.repository.CouponRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    public void 한번만응모() {
        applyService.apply(1L);

        final long count = couponRepository.count();
        Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    public void 여러명응모() throws InterruptedException {
        int threadCount = 1000;
        final ExecutorService executorService = Executors.newFixedThreadPool(32);
        final CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    applyService.apply(userId);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        Thread.sleep(10_000);
        final long count = couponRepository.count();
        Assertions.assertThat(count).isEqualTo(100);
    }

    @Test
    public void 한명당_한개의쿠폰만_발급() throws InterruptedException {
        int threadCount = 1000;
        final ExecutorService executorService = Executors.newFixedThreadPool(32);
        final CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    applyService.apply(1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        Thread.sleep(10_000);
        final long count = couponRepository.count();
        Assertions.assertThat(count).isEqualTo(1);
    }
}