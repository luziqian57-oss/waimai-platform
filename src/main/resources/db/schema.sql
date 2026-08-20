-- 外卖平台基础表结构：业务单据统一采用“主表 + 明细表”设计。

CREATE TABLE IF NOT EXISTS wm_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码摘要',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wm_user_username (username),
    UNIQUE KEY uk_wm_user_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户主表';

CREATE TABLE IF NOT EXISTS wm_user_address (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户主表ID',
    contact_name VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    province VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    district VARCHAR(50) NOT NULL,
    detail_address VARCHAR(255) NOT NULL,
    is_default TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_wm_user_address_user (user_id),
    CONSTRAINT fk_wm_user_address_user FOREIGN KEY (user_id) REFERENCES wm_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址明细表';

CREATE TABLE IF NOT EXISTS wm_wechat_identity (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    openid VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wm_wechat_identity_user (user_id),
    UNIQUE KEY uk_wm_wechat_identity_openid (openid),
    CONSTRAINT fk_wm_wechat_identity_user FOREIGN KEY (user_id) REFERENCES wm_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信身份绑定表';

CREATE TABLE IF NOT EXISTS wm_shop (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    shop_name VARCHAR(100) NOT NULL,
    logo_url VARCHAR(500) DEFAULT NULL,
    contact_phone VARCHAR(20) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家主表';

CREATE TABLE IF NOT EXISTS wm_category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    shop_id BIGINT UNSIGNED NOT NULL,
    category_name VARCHAR(50) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_wm_category_shop (shop_id),
    CONSTRAINT fk_wm_category_shop FOREIGN KEY (shop_id) REFERENCES wm_shop (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

CREATE TABLE IF NOT EXISTS wm_product (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    shop_id BIGINT UNSIGNED NOT NULL,
    category_id BIGINT UNSIGNED NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    image_url VARCHAR(500) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_wm_product_shop_category (shop_id, category_id),
    CONSTRAINT fk_wm_product_shop FOREIGN KEY (shop_id) REFERENCES wm_shop (id),
    CONSTRAINT fk_wm_product_category FOREIGN KEY (category_id) REFERENCES wm_category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品主表';

CREATE TABLE IF NOT EXISTS wm_product_sku (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_id BIGINT UNSIGNED NOT NULL COMMENT '商品主表ID',
    sku_name VARCHAR(100) NOT NULL COMMENT '规格名称',
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_wm_product_sku_product (product_id),
    CONSTRAINT fk_wm_product_sku_product FOREIGN KEY (product_id) REFERENCES wm_product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格明细表';

CREATE TABLE IF NOT EXISTS wm_cart (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    shop_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wm_cart_user_shop (user_id, shop_id),
    CONSTRAINT fk_wm_cart_user FOREIGN KEY (user_id) REFERENCES wm_user (id),
    CONSTRAINT fk_wm_cart_shop FOREIGN KEY (shop_id) REFERENCES wm_shop (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车主表';

CREATE TABLE IF NOT EXISTS wm_cart_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    cart_id BIGINT UNSIGNED NOT NULL COMMENT '购物车主表ID',
    product_id BIGINT UNSIGNED NOT NULL,
    sku_id BIGINT UNSIGNED NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wm_cart_item_sku (cart_id, sku_id),
    CONSTRAINT fk_wm_cart_item_cart FOREIGN KEY (cart_id) REFERENCES wm_cart (id) ON DELETE CASCADE,
    CONSTRAINT fk_wm_cart_item_product FOREIGN KEY (product_id) REFERENCES wm_product (id),
    CONSTRAINT fk_wm_cart_item_sku FOREIGN KEY (sku_id) REFERENCES wm_product_sku (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车明细表';

CREATE TABLE IF NOT EXISTS wm_order (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(40) NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    shop_id BIGINT UNSIGNED NOT NULL,
    address_id BIGINT UNSIGNED DEFAULT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payable_amount DECIMAL(10,2) NOT NULL,
    order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wm_order_no (order_no),
    KEY idx_wm_order_user_status (user_id, order_status),
    CONSTRAINT fk_wm_order_user FOREIGN KEY (user_id) REFERENCES wm_user (id),
    CONSTRAINT fk_wm_order_shop FOREIGN KEY (shop_id) REFERENCES wm_shop (id),
    CONSTRAINT fk_wm_order_address FOREIGN KEY (address_id) REFERENCES wm_user_address (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

CREATE TABLE IF NOT EXISTS wm_order_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL COMMENT '订单主表ID',
    product_id BIGINT UNSIGNED NOT NULL,
    sku_id BIGINT UNSIGNED NOT NULL,
    product_name VARCHAR(100) NOT NULL COMMENT '下单时商品名称快照',
    sku_name VARCHAR(100) NOT NULL COMMENT '下单时规格名称快照',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '下单时单价快照',
    quantity INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_wm_order_item_order (order_id),
    CONSTRAINT fk_wm_order_item_order FOREIGN KEY (order_id) REFERENCES wm_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_wm_order_item_product FOREIGN KEY (product_id) REFERENCES wm_product (id),
    CONSTRAINT fk_wm_order_item_sku FOREIGN KEY (sku_id) REFERENCES wm_product_sku (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- 本地演示数据：全部通过 NOT EXISTS 保证重复启动不会重复插入。
INSERT INTO wm_shop (shop_name, logo_url, contact_phone, address, status)
SELECT '星河厨房', 'https://example.com/images/xinghe-kitchen.png', '021-55556666', '上海市浦东新区演示路 88 号', 1
WHERE NOT EXISTS (SELECT 1 FROM wm_shop WHERE shop_name = '星河厨房');

INSERT INTO wm_category (shop_id, category_name, sort_no, status)
SELECT s.id, '招牌饭', 10, 1 FROM wm_shop s
WHERE s.shop_name = '星河厨房'
  AND NOT EXISTS (SELECT 1 FROM wm_category c WHERE c.shop_id = s.id AND c.category_name = '招牌饭');

INSERT INTO wm_category (shop_id, category_name, sort_no, status)
SELECT s.id, '暖心小食', 20, 1 FROM wm_shop s
WHERE s.shop_name = '星河厨房'
  AND NOT EXISTS (SELECT 1 FROM wm_category c WHERE c.shop_id = s.id AND c.category_name = '暖心小食');

INSERT INTO wm_category (shop_id, category_name, sort_no, status)
SELECT s.id, '清爽饮品', 30, 1 FROM wm_shop s
WHERE s.shop_name = '星河厨房'
  AND NOT EXISTS (SELECT 1 FROM wm_category c WHERE c.shop_id = s.id AND c.category_name = '清爽饮品');

INSERT INTO wm_product (shop_id, category_id, product_name, description, image_url, status)
SELECT s.id, c.id, '黑椒牛肉饭', '现炒黑椒牛肉配时蔬和米饭', 'https://example.com/images/beef-rice.png', 1
FROM wm_shop s JOIN wm_category c ON c.shop_id = s.id AND c.category_name = '招牌饭'
WHERE s.shop_name = '星河厨房'
  AND NOT EXISTS (SELECT 1 FROM wm_product p WHERE p.shop_id = s.id AND p.product_name = '黑椒牛肉饭');

INSERT INTO wm_product (shop_id, category_id, product_name, description, image_url, status)
SELECT s.id, c.id, '照烧鸡腿饭', '去骨鸡腿配照烧汁和时蔬', 'https://example.com/images/chicken-rice.png', 1
FROM wm_shop s JOIN wm_category c ON c.shop_id = s.id AND c.category_name = '招牌饭'
WHERE s.shop_name = '星河厨房'
  AND NOT EXISTS (SELECT 1 FROM wm_product p WHERE p.shop_id = s.id AND p.product_name = '照烧鸡腿饭');

INSERT INTO wm_product (shop_id, category_id, product_name, description, image_url, status)
SELECT s.id, c.id, '黄金鸡块', '外酥里嫩的六块装鸡块', 'https://example.com/images/chicken-bites.png', 1
FROM wm_shop s JOIN wm_category c ON c.shop_id = s.id AND c.category_name = '暖心小食'
WHERE s.shop_name = '星河厨房'
  AND NOT EXISTS (SELECT 1 FROM wm_product p WHERE p.shop_id = s.id AND p.product_name = '黄金鸡块');

INSERT INTO wm_product (shop_id, category_id, product_name, description, image_url, status)
SELECT s.id, c.id, '青柠气泡水', '青柠风味无酒精气泡饮', 'https://example.com/images/lime-soda.png', 1
FROM wm_shop s JOIN wm_category c ON c.shop_id = s.id AND c.category_name = '清爽饮品'
WHERE s.shop_name = '星河厨房'
  AND NOT EXISTS (SELECT 1 FROM wm_product p WHERE p.shop_id = s.id AND p.product_name = '青柠气泡水');

INSERT INTO wm_product_sku (product_id, sku_name, price, stock, status)
SELECT p.id, '标准份', 32.00, 100, 1 FROM wm_product p JOIN wm_shop sh ON sh.id = p.shop_id
WHERE sh.shop_name = '星河厨房' AND p.product_name = '黑椒牛肉饭'
  AND NOT EXISTS (SELECT 1 FROM wm_product_sku s WHERE s.product_id = p.id AND s.sku_name = '标准份');

INSERT INTO wm_product_sku (product_id, sku_name, price, stock, status)
SELECT p.id, '加肉份', 42.00, 60, 1 FROM wm_product p JOIN wm_shop sh ON sh.id = p.shop_id
WHERE sh.shop_name = '星河厨房' AND p.product_name = '黑椒牛肉饭'
  AND NOT EXISTS (SELECT 1 FROM wm_product_sku s WHERE s.product_id = p.id AND s.sku_name = '加肉份');

INSERT INTO wm_product_sku (product_id, sku_name, price, stock, status)
SELECT p.id, '标准份', 28.00, 100, 1 FROM wm_product p JOIN wm_shop sh ON sh.id = p.shop_id
WHERE sh.shop_name = '星河厨房' AND p.product_name = '照烧鸡腿饭'
  AND NOT EXISTS (SELECT 1 FROM wm_product_sku s WHERE s.product_id = p.id AND s.sku_name = '标准份');

INSERT INTO wm_product_sku (product_id, sku_name, price, stock, status)
SELECT p.id, '六块装', 15.00, 120, 1 FROM wm_product p JOIN wm_shop sh ON sh.id = p.shop_id
WHERE sh.shop_name = '星河厨房' AND p.product_name = '黄金鸡块'
  AND NOT EXISTS (SELECT 1 FROM wm_product_sku s WHERE s.product_id = p.id AND s.sku_name = '六块装');

INSERT INTO wm_product_sku (product_id, sku_name, price, stock, status)
SELECT p.id, '常温', 8.00, 200, 1 FROM wm_product p JOIN wm_shop sh ON sh.id = p.shop_id
WHERE sh.shop_name = '星河厨房' AND p.product_name = '青柠气泡水'
  AND NOT EXISTS (SELECT 1 FROM wm_product_sku s WHERE s.product_id = p.id AND s.sku_name = '常温');

INSERT INTO wm_product_sku (product_id, sku_name, price, stock, status)
SELECT p.id, '冰镇', 8.00, 200, 1 FROM wm_product p JOIN wm_shop sh ON sh.id = p.shop_id
WHERE sh.shop_name = '星河厨房' AND p.product_name = '青柠气泡水'
  AND NOT EXISTS (SELECT 1 FROM wm_product_sku s WHERE s.product_id = p.id AND s.sku_name = '冰镇');
