package com.farm.product.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
@Data
public class JwtUtil {
    /** JWT密钥 */
    @Value("${jwt.secret}")
    private String secret;

    /** token有效期：24小时（毫秒） */
    @Value("${jwt.expire}")
    private long expire;

    /** token前缀 */
    @Value("${jwt.prefix}")
    private String prefix;

    /** 请求头名称 */
    @Value("${jwt.header}")
    private String header;

    /** 密钥对象 */
    private SecretKey secretKey;

    /** 初始化密钥 */
    @PostConstruct
    public void init() {
        // 确保密钥长度足够（JJWT要求至少256位）
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT密钥长度不能少于32位");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     */
    public String generateToken(String userId, String username, String role) {
        // 构建载荷
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        // 生成token
        return Jwts.builder()
                .setClaims(claims) // 设置载荷
                .setSubject(username) // 设置主题（用户名）
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expire)) // 设置过期时间
                .signWith(secretKey) // 签名
                .compact();
    }

    /**
     * 解析token获取载荷
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从token中获取用户ID
     */
    public String getUserIdFromToken(String token) {
        return parseToken(token).get("userId", String.class);
    }

    /**
     * 从token中获取角色
     */
    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * 校验token是否过期
     */
    public boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }

    /**
     * 校验token是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}