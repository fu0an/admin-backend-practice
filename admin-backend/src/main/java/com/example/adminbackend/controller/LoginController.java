package com.example.adminbackend.controller;

import com.example.adminbackend.annotation.Log;
import com.example.adminbackend.common.BusinessException;
import com.example.adminbackend.common.Result;
import com.example.adminbackend.common.ResultCodeEnum;
import com.example.adminbackend.dto.LoginUser;
import com.example.adminbackend.entity.User;
import com.example.adminbackend.service.UserService;
import com.example.adminbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private UserService userService;

    // 登录接口：前端传 username 和 password
    @Log("用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        User loginUser = userService.login(user.getUsername(), user.getPassword());

        if (loginUser == null) {
            throw new BusinessException(ResultCodeEnum.LOGIN_ERROR);
        }

        // 登录成功 → 生成 JWT token
        String token = JwtUtil.createToken(loginUser.getId());

        // 构建并缓存登录用户信息（角色 + 权限）到 Redis
        userService.buildLoginUser(loginUser.getId());

        // 返回 token 给前端（vue-element-admin 必须这样返回）
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        return Result.success(map);
    }

    // 获取用户信息（前端登录后自动调用）
    @Log("获取用户信息")
    @GetMapping("/info")
    public Result getInfo(@RequestHeader("X-Token") String token) {
        if (!JwtUtil.verifyToken(token)) {
            throw new BusinessException(ResultCodeEnum.TOKEN_INVALID);
        }

        Long userId = JwtUtil.getUserIdByToken(token);
        LoginUser loginUser = userService.getLoginUser(userId);
        if (loginUser == null) {
            throw new BusinessException(ResultCodeEnum.TOKEN_INVALID);
        }

        Map<String, Object> info = new HashMap<>();
        info.put("roles", loginUser.getRoles());
        info.put("permissions", loginUser.getPermissions());
        info.put("name", loginUser.getNickname());
        info.put("avatar", loginUser.getAvatar());
        info.put("introduction", "");
        return Result.success(info);
    }

    // 登出接口
    @Log("用户登出")
    @PostMapping("/logout")
    public Result logout(@RequestHeader(value = "X-Token", required = false) String token) {
        if (token != null && JwtUtil.verifyToken(token)) {
            Long userId = JwtUtil.getUserIdByToken(token);
            userService.clearUserCache(userId);
        }
        return Result.success("登出成功");
    }
}
