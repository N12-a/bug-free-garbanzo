package com.farm.product.service;

import com.farm.product.dto.CodeRequest;
import com.farm.product.dto.LoginRequest;
import com.farm.product.dto.RegisterRequest;
import com.farm.product.dto.ResetPasswordRequest;
import com.farm.product.vo.LoginResponse;

public interface LoginService {
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest loginRequest);
    /**
     * 用户注册
     */
    void register(RegisterRequest registerRequest);
    void sendRegisterCode(RegisterRequest request);
    /**
     * 发送忘记密码验证码
     */
    void resetPassword(ResetPasswordRequest request);
    /**
     * 重置密码
     */
    void sendForgetPwdCode(String phone);
    void sendRegisterCode(CodeRequest request);
}
