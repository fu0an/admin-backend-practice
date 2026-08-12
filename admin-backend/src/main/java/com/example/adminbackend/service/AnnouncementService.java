package com.example.adminbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.adminbackend.entity.Announcement;

public interface AnnouncementService extends IService<Announcement> {
    Page<Announcement> pageList(Integer pageNum, Integer pageSize, Integer status, String title);

    boolean publish(Long id);

    boolean toggleTop(Long id);
}
