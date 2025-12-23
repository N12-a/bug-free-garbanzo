package com.farm.product.auth;

import com.farm.product.service.LoginService;
import com.farm.product.common.Result;
import com.farm.product.dto.ForgetPwdCodeRequest;
import com.farm.product.dto.ResetPasswordRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 忘记密码控制器
 */
@RestController
@RequestMapping("/api/forget-password")

public class ForgetPasswordController {

    private final LoginService loginService;
    // 新增：构造函数日志
    public ForgetPasswordController(LoginService loginService) {
        this.loginService = loginService;
        System.out.println("===== ForgetPasswordController 初始化成功 =====");
    }

    /**
     * 发送忘记密码验证码
     */

    @PostMapping("/send-code")
    public Result<Void> sendForgetPwdCode(@Valid @RequestBody ForgetPwdCodeRequest request) {
        try {
            loginService.sendForgetPwdCode(request.getPhone());
            return Result.build(200,"验证码发送成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            loginService.resetPassword(request);
            return Result.build(200,"密码重置成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}