package com.example.adminbackend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 明文密码加密
     */
    public static String encode(String rawPwd) {
        return ENCODER.encode(rawPwd);
    }

    /**
     * 校验密码
     * @param rawPwd 前端传入明文
     * @param dbPwd 数据库加密密码
     */
    public static boolean match(String rawPwd, String dbPwd) {
        return ENCODER.matches(rawPwd, dbPwd);
    }
}