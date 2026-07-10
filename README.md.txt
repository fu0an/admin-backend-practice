# Admin 后台管理系统（后端）
## 项目介绍
基于 SpringBoot + MyBatis-Plus 开发的前后端分离后台管理后端服务，
实现 JWT 无状态登录认证、BCrypt 密码加密、用户管理、全局异常处理、跨域配置等功能，
适配 Vue 前端访客/登录双模式业务。

## 技术栈
- 核心框架：SpringBoot 3.5.14
- ORM 框架：MyBatis-Plus
- 数据库：MySQL 8.0+
- 认证方案：JWT（遵循 RFC 7518 安全规范）
- 密码加密：BCrypt
- 接口文档：Knife4j
- 构建工具：Maven

## 环境要求
1. JDK 17 / JDK 21 / JDK24
2. MySQL 8.0 及以上
3. 网络正常（用于前端页面访问）

## 部署&启动方式
### 方式一：Jar 包启动
1. 确保本地 MySQL 服务已启动
2. 执行数据库脚本 `sys_user.sql`，创建库、表及测试数据
3. 打开 CMD / 终端，进入 jar 包所在目录，执行命令：
```bash
java -jar admin-backend.jar
4.# Admin 后台管理系统（后端）
## 项目介绍
基于 SpringBoot + MyBatis-Plus 开发的前后端分离后台管理后端服务，
实现 JWT 无状态登录认证、BCrypt 密码加密、用户管理、全局异常处理、跨域配置等功能，
适配 Vue 前端访客/登录双模式业务，符合企业后端开发规范。

## 技术栈
- 核心框架：SpringBoot 3.x
- ORM 框架：MyBatis-Plus
- 数据库：MySQL 8.0+
- 认证方案：JWT（遵循 RFC 7518 安全规范）
- 密码加密：BCrypt
- 接口文档：Knife4j
- 构建工具：Maven

## 环境要求
1. JDK 17 / JDK 21
2. MySQL 8.0 及以上
3. 网络正常（用于前端页面访问）

## 部署&启动方式
### 方式一：Jar 包启动（推荐，面试演示使用）
1. 确保本地 MySQL 服务已启动
2. 执行数据库脚本 `sys_user.sql`，创建库、表及测试数据
3. 打开 CMD / 终端，进入 jar 包所在目录，执行命令：
```bash
java -jar admin-backend.jar
4.服务默认端口：8080

### 方式二：IDEA 源码启动
导入 Maven 项目，配置 MySQL 连接信息，运行主启动类即可。
访问地址
接口文档：http://localhost:8080/doc.html
后端接口基础地址：http://localhost:8080
测试账号
用户名：admin
密码：111111
核心功能
登录认证：JWT 签发 & 校验 Token，规避密钥安全问题；
密码安全：BCrypt 加盐加密，数据库密文存储；
用户管理：用户分页查询、新增用户（自动加密密码）；
项目规范：统一返回结果、全局异常捕获、全局跨域配置；
工程化：Maven 打包，可独立部署运行。