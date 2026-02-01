-- 创建数据库
CREATE DATABASE IF NOT EXISTS `notespace` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `notespace`;

-- 用户表
CREATE TABLE IF NOT EXISTS `t_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态(1:正常 0:禁用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX idx_email (email),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 笔记本表
CREATE TABLE IF NOT EXISTS `t_notebook` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '笔记本ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '笔记本名称',
    `icon` VARCHAR(50) DEFAULT 'folder' COMMENT '图标名称',
    `color` VARCHAR(20) DEFAULT '#3b82f6' COMMENT '图标颜色',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记本表';

-- 笔记表
CREATE TABLE IF NOT EXISTS `t_note` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `notebook_id` BIGINT NOT NULL COMMENT '笔记本ID',
    `title` VARCHAR(500) DEFAULT '' COMMENT '标题',
    `content` TEXT COMMENT '内容',
    `preview` TEXT COMMENT '预览文本(前100字)',
    `is_pinned` TINYINT DEFAULT 0 COMMENT '是否固定(1:是 0:否)',
    `is_shared` TINYINT DEFAULT 0 COMMENT '是否共享(1:是 0:否)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX idx_user_id (user_id),
    INDEX idx_notebook_id (notebook_id),
    INDEX idx_update_time (update_time),
    INDEX idx_is_pinned (is_pinned),
    FULLTEXT INDEX ft_title_content (title, content) WITH PARSER ngram,
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    FOREIGN KEY (notebook_id) REFERENCES t_notebook(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';

-- 标签表
CREATE TABLE IF NOT EXISTS `t_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_name (user_id, name),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 笔记标签关联表
CREATE TABLE IF NOT EXISTS `t_note_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `note_id` BIGINT NOT NULL COMMENT '笔记ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_note_tag (note_id, tag_id),
    INDEX idx_note_id (note_id),
    INDEX idx_tag_id (tag_id),
    FOREIGN KEY (note_id) REFERENCES t_note(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES t_tag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记标签关联表';

-- 插入测试数据（密码为: admin123）
INSERT INTO `t_user` (`username`, `email`, `password`, `status`) VALUES
('admin', 'admin@notespace.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1);
