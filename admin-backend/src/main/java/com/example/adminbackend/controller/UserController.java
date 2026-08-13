package com.example.adminbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.adminbackend.annotation.Log;
import com.example.adminbackend.annotation.RequirePermission;
import com.example.adminbackend.common.BusinessException;
import com.example.adminbackend.common.Result;
import com.example.adminbackend.common.ResultCodeEnum;
import com.example.adminbackend.context.LoginUserContext;
import com.example.adminbackend.entity.User;
import com.example.adminbackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/user")
public class UserController {

    @Resource
    private UserService userService;

    @RequirePermission("system:user:list")
    @Log("分页查询用户")
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String username,
                       @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), User::getUsername, username);
        wrapper.eq(status != null, User::getStatus, status);
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> page = userService.page(new Page<>(pageNum, pageSize), wrapper);
        // 不返回密码
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(page);
    }

    @RequirePermission("system:user:list")
    @Log("查询用户详情")
    @GetMapping("/{id}")
    public Result getOne(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.DATA_NOT_EXIST);
        }
        user.setPassword(null);
        user.setRoleIds(userService.getRoleIdsByUserId(id));
        return Result.success(user);
    }

    @RequirePermission("system:user:add")
    @Log("新增用户")
    @PostMapping
    public Result add(@RequestBody User user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR);
        }
        userService.addUserWithRoles(user);
        return Result.success();
    }

    @RequirePermission("system:user:update")
    @Log("修改用户")
    @PutMapping
    public Result update(@RequestBody User user) {
        userService.updateUserWithRoles(user);
        return Result.success();
    }

    @RequirePermission("system:user:delete")
    @Log("删除用户")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        if (LoginUserContext.getUserId().equals(id)) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR);
        }
        userService.removeById(id);
        userService.clearUserCache(id);
        return Result.success();
    }

    @RequirePermission("system:user:resetPwd")
    @Log("重置密码")
    @PutMapping("/resetPwd")
    public Result resetPwd(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(String.valueOf(body.get("id")));
        String password = (String) body.get("password");
        if (!StringUtils.hasText(password)) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR);
        }
        userService.resetPassword(id, password);
        return Result.success();
    }

    @RequirePermission("system:user:assignRole")
    @Log("分配角色")
    @PutMapping("/assignRole")
    public Result assignRole(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(String.valueOf(body.get("userId")));
        List<?> roleIdsRaw = (List<?>) body.get("roleIds");
        List<Long> roleIds = roleIdsRaw == null ? null :
                roleIdsRaw.stream().map(r -> Long.valueOf(String.valueOf(r))).toList();
        userService.assignRoles(userId, roleIds);
        return Result.success();
    }
}
