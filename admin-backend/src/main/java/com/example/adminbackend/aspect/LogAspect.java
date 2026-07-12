package com.example.adminbackend.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("@annotation(com.example.adminbackend.annotation.Log)")
    public void logPointcut() {
    }

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.example.adminbackend.annotation.Log logAnnotation = method.getAnnotation(com.example.adminbackend.annotation.Log.class);

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        String operation = logAnnotation.value().isEmpty() ? methodName : logAnnotation.value();
        String requestUrl = request.getRequestURL().toString();
        String requestMethod = request.getMethod();
        Object[] args = joinPoint.getArgs();
        String requestParams = Arrays.toString(args);

        log.info("========== 开始执行操作 ==========");
        log.info("操作: {}", operation);
        log.info("请求URL: {}", requestUrl);
        log.info("请求方法: {}", requestMethod);
        log.info("类名: {}", className);
        log.info("方法名: {}", methodName);
        log.info("请求参数: {}", requestParams);

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;

        log.info("返回结果: {}", result);
        log.info("执行时间: {} ms", executeTime);
        log.info("========== 操作执行完毕 ==========");

        return result;
    }
}
