package com.example.adminbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.adminbackend.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
