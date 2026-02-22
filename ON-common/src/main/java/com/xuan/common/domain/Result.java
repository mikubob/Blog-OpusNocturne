package com.xuan.common.domain;

import com.xuan.common.enums.ErrorCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应包装类
 * <p>
 * 所有接口统一返回此格式，确保前端能以一致的方式处理响应。
 * code 为 0 表示成功，非 0 表示失败，message 始终为面向用户的友好提示。
 *
 * @param <T> 响应数据类型
 * @author 玄〤
 * @since 2026-02-16
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应码：0-成功，非0-失败（对应 ErrorCode 中的 code）
     */
    private Integer code;

    /**
     * 响应消息（面向用户的友好提示）
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    // ==================== 成功响应 ====================

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（自定义提示语）
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // ==================== 失败响应 ====================

    /**
     * 失败响应（从 ErrorCode 枚举构造，推荐使用）
     * <p>
     * 示例：Result.error(ErrorCode.UNAUTHORIZED)
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        result.setData(null);
        return result;
    }

    /**
     * 失败响应（自定义 code + message）
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    /**
     * 失败响应（仅消息，默认使用系统异常码）
     */
    public static <T> Result<T> error(String message) {
        return error(ErrorCode.SYSTEM_ERROR.getCode(), message);
    }
}
