package com.farm.product.config;

import com.farm.product.common.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 彻底关闭CSRF（REST API无需）
                .csrf(csrf -> csrf.disable())
                // 2. 关闭Session（无状态）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 3. 强化跨域配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 4. 权限规则（优先级：先放行，后拦截）
                .authorizeHttpRequests(auth -> auth
                        // 核心：放行OPTIONS预检请求（跨域必加）
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 核心：放行忘记密码所有接口（所有请求方法）
                        .requestMatchers("/api/forget-password/**").permitAll()
                        // 放行登录/注册
                        .requestMatchers("/api/login", "/api/register", "/api/register/**").permitAll()
                        // 放行Swagger
                        .requestMatchers("/api/swagger-ui/**", "/api/v3/api-docs/**").permitAll()
                        // 临时注释角色权限（先解决403，再恢复）
                        // .requestMatchers("/api/user/**").hasRole("0")
                        // .requestMatchers("/api/farmer/**").hasRole("1")
                        // .requestMatchers("/api/admin/**").hasRole("2")
                        // 其他接口需认证（临时注释，测试用）
                        // .anyRequest().authenticated()
                        // 临时：所有接口放行（仅用于测试403是否解决）
                        .anyRequest().permitAll()
                );
        // 临时注释JWT过滤器（先确认Security放行，再恢复）
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 强化跨域配置（解决跨域导致的403）
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许所有源（开发环境）
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        // 允许所有请求头
        configuration.addAllowedHeader(CorsConfiguration.ALL);
        // 允许所有请求方法
        configuration.setAllowedMethods(Collections.singletonList(CorsConfiguration.ALL));
        // 允许携带Cookie/Token
        configuration.setAllowCredentials(true);
        // 暴露响应头
        configuration.addExposedHeader("Authorization");
        // 预检请求缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径生效
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}