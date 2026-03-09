package com.example.api.config.async;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> parentContextMap = MDC.getCopyOfContextMap();

        return () -> {
            Map<String, String> previousContextMap = MDC.getCopyOfContextMap();

            try {
                if (parentContextMap != null) {
                    MDC.setContextMap(parentContextMap);
                } else {
                    MDC.clear();
                }

                runnable.run();
            } finally {
                if (previousContextMap != null) {
                    MDC.setContextMap(previousContextMap);
                } else {
                    MDC.clear();
                }
            }
        };
    }
}
