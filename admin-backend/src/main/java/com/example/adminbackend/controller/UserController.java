package com.example.adminbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.adminbackend.annotation.Log;
import com.example.adminbackend.common.Result;
import com.example.adminbackend.entity.User;
import com.example.adminbackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // 新增用户
    @Log("新增用户")
    @PostMapping
    public Result save(@RequestBody User user) {
        userService.addUser(user);
        return Result.success();
    }

    // 修改用户
    @Log("修改用户")
    @PutMapping
    public Result update(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    // 查询单个用户
    @Log("查询单个用户")
    @GetMapping("/{id}")
    public Result getOne(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    // 查询所有用户
    @Log("查询所有用户")
    @GetMapping
    public Result list(){
        return Result.success(userService.list());
    }

    // 删除用户
    @Log("删除用户")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    //用户分页
    @Log("用户分页查询")
    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.page(new Page<User>(pageNum, pageSize)));
    }
}