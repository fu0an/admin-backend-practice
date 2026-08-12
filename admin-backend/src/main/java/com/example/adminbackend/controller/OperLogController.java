package com.example.adminbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.adminbackend.annotation.Log;
import com.example.adminbackend.annotation.RequirePermission;
import com.example.adminbackend.common.Result;
import com.example.adminbackend.entity.SysOperLog;
import com.example.adminbackend.mapper.SysOperLogMapper;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/log")
public class OperLogController {

    @Resource
    private SysOperLogMapper sysOperLogMapper;

    @RequirePermission("system:log:list")
    @Log("分页查询操作日志")
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String title,
                       @RequestParam(required = false) String operName) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(title), SysOperLog::getTitle, title);
        wrapper.like(StringUtils.hasText(operName), SysOperLog::getOperName, operName);
        wrapper.orderByDesc(SysOperLog::getOperTime);
        Page<SysOperLog> page = sysOperLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @RequirePermission("system:log:delete")
    @Log("清空操作日志")
    @DeleteMapping("/clear")
    public Result clear() {
        sysOperLogMapper.delete(null);
        return Result.success();
    }
}
