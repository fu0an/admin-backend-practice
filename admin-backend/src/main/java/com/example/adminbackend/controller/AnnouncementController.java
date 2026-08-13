package com.example.adminbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.adminbackend.annotation.Log;
import com.example.adminbackend.annotation.RequirePermission;
import com.example.adminbackend.common.BusinessException;
import com.example.adminbackend.common.Result;
import com.example.adminbackend.common.ResultCodeEnum;
import com.example.adminbackend.context.LoginUserContext;
import com.example.adminbackend.dto.LoginUser;
import com.example.adminbackend.entity.Announcement;
import com.example.adminbackend.service.AnnouncementService;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    @RequirePermission("announcement:list")
    @Log("分页查询公告")
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) String title) {
        return Result.success(announcementService.pageList(pageNum, pageSize, status, title));
    }

    @RequirePermission("announcement:list")
    @Log("查询公告详情")
    @GetMapping("/{id}")
    public Result getOne(@PathVariable Long id) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCodeEnum.DATA_NOT_EXIST);
        }
        // 已发布的公告浏览量 +1
        if (announcement.getStatus() != null && announcement.getStatus() == 1) {
            announcementService.update(new LambdaUpdateWrapper<Announcement>()
                    .eq(Announcement::getId, id)
                    .setSql("view_count = view_count + 1"));
            announcement.setViewCount(announcement.getViewCount() + 1);
        }
        return Result.success(announcement);
    }

    @RequirePermission("announcement:add")
    @Log("新增公告")
    @PostMapping
    public Result add(@RequestBody Announcement announcement) {
        if (!StringUtils.hasText(announcement.getTitle())) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR);
        }
        LoginUser loginUser = LoginUserContext.get();
        announcement.setPublisherId(loginUser.getUserId());
        announcement.setPublisherName(StringUtils.hasText(loginUser.getNickname())
                ? loginUser.getNickname() : loginUser.getUsername());
        if (announcement.getStatus() == null) {
            announcement.setStatus(0);
        }
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        announcement.setViewCount(0);
        if (announcement.getStatus() == 1) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        announcementService.save(announcement);
        return Result.success();
    }

    @RequirePermission("announcement:update")
    @Log("修改公告")
    @PutMapping
    public Result update(@RequestBody Announcement announcement) {
        Announcement exist = announcementService.getById(announcement.getId());
        if (exist == null) {
            throw new BusinessException(ResultCodeEnum.DATA_NOT_EXIST);
        }
        announcement.setPublisherId(exist.getPublisherId());
        announcement.setPublisherName(exist.getPublisherName());
        announcementService.updateById(announcement);
        return Result.success();
    }

    @RequirePermission("announcement:delete")
    @Log("删除公告")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        announcementService.removeById(id);
        return Result.success();
    }

    @RequirePermission("announcement:publish")
    @Log("发布公告")
    @PutMapping("/publish/{id}")
    public Result publish(@PathVariable Long id) {
        announcementService.publish(id);
        return Result.success();
    }

    @RequirePermission("announcement:top")
    @Log("置顶公告")
    @PutMapping("/top/{id}")
    public Result top(@PathVariable Long id) {
        announcementService.toggleTop(id);
        return Result.success();
    }
}
