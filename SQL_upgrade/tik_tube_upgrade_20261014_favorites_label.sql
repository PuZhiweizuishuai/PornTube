-- =====================================================================
-- TikTube 收藏夹分组功能升级脚本（增量迁移）
-- 前置：已执行 tik_tube_upgrade_20261013.sql（favorites_label.name 已为
--       varchar、favorites_table.favorites_label_id 已存在）
--       若目标库根本没有 favorites_label 表，先按 tik_tube.sql 中
--       favorites_label 的新定义 CREATE TABLE，再执行本脚本
--       （本脚本可重复执行：已存在的列/索引报错可忽略）
-- 适用 MySQL 5.7 / 8.0
-- 注：本脚本已在开发库验证（建表+回填幂等执行通过）
--
-- 变更总览：
--   A. favorites_label 新增 is_public（是否公开）、is_default（是否默认
--      收藏夹，每用户至多一个、不可删除）两列与公开查询索引
--   B. favorites_table 新增 favorites_label_id 查询索引
--   C. 为存量用户补建「默认收藏夹」记录，并把未归组的存量收藏挂进去
--
-- 执行顺序：第一段 DDL → 第二段数据修补 → 第三段验证
-- =====================================================================


-- =====================================================================
-- 第一段：结构变更
-- =====================================================================

ALTER TABLE `favorites_label`
  ADD COLUMN `is_public` int NOT NULL DEFAULT 0 COMMENT '是否公开【0 私密， 1 公开，公开收藏夹在用户首页可见】',
  ADD COLUMN `is_default` int NOT NULL DEFAULT 0 COMMENT '是否默认收藏夹【0 否， 1 是，每个用户至多一个，不可删除】',
  ADD INDEX `idx_favorites_label_public`(`user_id`, `is_public`) USING BTREE;

ALTER TABLE `favorites_table`
  ADD INDEX `idx_favorites_label`(`favorites_label_id`) USING BTREE;


-- =====================================================================
-- 第二段：存量数据（幂等，可重复执行）
-- =====================================================================

-- 2.1 为没有默认收藏夹的用户补建「默认收藏夹」
INSERT INTO favorites_label (`name`, `user_id`, `is_public`, `is_default`, `create_time`)
SELECT '默认收藏夹', u.id, 0, 1, UNIX_TIMESTAMP(NOW(3)) * 1000
FROM `user` u
WHERE NOT EXISTS (
  SELECT 1 FROM favorites_label fl WHERE fl.user_id = u.id AND fl.is_default = 1
);

-- 2.2 未归组的存量收藏挂入各用户的默认收藏夹
UPDATE favorites_table f
INNER JOIN favorites_label fl ON fl.user_id = f.user_id AND fl.is_default = 1
SET f.favorites_label_id = fl.id
WHERE f.favorites_label_id IS NULL;


-- =====================================================================
-- 第三段：升级后验证
-- =====================================================================

-- 3.1 每个用户恰好一个默认收藏夹（两列应相等）
SELECT (SELECT COUNT(*) FROM `user`) AS user_count,
       (SELECT COUNT(*) FROM favorites_label WHERE is_default = 1) AS default_label_count;

-- 3.2 不应再有未归组收藏
SELECT COUNT(*) AS null_label FROM favorites_table WHERE favorites_label_id IS NULL;

-- 3.3 新索引就位
SHOW INDEX FROM `favorites_label` WHERE Key_name = 'idx_favorites_label_public';
SHOW INDEX FROM `favorites_table` WHERE Key_name = 'idx_favorites_label';
