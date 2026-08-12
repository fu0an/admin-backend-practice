package com.example.adminbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.adminbackend.entity.Announcement;
import com.example.adminbackend.mapper.AnnouncementMapper;
import com.example.adminbackend.service.AnnouncementService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Cacheable(value = "announcement",
            key = "#pageNum + '-' + #pageSize + '-' + (#status==null?'':#status) + '-' + (#title==null?'':#title)")
    @Override
    public Page<Announcement> pageList(Integer pageNum, Integer pageSize, Integer status, String title) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Announcement::getStatus, status);
        wrapper.like(StringUtils.hasText(title), Announcement::getTitle, title);
        wrapper.orderByDesc(Announcement::getIsTop);
        wrapper.orderByDesc(Announcement::getPublishTime);
        wrapper.orderByDesc(Announcement::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @CacheEvict(value = "announcement", allEntries = true)
    @Override
    public boolean publish(Long id) {
        Announcement announcement = new Announcement();
        announcement.setId(id);
        announcement.setStatus(1);
        announcement.setPublishTime(LocalDateTime.now());
        return updateById(announcement);
    }

    @CacheEvict(value = "announcement", allEntries = true)
    @Override
    public boolean toggleTop(Long id) {
        Announcement exist = getById(id);
        if (exist == null) {
            return false;
        }
        Announcement announcement = new Announcement();
        announcement.setId(id);
        announcement.setIsTop(exist.getIsTop() != null && exist.getIsTop() == 1 ? 0 : 1);
        return updateById(announcement);
    }

    @CacheEvict(value = "announcement", allEntries = true)
    @Override
    public boolean save(Announcement entity) {
        return super.save(entity);
    }

    @CacheEvict(value = "announcement", allEntries = true)
    @Override
    public boolean updateById(Announcement entity) {
        return super.updateById(entity);
    }

    @CacheEvict(value = "announcement", allEntries = true)
    @Override
    public boolean removeById(java.io.Serializable id) {
        return super.removeById(id);
    }
}
