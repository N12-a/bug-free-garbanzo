package com.farm.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.farm.product.common.JwtUtil;
import com.farm.product.dto.LoginRequest;
import com.farm.product.dto.RegisterRequest;
import com.farm.product.dto.ResetPasswordRequest;
import com.farm.product.entity.User;
import com.farm.product.mapper.UserMapper;
import com.farm.product.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.farm.product.vo.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author fn
 * @since 2025-12-16
 */
@Slf4j // 新增日志注解，方便排查问题
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {



}