# 企业权限管理系统（RBAC + 公告管理）

## 项目介绍
前后端分离的企业后台管理系统，内置完整 **RBAC（用户-角色-菜单）三级权限模型**，并附带
**公告管理**业务模块。后端基于 Spring Boot 3 + MyBatis-Plus，前端基于 vue-element-admin。

面向软件开发学习用途，权限设计可完整讲通，并支持 Docker 一键部署。

## 技术栈
- 后端：Spring Boot 3.5.14 / MyBatis-Plus 3.5.14 / MySQL 8.0 / Redis / JWT / BCrypt / Spring AOP
- 前端：Vue 2 + Element UI + vue-element-admin（RBAC 路由动态加载 + 按钮级权限指令）
- 构建：Maven 3.9+ / Node 16+ / Docker + docker-compose

## 系统功能
| 模块 | 功能 |
|---|---|
| 登录认证 | JWT 无状态登录、BCrypt 密码加密、Redis 会话缓存、登出失效 |
| 用户管理 | 分页/搜索、新增/编辑/删除、状态启停、分配角色、重置密码 |
| 角色管理 | 分页/搜索、新增/编辑/删除、角色-菜单分配 |
| 菜单管理 | 目录/菜单/按钮三级树、权限标识（perms）管理 |
| 操作日志 | @Log 注解 + AOP 自动落库、分页/搜索/清空 |
| 公告管理 | 公告 CRUD、发布/下线、置顶、浏览量统计、Redis 列表缓存 |

## RBAC 权限模型

### 表结构（`init_rbac.sql`）
```
sys_user        用户表（username/nickname/avatar/password/status）
sys_role        角色表（role_name/role_key/status）
sys_menu        菜单表（parent_id/menu_name/path/component/perms/menu_type/icon）
sys_user_role   用户-角色 关联表
sys_role_menu   角色-菜单 关联表
sys_oper_log    操作日志表
biz_announcement 公告表（业务模块）
```

### 认证与授权流程
```
前端登录 ──▶ /user/login 校验密码 ──▶ 生成 JWT ──▶ 构建 LoginUser(角色+权限) 缓存到 Redis
       ──▶ 后续请求带 X-Token ──▶ JwtAuthInterceptor:
             ① 校验 JWT 解析 userId
             ② 从 Redis 读登录用户信息（用户/角色/权限）
             ③ 校验 @RequirePermission 注解声明的权限标识
             ④ 写入 LoginUserContext（ThreadLocal）
角色/权限变更 ──▶ 清除该用户 Redis 缓存，下次请求自动重建
```

### 权限控制点
- **前端**：路由 `meta.roles` 控制菜单可见性；`v-perm="'system:user:add'"` 指令控制按钮可见性
- **后端**：`@RequirePermission("system:user:add")` 注解 + 拦截器强校验（前端隐藏不是安全边界，后端才是）
- **超级管理员**：角色标识为 `admin` 时自动拥有全部权限

## 目录结构
```
admin/
├── admin-backend/            # 后端
│   └── src/main/java/com/example/adminbackend/
│       ├── annotation/       # @Log、@RequirePermission 注解
│       ├── aspect/           # 日志切面（落库）
│       ├── config/           # CORS / Redis / MyBatis-Plus / 拦截器注册
│       ├── context/          # 登录用户上下文 ThreadLocal
│       ├── controller/       # 登录 / 用户 / 角色 / 菜单 / 日志 / 公告
│       ├── dto/              # LoginUser
│       ├── entity/           # 实体
│       ├── interceptor/      # JWT 认证+授权拦截器
│       ├── mapper/ service/ util/
├── vue-element-admin/        # 前端
│   └── src/
│       ├── api/              # user/role/menu/announcement/log
│       ├── directive/perm/   # v-perm 按钮级权限指令
│       ├── store/            # user（roles+permissions）
│       ├── router/           # 动态路由 + meta.roles
│       └── views/            # system/{user,role,menu,log} / announcement
├── init_rbac.sql             # 建库建表 + 种子数据
├── docker-compose.yml        # MySQL/Redis/后端/前端 一键部署
```

## 本地运行

### 环境要求
JDK 17/21、Maven 3.9+、MySQL 8.0+、Redis 5.0+、Node 16+

### 1. 初始化数据库
```bash
mysql -uboot -p123456 < init_rbac.sql
```
（可按需修改 `admin-backend/src/main/resources/application.yml` 中的数据库/Redis 连接）

### 2. 启动后端
```bash
cd admin-backend
mvn spring-boot:run
# 或 IDEA 运行 AdminBackendApplication，端口 8080，context-path: /api
```

### 3. 启动前端
```bash
cd vue-element-admin
npm install
npm run dev
# 访问 http://localhost:9527
```
前端 `src/utils/request.js` 中 baseURL 为 `http://localhost:8080/api`，由后端全局 CORS 放行，无需代理配置。

## Docker 一键部署
```bash
docker-compose up -d --build
```
- 前端：http://localhost:9527
- 后端：http://localhost:8080/api
- 首次启动自动执行 `init_rbac.sql` 初始化数据
- 可自行在 nginx.conf 中开启 `/api` 反代以隐藏后端端口

## 演示账号（密码统一 123456）
| 账号 | 角色 | 可见菜单 | 权限说明 |
|---|---|---|---|
| admin | 超级管理员 | 全部（公告+系统管理） | 全部权限 |
| editor | 公告编辑 | 公告管理 | 可发布/编辑/置顶公告，不可删除，无系统管理 |
| zhangsan | 普通员工 | 公告管理 | 仅查看公告，无任何操作按钮 |

## 核心亮点（面试可讲）
1. **RBAC 三级模型**：用户-角色-菜单，前端路由 + 按钮级权限 + 后端注解三层联动
2. **JWT 无状态认证 + Redis 会话**：登录即构建用户上下文缓存，改权限实时生效（缓存失效策略）
3. **后端强校验**：明确"前端隐藏≠安全"，权限判断落在拦截器
4. **操作日志落库**：AOP 切面统一采集操作人/IP/参数/结果，业务代码零侵入
5. **缓存一致性**：Spring Cache + Redis，公告列表/用户信息缓存，变更即时失效
