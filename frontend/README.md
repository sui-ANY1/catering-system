# 餐饮管理系统前端（Catering Frontend）

基于 **Vue 3 + Vite + Element Plus + Pinia + Vue Router + Axios** 的餐饮管理系统前端，对接 `catering` 后端（Spring Boot 3 + Sa-Token）。

覆盖 **管理端** 与 **点餐端** 两个端，实现后端全部已暴露接口的页面。

---

## 🚀 快速开始

### 环境要求

- Node.js 18+
- 后端已启动（`http://localhost:8080`）

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

浏览器打开 `http://localhost:5173`。

### 3. 生产构建（可选）

```bash
npm run build
npm run preview
```

---

## 🔌 后端代理说明

开发环境下，前端请求统一走 `/api` 前缀，由 Vite 代理转发到后端并去掉前缀：

```
前端 http://localhost:5173/api/auth/login
  -> 后端 http://localhost:8080/auth/login
```

代理配置见 `vite.config.js` 的 `server.proxy`。若后端地址/端口不同，修改 `target` 即可。

> 前端未做跨域配置，正是为了走代理避免 CORS。若直接改 `baseURL` 指向后端真实地址，需在后端添加 CORS 支持。

---

## 🔐 登录说明

- 登录接口：`POST /auth/login`，请求体 `{ username, password }`，返回 `data` 为 **token 字符串**。
- 前端将 token 存入 `localStorage`，请求拦截器自动以 `token` 请求头携带（Sa-Token `token-name: token`）。
- 管理端接口均需登录；**点餐端接口已免登录**（后端放行了菜品/分类/桌台/区域的只读 GET、购物车 `/cart/**`、提交订单 `/order/submit`）。
- 默认账号：`admin / 111111`（以数据库中 BCrypt 密文对应的明文为准）。

---

## 📄 页面清单

### 管理端（`/`，侧边栏布局）

| 路由 | 页面 | 对应后端 |
|------|------|----------|
| `/dashboard` | 数据看板 | 各 list 接口聚合统计 |
| `/dish` | 菜品管理 | `/dish` CRUD |
| `/category` | 分类管理 | `/category` CRUD |
| `/table` | 桌台管理 | `/table` CRUD |
| `/area` | 区域管理 | `/area` CRUD |
| `/order` | 订单管理 | `/order` CRUD |
| `/member` | 会员管理 | `/member` CRUD |
| `/user` | 用户管理 | `/user` CRUD |

### 点餐端（`/menu?tableId=xx`）

- 免登录，选择空闲桌台 → 按分类浏览上架菜品 → 加入购物车（支持数量加减/单条删除）→ 提交订单。
- 购物车基于桌台（`/cart/*`），提交走 `/order/submit?tableId=`。

---

## 🗂️ 目录结构

```
frontend
├── index.html
├── vite.config.js            # 开发代理 /api -> 8080
├── package.json
└── src
    ├── main.js               # 入口，注册 Element Plus / 图标 / Pinia / Router
    ├── App.vue
    ├── api                   # 各模块接口封装
    │   ├── request.js        # axios 实例：token 注入 + Result 解包 + 401 跳登录
    │   ├── auth.js / dish.js / category.js / table.js / area.js
    │   ├── cart.js / order.js / member.js / user.js
    ├── router/index.js       # 路由 + 登录守卫
    ├── store/user.js         # Pinia：登录态 / token
    ├── styles/index.css      # 全局样式
    ├── utils/constants.js    # 状态字典（桌台/菜品/订单/支付/会员/用户）
    ├── layout/AdminLayout.vue
    └── views
        ├── Login.vue
        ├── admin/            # 管理端 8 个页面
        └── customer/Menu.vue # 点餐端
```

---

## 📌 状态字典（前后端约定）

后端未显式定义的部分枚举，前端在 `src/utils/constants.js` 中按餐饮业务惯例约定，可直接调整：

| 字段 | 取值 |
|------|------|
| 桌台 `status` | 0 空闲 / 1 占用 / 2 维修 |
| 菜品 `status` | 0 下架 / 1 上架 |
| 菜品 `isRecommend` | 0 否 / 1 是 |
| 订单 `orderStatus` | 1 待接单 / 2 制作中 / 3 已完成 / 4 已取消（下单接口默认置 1） |
| 订单 `payStatus` | 0 未支付 / 1 已支付 |
| 订单 `payType` | 1 微信 / 2 支付宝 / 3 现金 / 4 会员余额 |
| 会员 `memberLevel` | 1 普通 / 2 银卡 / 3 金卡 / 4 钻石 |
| 用户 `roleId` | 1 管理员 / 2 收银员 / 3 厨师（后端无角色表，仅约定） |

---

## ⚠️ 注意事项

以下历史遗留问题均已在本轮修复，具体改动：

1. **用户密码加密**：`SysUserServiceImpl` 已重写 `save/updateById`，密码自动 `BCrypt.hashpw`，编辑时留空不改密码。
2. **购物车数量/删除**：新增 `CartItem`（含 `quantity`），`/cart` 增加 `decrease`、`remove`，下单按数量扣库存、写明细。
3. **订单明细接口**：新增 `GET /orderDetail/{orderId}`。
4. **点餐端免登录**：`SaTokenConfig` 放行顾客只读 GET、`/cart/**`、`/order/submit`。
5. **追加点餐**：`createOrder` 仅拦「维修」桌台，占用中可继续下单。
6. **建表脚本**：新增 `sql/init.sql`，含建表 + 种子数据（admin/111111）。

还需注意：

- **清空旧购物车数据**：购物车 value 结构由 `Dish` 改为 `CartItem`，升级前请清空 Redis 中 `cart:table:*` 键，否则旧数据反序列化会失败。
- 角色、状态枚举等仍为前端约定，见下方状态字典。

---

## 🤝 与后端字段对应

前端直接使用后端实体字段（驼峰，`application.yml` 已开启 `map-underscore-to-camel-case`），时间字段返回 ISO 字符串，前端统一格式化展示。
