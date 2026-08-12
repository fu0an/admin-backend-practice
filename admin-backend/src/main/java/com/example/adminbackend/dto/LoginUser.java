package com.example.adminbackend.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 登录用户信息（含角色与权限），登录成功后缓存到 Redis
 */
@Data
public class LoginUser implements Serializable {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private List<String> roles;
    private Set<String> permissions;
}
