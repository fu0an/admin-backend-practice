package com.example.adminbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.adminbackend.entity.User;
import com.example.adminbackend.mapper.UserMapper;
import com.example.adminbackend.service.UserService;
import com.example.adminbackend.util.PasswordUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(String username, String password) {
        // 1. 只根据用户名查询（不再带密码）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = getOne(wrapper);

        // 用户不存在
        if (user == null) {
            return null;
        }

        // 2. 使用 BCrypt 加密比对密码
        boolean isPasswordOk = PasswordUtil.match(password, user.getPassword());
        return isPasswordOk ? user : null;
    }

    /**
     * 新增用户（密码自动加密）
     */
    @Override
    @CacheEvict(value = "user", allEntries = true)
    public boolean addUser(User user) {
        // 明文密码加密
        String encodePwd = PasswordUtil.encode(user.getPassword());
        user.setPassword(encodePwd);
        // 保存到数据库
        return save(user);
    }

    @Cacheable(value = "user", key = "#id")
    public User getById(Long id) {
        return super.getById(id);
    }

    @Cacheable(value = "user", key = "'list'")
    public java.util.List<User> list() {
        return super.list();
    }

    @CachePut(value = "user", key = "#entity.id")
    @CacheEvict(value = "user", key = "'list'")
    public boolean updateById(User entity) {
        return super.updateById(entity);
    }

    @CacheEvict(value = "user", allEntries = true)
    public boolean removeById(Long id) {
        return super.removeById(id);
    }
}