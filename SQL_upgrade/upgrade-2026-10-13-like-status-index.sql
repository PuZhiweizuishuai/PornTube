-- 评论列表批量带出点赞状态需要的索引（2026-10-13）
-- 已有库执行这一份即可，新库直接用 tik_tube.sql 建表已包含。
-- MySQL 的 DDL 不是幂等的，重复执行会报 Duplicate key name，先按下面的语句确认存在性：
--   SHOW INDEX FROM `like_table` WHERE Key_name = 'idx_like_user_obj';

-- /like/status 与列表接口的批量点赞查询都是 (用户, 类型, 对象) 这三列等值命中，
-- 原先只有三个单列索引，优化器只能挑一个再回表过滤；合成一个复合索引后可整条走索引。
ALTER TABLE `like_table`
    ADD INDEX `idx_like_user_obj` (`user_id`, `type`, `like_obj_id`) USING BTREE;
