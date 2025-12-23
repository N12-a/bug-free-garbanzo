package com.farm.product.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // 定义无需JWT校验的白名单接口
    private static final List<String> WHITE_LIST = List.of(
            "/api/login",
            "/api/register",
            "/api/forget-password/"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 白名单接口直接放行，不校验JWT
        if (WHITE_LIST.stream().anyMatch(requestURI::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 原有JWT校验逻辑（保持不变）
        String token = getTokenFromRequest(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 注意：这里拼接ROLE_，和SecurityConfig中的hasRole对应
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singletonList(authority)
            );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(jwtUtil.getHeader());
        if (StringUtils.hasText(header) && header.startsWith(jwtUtil.getPrefix() + " ")) {
            return header.substring(jwtUtil.getPrefix().length() + 1);
        }
        return null;
    }
}