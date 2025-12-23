package com.farm.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.farm.product.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author fn
 * @since 2025-12-16
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 更新最后登录时间
     */
    @Update("UPDATE user SET last_login_time = #{lastLoginTime} WHERE user_id = #{userId}")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("lastLoginTime") LocalDateTime lastLoginTime);

}
