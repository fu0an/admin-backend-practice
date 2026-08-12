package com.example.adminbackend.config;

import com.example.adminbackend.interceptor.JwtAuthInterceptor;
import com.example.adminbackend.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserService userService;

    public WebMvcConfig(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtAuthInterceptor(userService))
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/user/logout", "/error");
    }
}
