package com.example.api.config;

import com.example.api.filter.HttpLoggingFilter;
import com.example.api.filter.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 *   Filter Bean이 서블릿 체인에 자동 등록되는 것을 막아(Security 체인에서만 실행),
 *   필터 이중 등록으로 인해 <code>web.ignoring()</code> 경로에서도 필터가 실행되는 문제를 방지한다.
 * </p>
 *
 * <p><b>배경</b></p>
 * <ul>
 *   <li>Spring Boot는 <code>Filter</code> 타입 Bean(<code>@Component</code>/<code>@Bean</code>)을 감지하면 기본적으로 서블릿 컨테이너 필터로 자동 등록한다.</li>
 *   <li>동시에 <code>SecurityFilterChain</code>에 <code>addFilterBefore/After</code>로 같은 필터를 추가하면 <b>이중 등록</b>이 발생할 수 있다.</li>
 *   <li>이 경우 <code>WebSecurityCustomizer</code>의 <code>web.ignoring()</code>으로 제외한 경로에서도 필터가 실행되는 등 의도와 다른 동작이 발생한다.</li>
 * </ul>
 *
 * <p><b>해결</b></p>
 * <ul>
 *   <li><code>FilterRegistrationBean#setEnabled(false)</code>로 서블릿 체인 자동 등록만 끄고,</li>
 *   <li>필터 실행/순서는 <code>SecurityFilterChain</code>에서 <code>addFilterBefore/After</code>로 명시적으로 제어한다.</li>
 * </ul>
 */
@Configuration
public class SecurityFilterRegistrationConfig {

    @Bean
    public FilterRegistrationBean<HttpLoggingFilter> loggingFilterRegistration(HttpLoggingFilter filter) {
        var bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        var bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }
}
