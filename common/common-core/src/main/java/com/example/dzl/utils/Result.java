package com.example.dzl.utils;

import lombok.Data;

/**
 * 统一接口返回结果类（泛型版）
 * @author 光的代言人
 * @param <T> 返回数据的类型
 */
@Data
public class Result<T> {
    /**
     * 状态码，200为成功，非200为失败
     */
    private Integer code;
    /**
     * 提示信息（成功/失败的描述）
     */
    private String msg;
    /**
     * 返回数据（成功时返回业务数据，失败时可为null）
     */
    private T data; // 替换Object为泛型T

    // 私有化构造器，仅通过静态方法创建实例，规范使用
    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 无参构造（默认成功）
     */
    private Result() {
        this.code = 200;
        this.msg = "操作成功";
    }

    /**
     * 仅指定状态码和提示信息（无数据）
     */
    private Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    // ========== 静态构建方法（泛型版） ==========

    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>();
    }

    /**
     * 成功返回（带数据）
     * @param data 业务数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（自定义提示信息+数据）
     * @param msg  提示信息
     * @param data 业务数据
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    /**
     * 失败返回（指定状态码+提示信息，无数据）
     * @param code 状态码
     * @param msg  错误信息
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 失败返回（指定状态码+提示信息+错误数据）
     * @param code 状态码
     * @param msg  错误信息
     * @param data 错误详情数据
     */
    public static <T> Result<T> error(Integer code, String msg, T data) {
        return new Result<>(code, msg, data);
    }
}