-- 相关推荐与热门榜改造需要的索引（2026-10-12）
-- 已有库执行这一份即可，新库直接用 tik_tube.sql 建表已包含。
-- MySQL 的 DDL 不是幂等的，重复执行会报 Duplicate key name，先按下面的语句确认存在性。

-- 热门榜按时间窗召回、标签索引按发布时间取最近稿件、首页与分区 feed 按 create_time 倒序分页，
-- 都走这一个复合索引：等值过滤 type/status/examine_status + 范围/排序 create_time
ALTER TABLE `article`
    ADD INDEX `idx_video_create_time` (`type`, `status`, `examine_status`, `create_time`) USING BTREE;

-- 单列的 find_video_by_type 已被上面的复合索引最左前缀覆盖，确认无其它查询依赖后可以删掉
-- ALTER TABLE `article` DROP INDEX `find_video_by_type`;

-- tag 上的单列索引对 LIKE '%xxx%' 没有帮助（前置通配符走不了 B+ 树），
-- 相关推荐的召回已经改到应用内的标签倒排索引，这个索引保留与否按是否有其它精确查询决定
