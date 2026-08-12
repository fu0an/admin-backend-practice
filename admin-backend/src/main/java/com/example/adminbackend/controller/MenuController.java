package com.example.adminbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.adminbackend.annotation.Log;
import com.example.adminbackend.annotation.RequirePermission;
import com.example.adminbackend.common.BusinessException;
import com.example.adminbackend.common.Result;
import com.example.adminbackend.common.ResultCodeEnum;
import com.example.adminbackend.entity.SysMenu;
import com.example.adminbackend.service.SysMenuService;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/menu")
public class MenuController {

    @Resource
    private SysMenuService sysMenuService;

    @RequirePermission("system:menu:list")
    @Log("查询菜单树")
    @GetMapping("/tree")
    public Result tree() {
        return Result.success(sysMenuService.buildTree());
    }

    @RequirePermission("system:menu:list")
    @Log("查询菜单详情")
    @GetMapping("/{id}")
    public Result getOne(@PathVariable Long id) {
        SysMenu menu = sysMenuService.getById(id);
        if (menu == null) {
            throw new BusinessException(ResultCodeEnum.DATA_NOT_EXIST);
        }
        return Result.success(menu);
    }

    @RequirePermission("system:menu:add")
    @Log("新增菜单")
    @PostMapping
    public Result add(@RequestBody SysMenu menu) {
        if (!StringUtils.hasText(menu.getMenuName())) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR);
        }
        sysMenuService.save(menu);
        return Result.success();
    }

    @RequirePermission("system:menu:update")
    @Log("修改菜单")
    @PutMapping
    public Result update(@RequestBody SysMenu menu) {
        sysMenuService.updateById(menu);
        return Result.success();
    }

    @RequirePermission("system:menu:delete")
    @Log("删除菜单")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        Long childCount = sysMenuService.count(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "存在子菜单，无法删除");
        }
        sysMenuService.removeById(id);
        return Result.success();
    }
}
