package com.farm.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.farm.product.common.JwtUtil;
import com.farm.product.dto.CodeRequest;
import com.farm.product.dto.LoginRequest;
import com.farm.product.dto.RegisterRequest;
import com.farm.product.dto.ResetPasswordRequest;
import com.farm.product.entity.User;
import com.farm.product.mapper.UserMapper;
import com.farm.product.service.LoginService;
import com.farm.product.service.UserService;
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

@Slf4j
@Service
public class LoginServiceImpl extends ServiceImpl<UserMapper, User> implements LoginService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private RedisTemplate<String, String> redisTemplate; // Redis用于存储验证码

    // ========== 验证码常量配置（统一管理） ==========
    /** 注册验证码 Redis Key 前缀 */
    private static final String REGISTER_CODE_PREFIX = "farm:user:register:code:";
    /** 忘记密码验证码 Redis Key 前缀（规范命名） */
    private static final String FORGET_PWD_CODE_PREFIX = "farm:user:forget_pwd:code:";
    /** 验证码有效期（分钟） */
    private static final int CODE_EXPIRE_MINUTES = 5;
    /** 验证码长度 */
    private static final int CODE_LENGTH = 6;

    /**
     * 用户登录核心逻辑（手机号+密码登录）
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest loginRequest) {
        // 1. 校验角色值有效性（Integer 类型直接校验）
        Integer role = loginRequest.getRole();
        if (role == null || role < 0 || role > 2) {
            throw new RuntimeException("无效的角色类型");
        }

        // 2. 校验手机号非空
        String phone = loginRequest.getPhone();
        if (!StringUtils.hasText(phone)) { // 优化：使用Spring工具类校验非空
            throw new RuntimeException("手机号不能为空");
        }

        // 3. 根据手机号查询用户
        User user = getUserByPhone(phone);
        if (user == null) {
            throw new RuntimeException("手机号未注册");
        }

        // 4. 校验账号状态（0-禁用，1-正常）
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 5. 校验角色是否匹配
        if (!role.equals(user.getRole())) {
            throw new RuntimeException("角色类型不匹配，请选择正确的登录角色");
        }

        // 6. 校验密码（BCrypt解密比对）
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 7. 更新最后登录时间
        userMapper.updateLastLoginTime(user.getUserId(), LocalDateTime.now());

        // 8. 生成JWT令牌（载荷包含用户ID、手机号、角色）
        String token = jwtUtil.generateToken(
                user.getUserId().toString(),
                user.getPhone(),
                user.getRole().toString()
        );

        // 9. 构建响应数据
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);

        // 构建用户信息VO
        LoginResponse.UserInfoVO userInfoVO = new LoginResponse.UserInfoVO();
        userInfoVO.setUserId(user.getUserId());
        userInfoVO.setUsername(user.getPhone());
        userInfoVO.setAvatar(user.getAvatar());
        userInfoVO.setRole(user.getRole());
        // 设置角色名称
        userInfoVO.setRoleName(switch (user.getRole()) {
            case 0 -> "普通用户";
            case 1 -> "农户";
            case 2 -> "管理员";
            default -> "未知角色";
        });

        loginResponse.setUserInfo(userInfoVO);
        return loginResponse;
    }

    /**
     * 用户注册核心逻辑（手机号+密码+验证码注册）
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest registerRequest) {
        // 1. 基础参数校验（新增username/email校验）
        Assert.notNull(registerRequest, "注册参数不能为空");
        String username = registerRequest.getUsername(); // 新增：获取前端用户名
        String phone = registerRequest.getPhone();
        String email = registerRequest.getEmail(); // 新增：获取前端邮箱
        String password = registerRequest.getPassword();
        String confirmPassword = registerRequest.getConfirmPassword();
        Integer role = registerRequest.getRole();
        String code = registerRequest.getCode();

        // 2. 新增：用户名校验
        if (!StringUtils.hasText(username) || !username.matches("^[a-zA-Z0-9]{4,20}$")) {
            throw new RuntimeException("用户名仅支持字母和数字，长度4-20位");
        }
        // 新增：用户名唯一性校验
        if (getUserByUsername(username) != null) {
            throw new RuntimeException("用户名已被占用");
        }

        // 3. 新增：邮箱格式校验（允许为空，若数据库已设为NULL）
        if (StringUtils.hasText(email) && !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new RuntimeException("请输入有效的电子邮箱");
        }

        // 4. 原有手机号/验证码/密码/角色校验逻辑不变
        if (!StringUtils.hasText(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("请输入有效的手机号");
        }
        if (!StringUtils.hasText(code)) {
            throw new RuntimeException("验证码不能为空");
        }
        String registerCodeKey = REGISTER_CODE_PREFIX + phone;
        String redisCode = redisTemplate.opsForValue().get(registerCodeKey);
        if (redisCode == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!redisCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        if (!StringUtils.hasText(password) || !password.equals(confirmPassword)) {
            throw new RuntimeException("两次密码输入不一致");
        }
        // 统一密码规则为8-20位（匹配前端）
        if (!password.matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$")) {
            throw new RuntimeException("密码必须包含字母和数字，长度8-20位");
        }
        if (role == null || role < 0 || role > 2) {
            throw new RuntimeException("无效的角色类型");
        }
        if (existsByPhone(phone)) {
            throw new RuntimeException("手机号已被注册");
        }

        // 5. 构建用户实体（补全email/username）
        User user = new User();
        user.setUsername(username); // 使用前端传递的用户名（不再默认手机号）
        user.setPhone(phone);
        user.setEmail(email); // 赋值邮箱（允许为null）
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(1);
        user.setAvatar("/default-avatar.png");
        user.setRegisterTime(LocalDateTime.now());
        user.setLastLoginTime(null);

        // 6. 插入数据库
        boolean saveSuccess = save(user);
        if (!saveSuccess) {
            throw new RuntimeException("注册失败，请重试");
        }

        redisTemplate.delete(registerCodeKey);
        log.info("注册成功（用户名：{}，手机号：{}）", username, maskPhone(phone));
    }

    public void sendRegisterCode(CodeRequest request) {
        String phone = request.getPhone();
        // 复用原有手机号校验逻辑
        if (!StringUtils.hasText(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("请输入有效的手机号");
        }
        if (existsByPhone(phone)) {
            throw new RuntimeException("手机号已被注册");
        }
        String code = generateRandomCode();
        String redisKey = REGISTER_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.info("注册验证码已存储Redis（手机号：{}）", maskPhone(phone));
    }

    /**
     * 发送注册验证码（适配前端POST传对象）
     */
    public void sendRegisterCode(RegisterRequest request) {
        String phone = request.getPhone();
        if (!StringUtils.hasText(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("请输入有效的手机号");
        }
        if (existsByPhone(phone)) {
            throw new RuntimeException("手机号已被注册");
        }
        String code = generateRandomCode();
        String redisKey = REGISTER_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.info("注册验证码已存储Redis（Key：{}，有效期{}分钟）", redisKey, CODE_EXPIRE_MINUTES);
    }

    /**
     * 发送忘记密码验证码（字符串参数版，原有逻辑）
     */
    public void sendForgetPwdCode(String phone) {
        // 1. 校验手机号非空+格式
        if (!StringUtils.hasText(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("请输入有效的手机号");
        }

        // 2. 校验手机号是否已注册
        if (!existsByPhone(phone)) {
            throw new RuntimeException("手机号未注册");
        }

        // 3. 生成6位随机验证码（优化生成逻辑，避免重复）
        String code = generateRandomCode();
        log.info("生成忘记密码验证码：{}，手机号：{}", code, maskPhone(phone));

        // 4. 存储验证码到Redis（规范前缀 + 有效期）
        String redisKey = FORGET_PWD_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(
                redisKey,
                code,
                CODE_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );
        log.info("忘记密码验证码已存储到Redis，Key：{}，有效期：{}分钟", redisKey, CODE_EXPIRE_MINUTES);

        // 移除短信发送逻辑，直接返回成功
        log.info("验证码生成并存储成功（手机号：{}），无需发送短信", maskPhone(phone));
    }

    /**
     * 发送忘记密码验证码（适配前端POST传对象）
     * 重载方法：接收 ResetPasswordRequest 对象
     */
    public void sendForgetPwdCode(ResetPasswordRequest request) {
        String phone = request.getPhone();
        this.sendForgetPwdCode(phone); // 复用原有字符串参数逻辑
    }

    /**
     * 重置密码核心逻辑（基于Redis验证码校验）
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        // 1. 基础参数校验
        Assert.notNull(request, "重置密码参数不能为空");
        String phone = request.getPhone();
        String code = request.getCode();
        String newPassword = request.getNewPassword();

        // 2. 校验手机号
        if (!StringUtils.hasText(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("请输入有效的手机号");
        }
        // 3. 校验验证码
        if (!StringUtils.hasText(code)) {
            throw new RuntimeException("验证码不能为空");
        }
        // 4. 校验新密码
        if (!StringUtils.hasText(newPassword) || !newPassword.matches("^(?=.*[a-zA-Z])(?=.*\\d).{6,16}$")) {
            throw new RuntimeException("新密码必须包含字母和数字，长度6-16位");
        }

        // 5. 校验手机号是否存在
        User user = getUserByPhone(phone);
        if (user == null) {
            throw new RuntimeException("手机号未注册");
        }

        // 6. 从Redis读取验证码并校验
        String redisKey = FORGET_PWD_CODE_PREFIX + phone;
        String redisCode = redisTemplate.opsForValue().get(redisKey);
        if (redisCode == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!redisCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        log.info("验证码校验通过，手机号：{}", maskPhone(phone));

        // 7. 加密新密码并更新
        user.setPassword(passwordEncoder.encode(newPassword));
        boolean updateSuccess = updateById(user);
        if (!updateSuccess) {
            throw new RuntimeException("密码重置失败，请重试");
        }

        // 8. 重置成功后删除Redis中的验证码（防止重复使用）
        redisTemplate.delete(redisKey);
        log.info("密码重置成功，已删除Redis中的验证码（手机号：{}）", maskPhone(phone));
    }

    /**
     * 根据手机号查询用户
     */
    public User getUserByPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 校验手机号是否已注册
     */
    private boolean existsByPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        return baseMapper.exists(queryWrapper);
    }

    /**
     * 根据用户名查询（保留兼容）
     */
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return baseMapper.selectOne(queryWrapper);
    }

    // ========== 私有工具方法 ==========
    /**
     * 生成6位随机数字验证码（优化生成逻辑，避免0开头）
     */
    private String generateRandomCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        // 第一位不为0
        code.append(random.nextInt(9) + 1);
        // 后5位为0-9
        for (int i = 1; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 手机号脱敏（138****1234），避免日志泄露敏感信息
     */
    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}