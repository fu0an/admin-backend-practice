package com.example.adminbackend.controller;

import com.example.adminbackend.annotation.Log;
import com.example.adminbackend.common.BusinessException;
import com.example.adminbackend.common.Result;
import com.example.adminbackend.common.ResultCodeEnum;
import com.example.adminbackend.entity.User;
import com.example.adminbackend.service.UserService;
import com.example.adminbackend.util.JwtUtil;
//import com.example.adminbackend.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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
            // 抛出自定义业务异常 → 自动走全局异常处理
            throw new BusinessException(ResultCodeEnum.LOGIN_ERROR);
        }

        // 登录成功 → 生成 JWT token
        String token = JwtUtil.createToken(loginUser.getId());

        // 返回 token 给前端（vue-element-admin 必须这样返回）
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        return Result.success(map);
    }

    // 获取用户信息（前端登录后自动调用）
    @Log("获取用户信息")
    @GetMapping("/info")
    public Result getInfo(@RequestHeader("X-Token") String token) {
        // 1. 校验 token 是否有效
        if (!JwtUtil.verifyToken(token)) {
            throw new BusinessException(ResultCodeEnum.TOKEN_INVALID);
        }

        // 2. 从 token 解析出用户ID
        Long userId = JwtUtil.getUserIdByToken(token);

        // 3. 根据ID查真实用户（你后面可以改成查库）
        // 这里先用模拟数据，不影响运行
        Map<String, Object> info = new HashMap<>();
        info.put("roles", Arrays.asList("admin"));
        info.put("name", "admin");
        info.put("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");

        return Result.success(info);
    }

    // 登出接口
    @Log("用户登出")
    @PostMapping("/logout")
    public Result logout() {
        return Result.success("登出成功");
    }
    /*临时接口，返回加密后的密码
    @GetMapping("/genPwd")
    public String genPwd(String pwd) {
        return PasswordUtil.encode(pwd);
    }
     */
}