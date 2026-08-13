# 企业权限管理系统（RBAC）

前后端分离的企业后台管理系统，内置完整 RBAC（用户-角色-菜单）三级权限模型，并附带公告管理业务模块。

![Java](https://img.shields.io/badge/Java-17+-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-green) ![Vue](https://img.shields.io/badge/Vue-2.x-4FC08D) ![MySQL](https://img.shields.io/badge/MySQL-8.0-orange) ![Redis](https://img.shields.io/badge/Redis-7-blue)

---

## 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [权限设计](#权限设计)
- [快速开始](#快速开始)
- [Docker 部署](#docker-部署)
- [演示账号](#演示账号)
- [项目结构](#项目结构)
- [接口文档](#接口文档)
- [协议](#协议)

---

## 功能特性

- **登录认证**：JWT 无状态登录、登出、Redis 会话缓存、BCrypt 密码加密；进入即登录页（内部系统）
- **用户管理**：分页搜索、新增/编辑/删除、状态启停、分配角色、重置密码
- **角色管理**：分页搜索、新增/编辑/删除、角色-菜单分配
- **操作日志**：AOP 切面自动记录操作人、IP、参数、结果
- **公告管理**：公告 CRUD、发布/下线、置顶、浏览量统计
- **统一返回与异常**：全局异常处理、统一响应格式

## 技术栈

**后端**

| 技术 | 用途 |
|---|---|
| Spring Boot 3.5.14 | 核心框架 |
| MyBatis-Plus 3.5.14 | ORM 与分页 |
| MySQL 8.0 | 数据存储 |
| Redis | 会话缓存与业务缓存 |
| JWT（jjwt 0.11.5） | 登录认证 |
| BCrypt | 密码加密 |
| Spring AOP | 日志切面 |
| Maven | 构建工具 |

**前端**

| 技术 | 用途 |
|---|---|
| Vue 2 + Element UI | UI 框架 |
| vue-element-admin | 后台管理模板 |
| Vue Router | 路由与权限过滤 |
| Vuex | 状态管理 |

## 权限设计

系统采用经典的 **用户 → 角色 → 菜单** 三级权限模型，权限点可精细到按钮级。

```
用户 (sys_user) ──关联──▶ 角色 (sys_role) ──关联──▶ 菜单 (sys_menu)
```

| 表 | 说明 |
|---|---|
| `sys_user` | 用户表 |
| `sys_role` | 角色表 |
| `sys_menu` | 菜单权限表（目录 / 菜单 / 按钮三级） |
| `sys_user_role` | 用户-角色关联表 |
| `sys_role_menu` | 角色-菜单关联表 |
| `sys_oper_log` | 操作日志表 |
| `biz_announcement` | 公告表 |

**认证与授权流程**

1. 登录成功后签发 JWT，并将用户角色、权限信息缓存至 Redis
2. 后续请求携带 `X-Token`，由拦截器校验 JWT 并从 Redis 加载用户信息
3. 拦截器匹配接口上 `@RequirePermission` 声明的权限标识，通过后放行
4. 角色或权限变更时自动清除对应 Redis 缓存，下次请求重新构建

**三层权限控制**

- 前端路由：`meta.roles` 控制菜单可见性
- 前端按钮：`v-perm="'system:user:add'"` 指令控制按钮可见性
- 后端接口：`@RequirePermission` 注解 + 拦截器校验，作为最终安全边界

## 快速开始

### 环境要求

JDK 17 或 21、Maven 3.9+、MySQL 8.0+、Redis 5.0+、Node 16+

### 1. 初始化数据库

```bash
mysql -uboot -p123456 --default-character-set=utf8mb4 < init_rbac.sql
```

数据库 `my_admin_db`，默认账号 `boot` / `123456`，可按需修改 `admin-backend/src/main/resources/application.yml`。

> **注意**：`init_rbac.sql` 为 UTF-8 编码，导入时**必须**加 `--default-character-set=utf8mb4`，否则中文数据会报 `ERROR 1406 (22001) Data too long`。
> 若提示 `mysql 不是内部或外部命令`，说明 mysql 不在 PATH 中，请使用完整路径，例如 `"D:\MySQL\MySQL Server 8.0\bin\mysql.exe" -uboot -p123456 --default-character-set=utf8mb4 < init_rbac.sql`。
> 若提示 `Access denied`，说明本机没有 `boot` 用户，需先用 root 创建该用户并授权（或改用 root 连接，同时修改 `admin-backend/src/main/resources/application.yml` 中的数据库账号密码）。

### 2. 启动后端

```bash
cd admin-backend
mvn spring-boot:run
```

后端需先启动本地 Redis（默认 `localhost:6379`）。服务地址 `http://localhost:8080`。

### 3. 启动前端

```bash
cd vue-element-admin
npm install
npm run dev
```

浏览器访问 `http://localhost:9527`。前端接口地址由 `VUE_APP_BASE_API` 控制（`.env.development` 为 `/api`，开发模式下经 `vue.config.js` 代理到后端 8080 端口）。

> 若使用 Node 17+ 运行 `npm run dev` 报 OpenSSL 相关错误，请设置环境变量：`set NODE_OPTIONS=--openssl-legacy-provider`。

## Docker 部署

```bash
docker-compose up -d --build
```

- 前端：`http://localhost:9527`
- 后端：`http://localhost:8080`
- 首次启动自动执行 `init_rbac.sql` 初始化数据

Docker 模式下前端经 nginx 将 `/api` 反向代理到后端容器，接口访问同本地一致。

## 演示账号

| 账号 | 密码 | 角色 | 权限范围 |
|---|---|---|---|
| `admin` | 123456 | 超级管理员 | 全部功能 |
| `editor` | 123456 | 公告编辑 | 公告管理（发布/编辑/置顶） |
| `zhangsan` | 123456 | 普通员工 | 公告查看 |

## 项目结构

```
admin/
├── admin-backend/                 # 后端服务
│   └── src/main/
│       ├── java/com/example/adminbackend/
│       │   ├── annotation/        # @Log、@RequirePermission
│       │   ├── aspect/            # 日志切面
│       │   ├── common/            # 统一返回、全局异常
│       │   ├── config/            # CORS、Redis、拦截器配置
│       │   ├── controller/        # 接口层（统一 /api 前缀）
│       │   ├── entity/            # 实体
│       │   ├── interceptor/       # JWT 认证拦截器
│       │   ├── mapper/            # 数据访问层
│       │   ├── service/           # 业务层
│       │   └── util/              # 工具类
│       └── resources/
│           ├── static/            # 前端构建产物（由 npm run build:prod 生成，不入库）
│           └── application.yml    # 配置文件
├── vue-element-admin/             # 前端应用
│   └── src/
│       ├── api/                   # 接口封装
│       ├── directive/perm/        # v-perm 按钮级权限指令
│       ├── router/                # 路由配置
│       ├── store/                 # 状态管理
│       └── views/                 # 页面
├── init_rbac.sql                  # 数据库脚本
└── docker-compose.yml             # 容器编排
```

## 接口文档

Base URL：`http://localhost:8080/api`，除登录/登出外均需请求头 `X-Token: <JWT>`

| 模块 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 认证 | POST | `/user/login` | 登录 |
| 认证 | GET | `/user/info` | 当前用户信息 |
| 认证 | POST | `/user/logout` | 登出 |
| 用户 | GET | `/system/user/page` | 用户分页查询 |
| 用户 | POST | `/system/user` | 新增用户 |
| 用户 | PUT | `/system/user` | 修改用户 |
| 用户 | DELETE | `/system/user/{id}` | 删除用户 |
| 用户 | PUT | `/system/user/resetPwd` | 重置密码 |
| 用户 | PUT | `/system/user/assignRole` | 分配角色 |
| 角色 | GET | `/system/role/page` | 角色分页查询 |
| 角色 | GET | `/system/role/listAll` | 全部角色 |
| 角色 | POST | `/system/role` | 新增角色 |
| 角色 | PUT | `/system/role` | 修改角色 |
| 角色 | DELETE | `/system/role/{id}` | 删除角色 |
| 角色 | PUT | `/system/role/assignMenu` | 分配菜单 |
| 菜单 | GET | `/system/menu/tree` | 菜单树（供角色分配菜单使用） |
| 日志 | GET | `/system/log/page` | 日志分页查询 |
| 日志 | DELETE | `/system/log/clear` | 清空日志 |
| 公告 | GET | `/announcement/page` | 公告分页查询 |
| 公告 | GET | `/announcement/{id}` | 公告详情 |
| 公告 | POST | `/announcement` | 新增公告 |
| 公告 | PUT | `/announcement` | 修改公告 |
| 公告 | DELETE | `/announcement/{id}` | 删除公告 |
| 公告 | PUT | `/announcement/publish/{id}` | 发布公告 |
| 公告 | PUT | `/announcement/top/{id}` | 置顶公告 |

## 协议

[MIT](LICENSE)
