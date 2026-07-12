package com.example.adminbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.adminbackend.entity.User;

public interface UserService extends IService<User> {
    // 登录方法
    User login(String username, String password);
    // 新增用户
    boolean addUser(User user);

}