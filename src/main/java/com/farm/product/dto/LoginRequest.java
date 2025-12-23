package com.farm.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotNull(message = "角色不能为空")
    private Integer role; // 0-用户，1-农户，2-管理员
}