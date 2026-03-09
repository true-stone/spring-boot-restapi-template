package com.example.api.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Spring의 @Async 기능을 위한 비동기 처리 설정을 구성합니다.
 */
@Configuration
@EnableAsync
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
        executor.setQueueCapacity(30);
        // 스레드 이름 접두사 (로그 분석 시 유용)
        executor.setThreadNamePrefix("app-async-");
        // 애플리케이션 종료 시 현재 진행 중인 작업이 끝날 때까지 기다릴지 여부
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // shutdown 시 최대 10초까지 작업 종료를 기다리고, 그 이후에는 종료 절차를 진행
        executor.setAwaitTerminationSeconds(10);
        // 부모 스레드의 MDC 값을 비동기 스레드로 전달하기 위한 데코레이터(traceId 같은 로깅 컨텍스트가 @Async 내부에서도 유지되도록 함)
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }
}
