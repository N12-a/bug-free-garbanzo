package com.farm.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 重置密码请求
 */
@Data
public class ResetPasswordRequest {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,16}$", message = "密码必须包含字母和数字，长度6-16位")
    private String newPassword;
}