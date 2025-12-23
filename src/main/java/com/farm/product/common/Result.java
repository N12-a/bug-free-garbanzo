package com.farm.product.common;

import lombok.Data;

/**
 * 通用接口响应结果
 */
@Data
public class Result<T> {
    /** 响应码：200成功，其他失败 */
    private Integer code;
    
    /** 响应消息 */
    private String msg;
    
    /** 响应数据 */
    private T data;

    // 成功响应
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }


    // 失败响应
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    // 自定义成功响应
    public static <T> Result<T> build(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}