package com.farm.product.auth;

import com.farm.product.service.LoginService;
import com.farm.product.common.Result;
import com.farm.product.dto.CodeRequest; // 新增导入
import com.farm.product.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegisterController {

    private final LoginService loginService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            loginService.register(registerRequest);
            return Result.build(200, "注册成功" , null);
        } catch (RuntimeException e) {
            log.error("注册失败：", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 注册验证码发送接口（使用专用DTO，仅校验手机号）
     */
    @PostMapping("/register/send-code")
    public Result<Void> sendRegisterCode(@Valid @RequestBody CodeRequest request) {
        try {
            loginService.sendRegisterCode(request); // 调用CodeRequest重载方法
            return Result.build(200, "验证码发送成功", null);
        } catch (RuntimeException e) {
            log.error("发送验证码失败：", e);
            return Result.error(e.getMessage());
        }
    }
}