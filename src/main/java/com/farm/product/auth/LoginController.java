package com.farm.product.auth;
import com.farm.product.service.LoginService;
import com.farm.product.common.Result;
import com.farm.product.dto.LoginRequest;
import com.farm.product.vo.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = loginService.login(loginRequest);
            return Result.success(loginResponse);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}