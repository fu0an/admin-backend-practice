package com.example.adminbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.adminbackend.annotation.Log;
import com.example.adminbackend.annotation.RequirePermission;
import com.example.adminbackend.common.BusinessException;
import com.example.adminbackend.common.Result;
import com.example.adminbackend.common.ResultCodeEnum;
import com.example.adminbackend.entity.SysRole;
import com.example.adminbackend.service.SysRoleService;
import com.example.adminbackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/role")
public class RoleController {

    @Resource
    private SysRoleService sysRoleService;
    @Resource
    private UserService userService;

    @RequirePermission("system:role:list")
    @Log("分页查询角色")
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String roleName,
                       @RequestParam(required = false) String roleKey) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName);
        wrapper.like(StringUtils.hasText(roleKey), SysRole::getRoleKey, roleKey);
        wrapper.orderByAsc(SysRole::getSort);
        return Result.success(sysRoleService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    @RequirePermission("system:role:list")
    @Log("查询全部角色")
    @GetMapping("/listAll")
    public Result listAll() {
        return Result.success(sysRoleService.listAll());
    }

    @RequirePermission("system:role:list")
    @Log("查询角色详情")
    @GetMapping("/{id}")
    public Result getOne(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        if (role == null) {
            throw new BusinessException(ResultCodeEnum.DATA_NOT_EXIST);
        }
        role.setMenuIds(sysRoleService.getMenuIdsByRoleId(id));
        return Result.success(role);
    }

    @RequirePermission("system:role:add")
    @Log("新增角色")
    @PostMapping
    public Result add(@RequestBody SysRole role) {
        Long count = sysRoleService.count(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, role.getRoleKey()));
        if (count > 0) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "角色标识已存在");
        }
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        sysRoleService.save(role);
        return Result.success();
    }

    @RequirePermission("system:role:update")
    @Log("修改角色")
    @PutMapping
    public Result update(@RequestBody SysRole role) {
        sysRoleService.updateById(role);
        userService.clearAllLoginUserCache();
        return Result.success();
    }

    @RequirePermission("system:role:delete")
    @Log("删除角色")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        if (id == 1L) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "内置角色不可删除");
        }
        sysRoleService.removeById(id);
        userService.clearAllLoginUserCache();
        return Result.success();
    }

    @RequirePermission("system:role:assignMenu")
    @Log("分配菜单")
    @PutMapping("/assignMenu")
    public Result assignMenu(@RequestBody Map<String, Object> body) {
        Long roleId = Long.valueOf(String.valueOf(body.get("roleId")));
        List<?> menuIdsRaw = (List<?>) body.get("menuIds");
        List<Long> menuIds = menuIdsRaw == null ? null :
                menuIdsRaw.stream().map(m -> Long.valueOf(String.valueOf(m))).toList();
        sysRoleService.assignMenus(roleId, menuIds);
        userService.clearAllLoginUserCache();
        return Result.success();
    }
}
