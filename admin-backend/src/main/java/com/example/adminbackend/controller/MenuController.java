package com.example.adminbackend.controller;

import com.example.adminbackend.annotation.RequirePermission;
import com.example.adminbackend.common.Result;
import com.example.adminbackend.service.SysMenuService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/menu")
public class MenuController {

    @Resource
    private SysMenuService sysMenuService;

    @RequirePermission("system:menu:list")
    @GetMapping("/tree")
    public Result tree() {
        return Result.success(sysMenuService.buildTree());
    }
}
