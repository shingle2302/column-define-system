-- ============================================================
-- Column Define System v2.0 — 多系统/多业务/多层级覆盖
-- ============================================================

DROP TABLE IF EXISTS column_share;
DROP TABLE IF EXISTS column_override;
DROP TABLE IF EXISTS cds_role_preset;
DROP TABLE IF EXISTS column_definition;
DROP TABLE IF EXISTS column_group;
DROP TABLE IF EXISTS column_preset;
DROP TABLE IF EXISTS cds_business;
DROP TABLE IF EXISTS cds_system;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;

-- 系统（租户/接入方）
CREATE TABLE cds_system (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(512),
    status VARCHAR(16) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 业务模块
CREATE TABLE cds_business (
    id VARCHAR(64) PRIMARY KEY,
    system_id VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    code VARCHAR(64) NOT NULL,
    description VARCHAR(512),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (system_id) REFERENCES cds_system(id) ON DELETE CASCADE,
    CONSTRAINT uk_business UNIQUE(system_id, code)
);

-- 预设方案（归属到业务模块）
CREATE TABLE column_preset (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    system_id VARCHAR(64) NOT NULL,
    business_id VARCHAR(64),
    allow_cross_group BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 1,
    is_default BOOLEAN DEFAULT FALSE,
    -- 全局约束
    min_visible_columns INT DEFAULT 0,
    max_visible_columns INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (system_id) REFERENCES cds_system(id) ON DELETE CASCADE,
    FOREIGN KEY (business_id) REFERENCES cds_business(id) ON DELETE SET NULL
);

-- 列分组
CREATE TABLE column_group (
    id VARCHAR(64) PRIMARY KEY,
    label VARCHAR(128) NOT NULL,
    parent_id VARCHAR(64),
    preset_id VARCHAR(64) NOT NULL,
    collapsed BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (preset_id) REFERENCES column_preset(id) ON DELETE CASCADE
);

-- 列定义（含约束）
CREATE TABLE column_definition (
    id VARCHAR(64) PRIMARY KEY,
    label VARCHAR(128) NOT NULL,
    field VARCHAR(128) NOT NULL,
    type VARCHAR(32) NOT NULL,
    width INT DEFAULT 150,
    min_width INT DEFAULT 50,
    max_width INT DEFAULT 800,
    sortable BOOLEAN DEFAULT FALSE,
    filterable BOOLEAN DEFAULT FALSE,
    resizable BOOLEAN DEFAULT TRUE,
    visible BOOLEAN DEFAULT TRUE,
    pinned VARCHAR(16),
    locked BOOLEAN DEFAULT FALSE,
    -- 用户操作约束
    allow_hide BOOLEAN DEFAULT TRUE,
    allow_sort BOOLEAN DEFAULT TRUE,
    allow_resize BOOLEAN DEFAULT TRUE,
    allow_pin BOOLEAN DEFAULT TRUE,
    -- 文本对齐: left=居左, center=居中, right=居右
    align VARCHAR(16) DEFAULT 'left',
    -- 校验规则（validation_json 内可包含 required 等规则）
    validation_json CLOB,
    options_json CLOB,
    default_value VARCHAR(256),
    meta_json CLOB,
    group_id VARCHAR(64),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES column_group(id) ON DELETE CASCADE
);

-- 角色级预设覆盖
CREATE TABLE cds_role_preset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    preset_id VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL,
    column_id VARCHAR(64) NOT NULL,
    override_order INT,
    override_width INT,
    override_visible BOOLEAN,
    override_pinned VARCHAR(16),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_role_preset UNIQUE(preset_id, role, column_id)
);

-- 用户级覆盖
CREATE TABLE column_override (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    preset_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    column_id VARCHAR(64) NOT NULL,
    override_order INT,
    override_width INT,
    override_visible BOOLEAN,
    override_pinned VARCHAR(16),
    version INT DEFAULT 1,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_override UNIQUE(preset_id, user_id, column_id)
);

-- 分享
CREATE TABLE column_share (
    token VARCHAR(64) PRIMARY KEY,
    preset_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    override_data CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- 用户
CREATE TABLE sys_user (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    display_name VARCHAR(128),
    role VARCHAR(32) DEFAULT 'user',
    system_id VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (system_id) REFERENCES cds_system(id) ON DELETE SET NULL
);

-- 角色
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE,
    description VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
