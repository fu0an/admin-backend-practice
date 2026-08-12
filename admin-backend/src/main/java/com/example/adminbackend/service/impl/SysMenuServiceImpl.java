package com.example.adminbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.adminbackend.entity.SysMenu;
import com.example.adminbackend.mapper.SysMenuMapper;
import com.example.adminbackend.service.SysMenuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<SysMenu> buildTree() {
        List<SysMenu> all = list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort));
        Map<Long, List<SysMenu>> childrenMap = all.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu menu : all) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                roots.add(menu);
            }
            menu.setChildren(childrenMap.getOrDefault(menu.getId(), new ArrayList<>()));
        }
        return roots;
    }
}
