package com.example.adminbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.adminbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}