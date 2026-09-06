-- =============================================================
-- 餐饮管理系统 数据库初始化脚本
-- MySQL 8.0
-- 执行方式：mysql -uroot -p < init.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS catering DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE catering;

-- 按外键/依赖顺序删除旧表
DROP TABLE IF EXISTS order_detail;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS dining_table;
DROP TABLE IF EXISTS table_area;
DROP TABLE IF EXISTS dish;
DROP TABLE IF EXISTS dish_category;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS sys_user;

-- -------------------------------------------------------------
-- 系统用户
-- -------------------------------------------------------------
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(64)  NOT NULL COMMENT '用户名',
    password    VARCHAR(128) NOT NULL COMMENT 'BCrypt 密码密文',
    real_name   VARCHAR(64)  DEFAULT NULL COMMENT '姓名',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    role_id     BIGINT       DEFAULT NULL COMMENT '角色ID（1管理员 2收银员 3厨师）',
    status      INT          DEFAULT 1 COMMENT '状态：0禁用 1启用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     INT          DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户';

-- -------------------------------------------------------------
-- 菜品分类
-- -------------------------------------------------------------
CREATE TABLE dish_category (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(64) NOT NULL COMMENT '分类名称',
    parent_id   BIGINT      DEFAULT 0 COMMENT '上级分类ID，0为顶级',
    sort        INT         DEFAULT 0 COMMENT '排序',
    status      INT         DEFAULT 1 COMMENT '状态：0停用 1启用',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     INT         DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='菜品分类';

-- -------------------------------------------------------------
-- 菜品
-- -------------------------------------------------------------
CREATE TABLE dish (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    name         VARCHAR(128)  NOT NULL COMMENT '菜品名称',
    category_id  BIGINT        DEFAULT NULL COMMENT '分类ID',
    price        DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '价格',
    image        VARCHAR(255)  DEFAULT NULL COMMENT '图片地址',
    description  VARCHAR(500)  DEFAULT NULL COMMENT '描述',
    status       INT           DEFAULT 1 COMMENT '状态：0下架 1上架',
    stock        INT           DEFAULT 0 COMMENT '库存',
    sales        INT           DEFAULT 0 COMMENT '销量',
    is_recommend INT           DEFAULT 0 COMMENT '是否推荐：0否 1是',
    sort         INT           DEFAULT 0 COMMENT '排序',
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      INT           DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='菜品';

-- -------------------------------------------------------------
-- 用餐区域
-- -------------------------------------------------------------
CREATE TABLE table_area (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(64) NOT NULL COMMENT '区域名称',
    sort        INT         DEFAULT 0 COMMENT '排序',
    status      INT         DEFAULT 1 COMMENT '状态：0停用 1启用',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用餐区域';

-- -------------------------------------------------------------
-- 桌台
-- -------------------------------------------------------------
CREATE TABLE dining_table (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    table_no    VARCHAR(32) NOT NULL COMMENT '桌号',
    area_id     BIGINT      DEFAULT NULL COMMENT '区域ID',
    capacity    INT         DEFAULT 4 COMMENT '可容纳人数',
    status      INT         DEFAULT 0 COMMENT '状态：0空闲 1占用 2维修',
    qr_code_url VARCHAR(255) DEFAULT NULL COMMENT '二维码地址',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     INT         DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_table_no (table_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='桌台';

-- -------------------------------------------------------------
-- 订单主表
-- -------------------------------------------------------------
CREATE TABLE orders (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no        VARCHAR(64)   NOT NULL COMMENT '订单号',
    table_id        BIGINT        DEFAULT NULL COMMENT '桌台ID',
    table_no        VARCHAR(32)   DEFAULT NULL COMMENT '桌号（冗余）',
    member_id       BIGINT        DEFAULT NULL COMMENT '会员ID',
    total_amount    DECIMAL(10, 2) DEFAULT 0 COMMENT '总金额',
    discount_amount DECIMAL(10, 2) DEFAULT 0 COMMENT '优惠金额',
    pay_amount      DECIMAL(10, 2) DEFAULT 0 COMMENT '实付金额',
    pay_type        INT           DEFAULT NULL COMMENT '支付方式：1微信 2支付宝 3现金 4会员余额',
    pay_status      INT           DEFAULT 0 COMMENT '支付状态：0未支付 1已支付',
    order_status    INT           DEFAULT 1 COMMENT '订单状态：1待接单 2制作中 3已完成 4已取消',
    remark          VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    pay_time        DATETIME      DEFAULT NULL COMMENT '支付时间',
    complete_time   DATETIME      DEFAULT NULL COMMENT '完成时间',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         INT           DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    PRIMARY KEY (id),
    KEY idx_order_no (order_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单主表';

-- -------------------------------------------------------------
-- 订单明细
-- -------------------------------------------------------------
CREATE TABLE order_detail (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id    BIGINT        NOT NULL COMMENT '订单ID',
    dish_id     BIGINT        DEFAULT NULL COMMENT '菜品ID',
    dish_name   VARCHAR(128)  DEFAULT NULL COMMENT '菜品名称（下单快照）',
    price       DECIMAL(10, 2) DEFAULT NULL COMMENT '单价',
    quantity    INT           DEFAULT 1 COMMENT '数量',
    total_price DECIMAL(10, 2) DEFAULT NULL COMMENT '小计',
    taste_remark VARCHAR(255) DEFAULT NULL COMMENT '口味备注',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单明细';

-- -------------------------------------------------------------
-- 会员
-- -------------------------------------------------------------
CREATE TABLE member (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    nickname      VARCHAR(64)   DEFAULT NULL COMMENT '昵称',
    phone         VARCHAR(20)   DEFAULT NULL COMMENT '手机号',
    balance       DECIMAL(10, 2) DEFAULT 0 COMMENT '余额',
    points        INT           DEFAULT 0 COMMENT '积分',
    total_consume DECIMAL(10, 2) DEFAULT 0 COMMENT '累计消费',
    member_level  INT           DEFAULT 1 COMMENT '会员等级：1普通 2银卡 3金卡 4钻石',
    status        INT           DEFAULT 1 COMMENT '状态：0禁用 1正常',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='会员';

-- =============================================================
-- 种子数据
-- =============================================================

-- 管理员账号：admin / 111111（BCrypt 密文）
INSERT INTO sys_user (username, password, real_name, phone, role_id, status) VALUES
('admin',  '$2a$10$ho1x6JRFONi8G9veNjYrBuYhyQZcbR.znncM1AKnCVfPAyViw12Gq', '管理员', '13800000000', 1, 1),
('cashier','$2a$10$ho1x6JRFONi8G9veNjYrBuYhyQZcbR.znncM1AKnCVfPAyViw12Gq', '收银员', '13800000001', 2, 1);

-- 菜品分类
INSERT INTO dish_category (id, name, parent_id, sort, status) VALUES
(1, '热菜', 0, 1, 1),
(2, '凉菜', 0, 2, 1),
(3, '主食', 0, 3, 1),
(4, '饮品', 0, 4, 1);

-- 菜品
INSERT INTO dish (name, category_id, price, image, description, status, stock, sales, is_recommend, sort) VALUES
('鱼香肉丝', 1, 28.00, NULL, '经典川菜，咸甜酸辣', 1, 100, 0, 1, 1),
('宫保鸡丁', 1, 32.00, NULL, '香辣下饭，花生酥脆', 1, 100, 0, 1, 2),
('麻婆豆腐', 1, 18.00, NULL, '麻辣鲜香，豆腐嫩滑', 1, 100, 0, 0, 3),
('红烧肉',   1, 45.00, NULL, '肥而不腻，入口即化', 1, 80,  0, 0, 4),
('拍黄瓜',   2, 12.00, NULL, '清爽解腻', 1, 100, 0, 0, 1),
('凉拌木耳', 2, 15.00, NULL, '爽口开胃', 1, 100, 0, 0, 2),
('米饭',     3, 3.00,  NULL, '东北大米', 1, 500, 0, 0, 1),
('蛋炒饭',   3, 15.00, NULL, '金黄蛋香', 1, 100, 0, 0, 2),
('可乐',     4, 6.00,  NULL, '330ml 罐装', 1, 200, 0, 0, 1),
('鲜榨果汁', 4, 12.00, NULL, '当季水果鲜榨', 1, 100, 0, 0, 2);

-- 用餐区域
INSERT INTO table_area (id, name, sort, status) VALUES
(1, '大厅', 1, 1),
(2, '包间', 2, 1);

-- 桌台
INSERT INTO dining_table (table_no, area_id, capacity, status, qr_code_url) VALUES
('A01', 1, 4, 0, NULL),
('A02', 1, 4, 0, NULL),
('A03', 1, 6, 0, NULL),
('B01', 2, 8, 0, NULL),
('B02', 2, 10, 0, NULL);

-- 会员
INSERT INTO member (nickname, phone, balance, points, total_consume, member_level, status) VALUES
('张三', '13900000001', 200.00, 100, 500.00, 2, 1),
('李四', '13900000002', 50.00,  20,  120.00, 1, 1);
