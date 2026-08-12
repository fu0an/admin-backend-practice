package com.example.adminbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.adminbackend.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {
    List<Long> getMenuIdsByRoleId(Long roleId);

    void assignMenus(Long roleId, List<Long> menuIds);

    List<SysRole> listAll();
}
