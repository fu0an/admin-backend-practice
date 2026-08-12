CREATE DATABASE IF NOT EXISTS my_admin_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE my_admin_db;

SET NAMES utf8mb4;

-- ===================== 用户表 =====================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL,
    nickname    VARCHAR(50)  DEFAULT ''                COMMENT '昵称',
    avatar      VARCHAR(255) DEFAULT ''                COMMENT '头像',
    password    VARCHAR(100) NOT NULL,
    status      TINYINT      DEFAULT 1                 COMMENT '状态: 1启用 0禁用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户';

-- ===================== 角色表 =====================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name   VARCHAR(50) NOT NULL,
    role_key    VARCHAR(50) NOT NULL,
    sort        INT         DEFAULT 0,
    status      TINYINT     DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    remark      VARCHAR(255) DEFAULT '',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_key (role_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='系统角色';

-- ===================== 菜单/权限表 =====================
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id   BIGINT       DEFAULT 0,
    menu_name   VARCHAR(50)  NOT NULL,
    path        VARCHAR(200) DEFAULT '',
    component   VARCHAR(200) DEFAULT '',
    perms       VARCHAR(100) DEFAULT '' COMMENT '权限标识, 如 system:user:add',
    icon        VARCHAR(100) DEFAULT '',
    menu_type   CHAR(1)      DEFAULT 'C' COMMENT 'M目录 C菜单 F按钮',
    sort        INT          DEFAULT 0,
    visible     TINYINT      DEFAULT 1,
    status      TINYINT      DEFAULT 1,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='菜单/权限表';

-- ===================== 用户角色关联 =====================
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户角色关联';

-- ===================== 角色菜单关联 =====================
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='角色菜单关联';

-- ===================== 操作日志 =====================
DROP TABLE IF EXISTS sys_oper_log;
CREATE TABLE sys_oper_log (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    title          VARCHAR(50)   DEFAULT ''  COMMENT '操作模块',
    method         VARCHAR(200)  DEFAULT ''  COMMENT '方法名',
    request_method VARCHAR(10)   DEFAULT ''  COMMENT 'HTTP方法',
    oper_name      VARCHAR(50)   DEFAULT ''  COMMENT '操作人',
    oper_url       VARCHAR(255)  DEFAULT ''  COMMENT '请求URL',
    oper_ip        VARCHAR(50)   DEFAULT ''  COMMENT '操作IP',
    oper_param     TEXT                       COMMENT '请求参数',
    json_result    TEXT                       COMMENT '返回结果',
    status         TINYINT       DEFAULT 1   COMMENT '1成功 0失败',
    error_msg      VARCHAR(2000) DEFAULT ''  COMMENT '错误信息',
    oper_time      DATETIME      DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='操作日志';

-- ===================== 公告表（业务模块） =====================
DROP TABLE IF EXISTS biz_announcement;
CREATE TABLE biz_announcement (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    title          VARCHAR(200) NOT NULL,
    content        TEXT,
    summary        VARCHAR(500) DEFAULT '',
    publisher_id   BIGINT       DEFAULT NULL,
    publisher_name VARCHAR(50)  DEFAULT '',
    status         TINYINT      DEFAULT 0 COMMENT '0草稿 1已发布 2已下线',
    is_top         TINYINT      DEFAULT 0 COMMENT '0否 1置顶',
    view_count     INT          DEFAULT 0,
    publish_time   DATETIME     DEFAULT NULL,
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='公告表';

-- ===================== 初始化数据 =====================
-- 密码统一为 123456 的 BCrypt 密文
INSERT INTO sys_user (username, nickname, avatar, password, status) VALUES
('admin',    '系统管理员', '', '$2a$10$38CtmpJNHrkGEMSCyJJHBOkIFNAiQdROC2LD7wZn/2axAHO7S2twK', 1),
('editor',   '公告编辑',   '', '$2a$10$38CtmpJNHrkGEMSCyJJHBOkIFNAiQdROC2LD7wZn/2axAHO7S2twK', 1),
('zhangsan', '张三',       '', '$2a$10$38CtmpJNHrkGEMSCyJJHBOkIFNAiQdROC2LD7wZn/2axAHO7S2twK', 1);

INSERT INTO sys_role (role_name, role_key, sort, status, remark) VALUES
('超级管理员', 'admin',  1, 1, '拥有全部权限'),
('公告编辑',   'editor', 2, 1, '可发布和管理公告'),
('普通员工',   'user',   3, 1, '仅可查看公告');

-- 菜单（目录/菜单/按钮）
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, perms, icon, menu_type, sort) VALUES
(1,  0, '系统管理',     '/system',       '',                    '',                      'el-icon-s-tools', 'M', 1),
(2,  1, '用户管理',     'user',          'system/user/index',   'system:user:list',      'el-icon-user',     'C', 1),
(3,  2, '用户新增',     '',              '',                    'system:user:add',       '',                 'F', 1),
(4,  2, '用户修改',     '',              '',                    'system:user:update',    '',                 'F', 2),
(5,  2, '用户删除',     '',              '',                    'system:user:delete',    '',                 'F', 3),
(6,  2, '重置密码',     '',              '',                    'system:user:resetPwd',  '',                 'F', 4),
(7,  2, '分配角色',     '',              '',                    'system:user:assignRole','',                 'F', 5),
(8,  1, '角色管理',     'role',          'system/role/index',   'system:role:list',      'el-icon-s-check',  'C', 2),
(9,  8, '角色新增',     '',              '',                    'system:role:add',       '',                 'F', 1),
(10, 8, '角色修改',     '',              '',                    'system:role:update',    '',                 'F', 2),
(11, 8, '角色删除',     '',              '',                    'system:role:delete',    '',                 'F', 3),
(12, 8, '分配菜单',     '',              '',                    'system:role:assignMenu','',                 'F', 4),
(13, 1, '菜单管理',     'menu',          'system/menu/index',   'system:menu:list',      'el-icon-menu',     'C', 3),
(14, 13,'菜单新增',     '',              '',                    'system:menu:add',       '',                 'F', 1),
(15, 13,'菜单修改',     '',              '',                    'system:menu:update',    '',                 'F', 2),
(16, 13,'菜单删除',     '',              '',                    'system:menu:delete',    '',                 'F', 3),
(17, 1, '操作日志',     'log',           'system/log/index',    'system:log:list',       'el-icon-document', 'C', 4),
(18, 17,'日志删除',     '',              '',                    'system:log:delete',     '',                 'F', 1),
(19, 0, '公告管理',     '/announcement', '',                    '',                      'el-icon-message',  'M', 2),
(20, 19,'公告列表',     'index',         'announcement/index',  'announcement:list',     'el-icon-document', 'C', 1),
(21, 20,'公告新增',     '',              '',                    'announcement:add',      '',                 'F', 1),
(22, 20,'公告修改',     '',              '',                    'announcement:update',   '',                 'F', 2),
(23, 20,'公告删除',     '',              '',                    'announcement:delete',   '',                 'F', 3),
(24, 20,'公告发布',     '',              '',                    'announcement:publish',  '',                 'F', 4),
(25, 20,'公告置顶',     '',              '',                    'announcement:top',      '',                 'F', 5);

-- 用户-角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin -> 超级管理员
(2, 2),  -- editor -> 公告编辑
(3, 3);  -- zhangsan -> 普通员工

-- 角色-菜单: admin 拥有全部
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;
-- editor: 公告管理全部（不含删除）+ 公告菜单目录
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 19), (2, 20), (2, 21), (2, 22), (2, 24), (2, 25);
-- user: 仅查看公告
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 19), (3, 20);

-- 示例公告数据
INSERT INTO biz_announcement (title, content, summary, publisher_id, publisher_name, status, is_top, view_count, publish_time) VALUES
('欢迎使用企业权限管理系统', '本系统基于 Spring Boot + Vue 开发，内置用户、角色、菜单三级 RBAC 权限模型，以及公告管理业务模块。', '系统上线公告', 1, '系统管理员', 1, 1, 128, NOW()),
('关于员工考勤新规的通知', '自下月起，员工上下班需打卡，请各部门做好宣导。', '考勤新规', 1, '系统管理员', 1, 0, 66, NOW()),
('系统维护公告', '本周六 22:00-24:00 系统例行维护，期间服务不可用，敬请谅解。', '维护通知', 2, '公告编辑', 1, 0, 35, NOW());
