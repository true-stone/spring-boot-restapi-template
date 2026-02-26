package com.example.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Spring의 @Async 기능을 위한 비동기 처리 설정을 구성합니다.
 */
@Configuration
@EnableAsync // Spring의 비동기 기능 활성화
public class AsyncConfig {

    /**
     * 비동기 작업을 처리할 커스텀 스레드 풀을 정의합니다.
     * Bean 이름을 "taskExecutor"로 지정하면 Spring이 @Async 사용 시 이 Executor를 기본으로 사용합니다.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 기본적으로 실행 대기 중인 스레드 수
        executor.setCorePoolSize(5); 
        // 동시에 동작하는 최대 스레드 수
        executor.setMaxPoolSize(10); 
        // CorePool이 모두 사용 중일 때 대기하는 큐의 크기
        executor.setQueueCapacity(100); 
        // 스레드 이름 접두사 (로그 분석 시 유용)
        executor.setThreadNamePrefix("Async-"); 
        
        executor.initialize();
        return executor;
    }
}
