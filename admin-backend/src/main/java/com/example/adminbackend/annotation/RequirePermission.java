package com.example.adminbackend.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解：标注在 Controller 方法上，要求当前登录用户拥有指定权限标识
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    String value();
}
