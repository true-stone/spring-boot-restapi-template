package com.example.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MyAsyncService {

    @Async("taskExecutor")
    public CompletableFuture<String> doSomethingAsync() {
        try {
            Thread.sleep(3000); // 3초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return CompletableFuture.completedFuture("Async task finished!");
    }

    @Async("taskExecutor")
    public void fireAndForget() {
        try {
            log.info("[fireAndForget] 백그라운드 작업 시작");
            Thread.sleep(3000); // 3초 대기
            log.info("[fireAndForget] 백그라운드 작업 완료");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
