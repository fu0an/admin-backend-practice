package com.example.adminbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.adminbackend.dto.LoginUser;
import com.example.adminbackend.entity.User;

import java.util.List;
import java.util.Set;

public interface UserService extends IService<User> {
    // 登录方法
    User login(String username, String password);

    // 新增用户（密码自动加密 + 校验用户名唯一）
    boolean addUser(User user);

    // 获取登录用户信息（优先走 Redis 缓存）
    LoginUser getLoginUser(Long userId);

    // 从数据库构建登录用户信息并写入缓存
    LoginUser buildLoginUser(Long userId);

    // 查询用户拥有的角色ID
    List<Long> getRoleIdsByUserId(Long userId);

    // 查询用户拥有的权限标识
    Set<String> getPermsByUserId(Long userId);

    // 分配角色
    void assignRoles(Long userId, List<Long> roleIds);

    // 重置密码
    boolean resetPassword(Long userId, String newPassword);

    // 新增用户并分配角色
    boolean addUserWithRoles(User user);

    // 修改用户并同步角色
    boolean updateUserWithRoles(User user);

    // 清除登录缓存
    void clearUserCache(Long userId);

    // 清除全部登录缓存（角色/权限变更后调用）
    void clearAllLoginUserCache();
}
