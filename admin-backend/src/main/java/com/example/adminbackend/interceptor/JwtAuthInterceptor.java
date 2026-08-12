package com.example.adminbackend.interceptor;

import com.example.adminbackend.annotation.RequirePermission;
import com.example.adminbackend.common.BusinessException;
import com.example.adminbackend.common.ResultCodeEnum;
import com.example.adminbackend.context.LoginUserContext;
import com.example.adminbackend.dto.LoginUser;
import com.example.adminbackend.service.UserService;
import com.example.adminbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证 + 权限校验拦截器
 * 认证：校验 X-Token，加载登录用户信息
 * 授权：校验方法上的 @RequirePermission 注解
 */
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public JwtAuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("X-Token");
        if (token == null || token.isEmpty() || !JwtUtil.verifyToken(token)) {
            throw new BusinessException(ResultCodeEnum.NO_LOGIN);
        }

        Long userId = JwtUtil.getUserIdByToken(token);
        LoginUser loginUser = userService.getLoginUser(userId);
        if (loginUser == null) {
            throw new BusinessException(ResultCodeEnum.NO_LOGIN);
        }

        if (handler instanceof HandlerMethod handlerMethod) {
            RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
            if (requirePermission != null) {
                boolean isAdmin = loginUser.getRoles() != null && loginUser.getRoles().contains("admin");
                boolean hasPerm = loginUser.getPermissions() != null && loginUser.getPermissions().contains(requirePermission.value());
                if (!isAdmin && !hasPerm) {
                    throw new BusinessException(ResultCodeEnum.NO_PERMISSION);
                }
            }
        }

        LoginUserContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContext.clear();
    }
}
