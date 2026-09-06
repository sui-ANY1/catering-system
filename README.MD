# 🍽️ 餐饮管理系统（Catering System）

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.5-blue.svg)](https://baomidou.com/)
[![Sa-Token](https://img.shields.io/badge/Sa--Token-1.40.0-orange.svg)](https://sa-token.cc/)
[![Vue 3](https://img.shields.io/badge/Vue-3.4-42b883.svg)](https://vuejs.org/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-2.7-409eff.svg)](https://element-plus.org/)

一个前后端分离的餐饮管理系统：后端基于 **Spring Boot 3**，前端基于 **Vue 3 + Element Plus**。支持堂食扫码点餐、桌台管理、订单管理、会员管理等核心业务，含权限认证、Redis 购物车、分布式锁、事务控制等企业级实践。

---

## 📌 项目简介

面向中小型餐饮门店，提供完整的点餐业务流程：

- 顾客扫描桌台二维码进入点餐界面（**免登录**）
- 将菜品加入购物车（Redis 存储，支持数量增减）
- 提交订单（分布式锁防重复提交、事务保证一致性、按数量扣库存）
- 后厨接单制作、收银员结账
- 管理员管理菜品、桌台、会员、查看订单明细与统计

后端接口文档自动生成（Knife4j），可直接用于毕业设计或二次开发。

---

## 🛠️ 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.5 | 基础框架 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 6.0+ | 购物车缓存、分布式锁 |
| Sa-Token | 1.40.0 | 认证与权限 |
| Hutool | 5.8.32 | BCrypt 密码加密等工具 |
| Knife4j | 4.5.0 | 接口文档 |
| Lombok | - | 简化代码 |
| JDK | 17+ | 运行环境 |

### 前端（`frontend/`）

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4 | 视图框架 |
| Vite | 5 | 构建工具 |
| Element Plus | 2.7 | UI 组件库 |
| Pinia | 2.1 | 状态管理 |
| Vue Router | 4.4 | 路由 |
| Axios | 1.7 | HTTP 请求 |

---

## ✨ 功能模块

- **系统管理**：用户登录、角色权限（RBAC）、接口拦截、BCrypt 密码加密
- **菜品管理**：菜品分类、菜品 CRUD、上下架、库存、推荐
- **桌台管理**：区域管理、桌台状态（空闲/占用/维修）
- **点餐购物车**：基于 Redis Hash，支持加入/减少/移除/清空，数量累计
- **订单管理**：下单（事务+分布式锁）、按数量扣减库存、订单状态流转、订单明细
- **会员管理**：会员 CRUD、余额、积分
- **数据看板**：菜品/分类/桌台/订单/会员统计与营业额汇总
- **接口文档**：Knife4j 在线调试

---

## 🗂️ 项目结构

```
catering-system
├── src/main/java/com/catering      # 后端
│   ├── common        # 统一返回、全局异常处理
│   ├── config        # Redis、Sa-Token、MyBatis-Plus 配置
│   ├── controller    # 接口层
│   ├── dto           # 请求/视图对象（LoginDTO、CartItem）
│   ├── entity        # 数据库实体
│   ├── mapper        # MyBatis Mapper 接口
│   └── service       # 业务逻辑层
├── src/main/resources/application.yml
├── sql/init.sql                  # 建表脚本 + 种子数据
├── frontend/                     # 前端（Vue3 + Element Plus）
│   ├── src/api        # 接口封装
│   ├── src/router     # 路由 + 登录守卫
│   ├── src/store      # Pinia 状态
│   ├── src/views      # 页面（admin 管理端 / customer 点餐端）
│   └── vite.config.js # 开发代理 /api -> 8080
└── pom.xml
```

---

## 🚀 快速开始

### 环境要求

- JDK 17+、Maven 3.6+
- MySQL 8.0、Redis 6.0+
- Node.js 18+

### 1. 创建数据库并导入建表脚本

脚本会自动创建 `catering` 库、建表并插入种子数据（管理员账号等）：

```bash
mysql -uroot -p < sql/init.sql
```

### 2. 修改配置文件

编辑 `src/main/resources/application.yml`，按需修改 MySQL / Redis 连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/catering?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: 111111
  data:
    redis:
      host: localhost
      port: 6379
```

### 3. 启动后端

```bash
mvn spring-boot:run
```

接口文档：http://localhost:8080/doc.html

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173

> 前端开发环境通过 Vite 代理将 `/api` 转发到后端 `http://localhost:8080`，无需处理跨域。

### 5. 登录测试

种子数据中的默认账号：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 111111 | 管理员 |
| cashier | 111111 | 收银员 |

点餐端（`/menu`）免登录，直接选桌台即可点餐下单。

---

## 🔌 接口概览

| 模块 | 接口 | 说明 | 鉴权 |
|------|------|------|------|
| 认证 | `POST /auth/login` | 登录，返回 token | 免登录 |
| 认证 | `GET /auth/logout` | 退出登录 | 登录 |
| 菜品 | `/dish` CRUD | 菜品增删改查 | 读免登录，写需登录 |
| 分类 | `/category` CRUD | 菜品分类 | 读免登录，写需登录 |
| 桌台 | `/table` CRUD | 桌台管理 | 读免登录，写需登录 |
| 区域 | `/area` CRUD | 用餐区域 | 读免登录，写需登录 |
| 购物车 | `POST /cart/add` | 加入购物车（数量+1） | 免登录 |
| 购物车 | `POST /cart/decrease` | 减少数量（减到 0 移除） | 免登录 |
| 购物车 | `DELETE /cart/remove` | 移除单个菜品 | 免登录 |
| 购物车 | `GET /cart/list` | 查看购物车 | 免登录 |
| 购物车 | `DELETE /cart/clear` | 清空购物车 | 免登录 |
| 订单 | `/order` CRUD | 订单管理 | 登录 |
| 订单 | `POST /order/submit` | 提交订单 | 免登录 |
| 订单明细 | `GET /orderDetail/{orderId}` | 查询订单明细 | 登录 |
| 会员 | `/member` CRUD | 会员管理 | 登录 |
| 用户 | `/user` CRUD | 系统用户 | 登录 |

> 统一返回格式 `Result { code, msg, data }`，`code=200` 成功、`500` 失败、`401` 未登录。鉴权 token 通过请求头 `token` 传递（Sa-Token `token-name: token`）。

---

## 📊 数据库设计（核心表）

| 表名 | 说明 |
|------|------|
| `sys_user` | 系统用户（管理员/收银员/厨师等） |
| `dish_category` | 菜品分类 |
| `dish` | 菜品信息 |
| `table_area` | 用餐区域 |
| `dining_table` | 桌台 |
| `orders` | 订单主表 |
| `order_detail` | 订单明细 |
| `member` | 会员 |

逻辑删除：`sys_user`、`dish`、`dish_category`、`dining_table`、`orders` 含 `deleted` 字段（0=正常，1=删除）；时间字段由 `MetaObjectHandler` 自动填充。

---

## 🔒 核心亮点

**1. Redis 分布式锁防重复下单**

```java
Boolean locked = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
if (!locked) {
    throw new RuntimeException("订单处理中，请稍后重试");
}
try {
    // 下单逻辑...
} finally {
    redisTemplate.delete(lockKey);
}
```

**2. 事务保证数据一致性**

订单创建涉及订单表、订单明细表、菜品库存、桌台状态，使用 `@Transactional` 确保同时成功或失败。

**3. BCrypt 密码加密**

用户密码加密存储（`SysUserServiceImpl` 在保存/更新时自动 `BCrypt.hashpw`），登录使用 `BCrypt.checkpw` 校验。

**4. Sa-Token 统一认证**

自定义拦截器对所有管理端接口进行登录校验，白名单放行登录、接口文档及顾客点餐端接口。

**5. MyBatis-Plus 自动填充**

`MetaObjectHandler` 自动维护 `createTime`、`updateTime`，减少重复代码。

---

## 🤝 贡献

欢迎提交 Issue 或 Pull Request。

## 📝 许可证

MIT License
