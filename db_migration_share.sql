-- NoteSpace 分享功能数据库迁移脚本
-- 执行时间: 2026-01-31

-- 添加 share_id 字段到 t_note 表
ALTER TABLE t_note ADD COLUMN share_id VARCHAR(32) UNIQUE COMMENT '分享ID，用于公开访问';

-- 添加索引以优化分享查询
CREATE INDEX idx_share_id ON t_note(share_id);
CREATE INDEX idx_is_shared ON t_note(is_shared);

-- 验证字段是否添加成功
-- DESC t_note;
