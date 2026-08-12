package com.example.adminbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.adminbackend.common.BusinessException;
import com.example.adminbackend.common.ResultCodeEnum;
import com.example.adminbackend.dto.LoginUser;
import com.example.adminbackend.entity.SysMenu;
import com.example.adminbackend.entity.SysRole;
import com.example.adminbackend.entity.SysRoleMenu;
import com.example.adminbackend.entity.SysUserRole;
import com.example.adminbackend.entity.User;
import com.example.adminbackend.mapper.SysMenuMapper;
import com.example.adminbackend.mapper.SysRoleMapper;
import com.example.adminbackend.mapper.SysRoleMenuMapper;
import com.example.adminbackend.mapper.SysUserRoleMapper;
import com.example.adminbackend.mapper.UserMapper;
import com.example.adminbackend.service.UserService;
import com.example.adminbackend.util.PasswordUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String LOGIN_USER_KEY = "login:user:";
    private static final Duration LOGIN_USER_TTL = Duration.ofHours(2);

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Resource
    private SysMenuMapper sysMenuMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = getOne(wrapper);

        if (user == null) {
            return null;
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCodeEnum.LOGIN_ERROR);
        }

        boolean isPasswordOk = PasswordUtil.match(password, user.getPassword());
        return isPasswordOk ? user : null;
    }

    @Override
    @CacheEvict(value = "user", allEntries = true)
    public boolean addUser(User user) {
        String encodePwd = PasswordUtil.encode(user.getPassword());
        user.setPassword(encodePwd);
        return save(user);
    }

    @Override
    public LoginUser getLoginUser(Long userId) {
        String key = LOGIN_USER_KEY + userId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            try {
                return objectMapper.readValue(json, LoginUser.class);
            } catch (Exception e) {
                log.warn("解析登录缓存失败: {}", e.getMessage());
            }
        }
        return buildLoginUser(userId);
    }

    @Override
    public LoginUser buildLoginUser(Long userId) {
        User user = getById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            return null;
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setAvatar(user.getAvatar());

        List<Long> roleIds = getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            loginUser.setRoles(Collections.emptyList());
            loginUser.setPermissions(Collections.emptySet());
        } else {
            List<String> roleKeys = sysRoleMapper.selectBatchIds(roleIds)
                    .stream().map(SysRole::getRoleKey).collect(Collectors.toList());
            loginUser.setRoles(roleKeys);
            loginUser.setPermissions(getPermsByRoleIds(roleIds));
        }

        cacheLoginUser(loginUser);
        return loginUser;
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        return sysUserRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    @Override
    public Set<String> getPermsByUserId(Long userId) {
        return getPermsByRoleIds(getRoleIdsByUserId(userId));
    }

    private Set<String> getPermsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptySet();
        }
        boolean isAdmin = sysRoleMapper.selectBatchIds(roleIds).stream()
                .anyMatch(r -> "admin".equals(r.getRoleKey()));
        if (isAdmin) {
            return sysMenuMapper.selectList(null).stream()
                    .map(SysMenu::getPerms)
                    .filter(s -> s != null && !s.isEmpty())
                    .collect(Collectors.toSet());
        }
        List<Long> menuIds = sysRoleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds))
                .stream().map(SysRoleMenu::getMenuId).distinct().collect(Collectors.toList());
        if (menuIds.isEmpty()) {
            return Collections.emptySet();
        }
        return sysMenuMapper.selectBatchIds(menuIds).stream()
                .map(SysMenu::getPerms)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                sysUserRoleMapper.insert(ur);
            }
        }
        clearUserCache(userId);
    }

    @Override
    public boolean resetPassword(Long userId, String newPassword) {
        User user = new User();
        user.setId(userId);
        user.setPassword(PasswordUtil.encode(newPassword));
        boolean ok = updateById(user);
        clearUserCache(userId);
        return ok;
    }

    @Override
    @Transactional
    public boolean addUserWithRoles(User user) {
        Long count = count(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCodeEnum.USER_EXIST);
        }
        user.setPassword(PasswordUtil.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        boolean ok = save(user);
        if (ok && user.getRoleIds() != null) {
            assignRoles(user.getId(), user.getRoleIds());
        }
        return ok;
    }

    @Override
    @Transactional
    public boolean updateUserWithRoles(User user) {
        User exist = getById(user.getId());
        if (exist == null) {
            throw new BusinessException(ResultCodeEnum.DATA_NOT_EXIST);
        }
        user.setPassword(null);
        boolean ok = updateById(user);
        if (ok && user.getRoleIds() != null) {
            assignRoles(user.getId(), user.getRoleIds());
        }
        clearUserCache(user.getId());
        return ok;
    }

    @Override
    public void clearUserCache(Long userId) {
        if (userId != null) {
            stringRedisTemplate.delete(LOGIN_USER_KEY + userId);
        }
    }

    @Override
    public void clearAllLoginUserCache() {
        Set<String> keys = stringRedisTemplate.keys(LOGIN_USER_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    private void cacheLoginUser(LoginUser loginUser) {
        try {
            stringRedisTemplate.opsForValue().set(
                    LOGIN_USER_KEY + loginUser.getUserId(),
                    objectMapper.writeValueAsString(loginUser),
                    LOGIN_USER_TTL);
        } catch (Exception e) {
            log.warn("缓存登录用户失败: {}", e.getMessage());
        }
    }

    @Cacheable(value = "user", key = "#id")
    public User getById(Long id) {
        return super.getById(id);
    }

    @Cacheable(value = "user", key = "'list'")
    public java.util.List<User> list() {
        return super.list();
    }

    @CachePut(value = "user", key = "#entity.id")
    @CacheEvict(value = "user", key = "'list'")
    public boolean updateById(User entity) {
        return super.updateById(entity);
    }

    @CacheEvict(value = "user", allEntries = true)
    public boolean removeById(Long id) {
        return super.removeById(id);
    }
}
