package com.farm.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author fn
 * @since 2025-12-16
 */
@Getter
@Setter
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色(0:普通用户 1:农户 2:管理员)
     */
    private Integer role;

    /**
     * 状态(0:禁用 1:正常)
     */
    private Integer status;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
}
