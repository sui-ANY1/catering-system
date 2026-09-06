# 🍽️ 餐饮管理系统（Catering System）

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.5-blue.svg)](https://baomidou.com/)
[![Sa-Token](https://img.shields.io/badge/Sa--Token-1.40.0-orange.svg)](https://sa-token.cc/)

一个基于 **SpringBoot 3** 的餐饮管理系统后端服务，支持堂食扫码点餐、桌台管理、订单管理、会员管理等核心业务。采用主流技术栈，包含权限认证、缓存、分布式锁、事务控制等企业级实践。

---

## 📌 项目简介

本系统面向中小型餐饮门店，提供完整的点餐业务流程：

- 顾客扫描桌台二维码进入点餐界面
- 将菜品加入购物车（Redis 存储）
- 提交订单（分布式锁防重复提交，事务保证数据一致性）
- 后厨接单制作
- 收银员结账、退款
- 管理员管理菜品、桌台、会员、查看统计

项目后端代码结构清晰，接口文档自动生成（Knife4j），可直接用于毕业设计或二次开发。

---

## 🛠️ 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.5 | 基础框架 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 6.0+ | 购物车缓存、分布式锁 |
| Sa-Token | 1.40.0 | 认证与权限 |
| BCrypt (Hutool) | - | 密码加密 |
| Knife4j | 4.5.0 | 接口文档 |
| Lombok | - | 简化代码 |
| Maven | 3.6+ | 构建工具 |
| JDK | 17+ | 运行环境 |

---

## ✨ 功能模块

- **系统管理**：用户登录、角色权限（RBAC）、接口拦截
- **菜品管理**：菜品分类、菜品 CRUD、上下架、库存、推荐
- **桌台管理**：区域管理、桌台状态（空闲/占用/维修）
- **点餐购物车**：基于 Redis Hash 实现，支持增删改查
- **订单管理**：下单（事务+分布式锁）、库存扣减、订单状态流转
- **会员管理**：会员 CRUD、余额、积分
- **数据安全**：BCrypt 密码加密、参数校验（@Valid）
- **接口文档**：Knife4j 在线调试

---

## 🗂️ 项目结构
catering-system
├── src/main/java/com/catering
│ ├── common # 统一返回、全局异常处理
│ ├── config # Redis、Sa-Token、MyBatis-Plus 配置
│ ├── controller # 接口层
│ ├── dto # 请求参数对象
│ ├── entity # 数据库实体
│ ├── mapper # MyBatis Mapper 接口
│ ├── service # 业务逻辑层
│ │ └── impl
│ └── vo # 视图对象（如有）
├── src/main/resources
│ ├── application.yml # 配置文件
│ └── mapper/ # XML 映射文件（如有）
└── pom.xml

text

---

## 🚀 快速开始

### 环境要求

- JDK 17+（推荐 21）
- Maven 3.6+
- MySQL 8.0
- Redis 6.0+

### 1. 创建数据库

```sql
CREATE DATABASE catering DEFAULT CHARACTER SET utf8mb4;
2. 导入建表脚本
执行项目中的 sql/init.sql（或手动创建数据表，结构见下方“数据库设计”）。

3. 修改配置文件
编辑 src/main/resources/application.yml：

yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/catering?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: ${MYSQL_PASSWORD}
  data:
    redis:
      host: localhost
      port: 6379
4. 启动项目
bash
mvn spring-boot:run
或者用 IDE 直接运行 CateringApplication.java。

5. 访问接口文档
浏览器打开：http://localhost:8080/doc.html

6. 登录测试
默认管理员账号（需提前在数据库插入用户，密码为 BCrypt 加密）：

用户名：admin

密码：111111（实际以数据库密文对应明文为准）

📊 数据库设计（核心表）
表名	说明
sys_user	系统用户（管理员/收银员/厨师等）
dish_category	菜品分类
dish	菜品信息
table_area	用餐区域
dining_table	桌台
orders	订单主表
order_detail	订单明细
member	会员
逻辑删除：deleted 字段（0=正常，1=删除）；时间字段自动填充。

🔒 核心亮点
1. Redis 分布式锁防重复下单
java
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
2. 事务保证数据一致性
订单创建涉及订单表、订单明细表、菜品库存、桌台状态，使用 @Transactional 确保同时成功或失败。

3. BCrypt 密码加密
用户密码加密存储，登录时使用 BCrypt.checkpw() 校验。
__
4. Sa-Token 统一认证
自定义拦截器，对所有接口进行登录校验，白名单放行登录和文档路径。

5. MyBatis-Plus 自动填充
MetaObjectHandler 自动维护 createTime、updateTime，减少重复代码。

🤝 贡献
欢迎提交 Issue 或 Pull Request。

📝 许可证
MIT License

