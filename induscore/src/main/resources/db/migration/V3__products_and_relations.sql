-- V3: 产品管理表 + 产品与生产线关联表

-- 产品表
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL COMMENT '产品名称',
    model VARCHAR(64) NOT NULL UNIQUE COMMENT '产品型号',
    category VARCHAR(32) COMMENT '产品分类 steel/aluminum/plastic',
    dimensions VARCHAR(128) COMMENT '尺寸规格',
    material VARCHAR(64) COMMENT '材质',
    weight DOUBLE COMMENT '重量(kg)',
    surface_treatment VARCHAR(64) COMMENT '表面处理',
    standard VARCHAR(128) COMMENT '检测标准(国标)',
    threshold DOUBLE COMMENT '缺陷容忍阈值(%)',
    target_yield DOUBLE COMMENT '目标良率(%)',
    status VARCHAR(16) NOT NULL DEFAULT 'active' COMMENT '状态 active/inactive',
    image_url VARCHAR(255) COMMENT '产品图片URL',
    description VARCHAR(512) COMMENT '产品描述',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- 产品缺陷类型配置表（一个产品可配置多种缺陷检测类型）
CREATE TABLE product_defect_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '关联产品ID',
    name VARCHAR(64) NOT NULL COMMENT '缺陷类型名称',
    threshold DOUBLE COMMENT '检测阈值(%)',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_pdc_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品缺陷类型配置';

-- 产品与生产线关联表（多对多关系）
CREATE TABLE product_line_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '产品ID',
    production_line_id BIGINT NOT NULL COMMENT '生产线ID',
    priority INT DEFAULT 0 COMMENT '优先级（数字越小优先级越高）',
    is_primary BOOLEAN DEFAULT FALSE COMMENT '是否为主生产线',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_product_line (product_id, production_line_id),
    CONSTRAINT fk_plr_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_plr_line FOREIGN KEY (production_line_id) REFERENCES production_lines(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品与生产线关联表';

-- 添加索引优化查询
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_model ON products(model);
CREATE INDEX idx_pdc_product_id ON product_defect_configs(product_id);
CREATE INDEX idx_plr_product_id ON product_line_relations(product_id);
CREATE INDEX idx_plr_line_id ON product_line_relations(production_line_id);
