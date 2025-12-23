package com.farm.product.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginResponse {
    /** JWT令牌 */
    private String token;
    
    /** 用户基本信息 */
    private UserInfoVO userInfo;

    /**
     * 用户信息子VO
     */
    @Data
    public static class UserInfoVO {
        private Long userId;
        private String username;
        private String avatar;
        private Integer role; // 0-普通用户 1-农户 2-管理员
        private String roleName; // 角色名称：普通用户/农户/管理员
    }
}