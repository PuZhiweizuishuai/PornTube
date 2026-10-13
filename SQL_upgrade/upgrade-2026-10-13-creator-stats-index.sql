-- 创作中心「信息中心 / 数据分析」需要的索引（2026-10-13）
-- 已有库执行这一份即可，新库直接用 tik_tube.sql 建表已包含。
-- MySQL 的 DDL 不是幂等的，重复执行会报 Duplicate key name，先按下面的语句确认存在性：
--   SHOW INDEX FROM `play_recording` WHERE Key_name = 'idx_play_article_update';

-- 每日观看人数：按 (稿件, 更新时间) 范围扫描
ALTER TABLE `play_recording`
    ADD INDEX `idx_play_article_update` (`article_id`, `update_time`) USING BTREE;

-- 每日涨粉：follow_user 上原先只有单列索引，加上 create_time 才能走范围扫描
ALTER TABLE `follow`
    ADD INDEX `idx_follow_user_time` (`follow_user`, `create_time`) USING BTREE;

-- 每日点赞（type = 0 为稿件，1 为评论）
ALTER TABLE `like_table`
    ADD INDEX `idx_like_obj_time` (`like_obj_id`, `type`, `create_time`) USING BTREE;

-- 每日评论（status = 0 为正常评论）
ALTER TABLE `comment`
    ADD INDEX `idx_comment_article_time` (`article_id`, `status`, `create_time`) USING BTREE;

-- 每日收藏
ALTER TABLE `favorites_table`
    ADD INDEX `idx_fav_article_time` (`artice_id`, `create_time`) USING BTREE;

-- 每日弹幕
ALTER TABLE `danmaku`
    ADD INDEX `idx_danmaku_video_time` (`video_id`, `create_time`) USING BTREE;
