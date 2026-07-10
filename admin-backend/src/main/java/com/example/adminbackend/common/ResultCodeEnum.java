package com.example.adminbackend.common;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "操作成功"),
    ERROR(500, "系统异常"),

    // 登录相关
    LOGIN_ERROR(201, "账号或密码错误"),
    TOKEN_INVALID(202, "登录已过期，请重新登录"),
    NO_LOGIN(203, "请先登录"),

    // 业务异常
    PARAM_ERROR(301, "参数错误"),
    DATA_NOT_EXIST(302, "数据不存在"),
    USER_EXIST(303, "用户已存在");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}