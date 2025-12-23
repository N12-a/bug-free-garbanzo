package com.farm.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册请求参数
 */
@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,16}$", message = "密码必须包含字母和数字，长度6-16位")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotNull(message = "角色不能为空")
    private Integer role;

    private String email;

    // 可选：验证码
    private String code;
}