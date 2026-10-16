-- =====================================================================
-- TikTube 播放记录去重与唯一约束升级脚本（增量迁移）
-- 配合代码改动：播放记录改为「内存缓冲 + 定时批量 upsert」落库，
--   upsert 依赖 (user_id, article_id, video_id) 唯一索引才能命中已有行
-- 前置：目标库 play_recording 上按 user_id 的索引名为 `idx_play_user`
--       （即已执行 tik_tube_upgrade_20261013.sql；旧库该索引还叫
--         find_play_by_user_id 时，把第三段的 DROP INDEX 换成旧名）
--       先执行第一段检查确认索引名：SHOW INDEX FROM `play_recording`;
-- 适用 MySQL 5.7 / 8.0（不使用窗口函数）
-- 注：第三段 upsert 语义已在 MySQL 8.4.5 实测（进度为 NULL 时不覆盖已有
--     video_time、create_time 保持首次、update_time 正常推进）
--
-- 为什么必须执行：
--   1. 播放记录原先只靠 PlayRecordingServiceImpl 里的一把 JVM 全局锁去重，
--      而进度上报接口 /api/user/playrecording/save 完全没有加锁，
--      同一 (用户, 稿件, 分P) 在存量库里可能已经有多行重复数据。
--   2. 新代码不再同步 SELECT 判断行是否存在，改为直接 upsert。没有唯一索引时
--      ON DUPLICATE KEY UPDATE 永远不会命中，退化成纯 INSERT，重复行会继续增加。
--
-- 执行顺序：第一段只读检查 → 第二段去重 → 第三段 DDL → 第四段验证
-- 建议在业务低峰或停服窗口执行：先去重再加索引，再加索引后应用旧代码的
--   SELECT-then-INSERT 路径才可能报 1062（仅跨实例并发时，概率极低）。
-- 备份：mysqldump -u root -p --single-transaction tik_tube > play_recording_backup.sql
--
-- 注：去重保留每个键中 update_time 最大的一行（即最近一次观看），未保留行上的
--     续播进度会被丢弃。属于历史脏数据的极小概率损失，不做跨行合并。
-- =====================================================================


-- =====================================================================
-- 第一段：执行前只读检查（逐条执行，确认重复规模）
-- =====================================================================

-- 1.1 存在重复的 (用户, 稿件, 分P) 组合数与需要删除的行数
SELECT COUNT(*) AS duplicated_keys,
       SUM(cnt) - COUNT(*) AS rows_to_delete
FROM (
  SELECT user_id, article_id, video_id, COUNT(*) AS cnt
  FROM play_recording
  GROUP BY user_id, article_id, video_id
  HAVING COUNT(*) > 1
) d;

-- 1.2 抽样看几组重复明细（确认确实是同一用户同一分P的多行，而非脏数据）
SELECT pr.id, pr.user_id, pr.article_id, pr.video_id, pr.video_time,
       pr.create_time, pr.update_time
FROM play_recording pr
INNER JOIN (
  SELECT user_id, article_id, video_id
  FROM play_recording
  GROUP BY user_id, article_id, video_id
  HAVING COUNT(*) > 1
  LIMIT 5
) d ON pr.user_id = d.user_id AND pr.article_id = d.article_id AND pr.video_id = d.video_id
ORDER BY pr.user_id, pr.article_id, pr.video_id, pr.update_time DESC;


-- =====================================================================
-- 第二段：去重（可重复执行，第二次执行时删除 0 行）
-- =====================================================================

-- 2.1 先删掉观看时间早于同组最新记录的行，保留最近一次观看
DELETE pr FROM play_recording pr
INNER JOIN (
  SELECT user_id, article_id, video_id, MAX(update_time) AS keep_update_time
  FROM play_recording
  GROUP BY user_id, article_id, video_id
  HAVING COUNT(*) > 1
) d ON pr.user_id = d.user_id AND pr.article_id = d.article_id AND pr.video_id = d.video_id
WHERE pr.update_time < d.keep_update_time;

-- 2.2 同一 (用户, 稿件, 分P) 仍并列的（update_time 相同），按 id 保留最大的一行
DELETE pr FROM play_recording pr
INNER JOIN (
  SELECT user_id, article_id, video_id, MAX(id) AS keep_id
  FROM play_recording
  GROUP BY user_id, article_id, video_id
  HAVING COUNT(*) > 1
) d ON pr.user_id = d.user_id AND pr.article_id = d.article_id AND pr.video_id = d.video_id
WHERE pr.id <> d.keep_id;


-- =====================================================================
-- 第三段：结构变更
-- =====================================================================

-- 3.1 加唯一约束；idx_play_user(user_id) 是新索引的最左前缀，直接删除避免冗余
--     观看请求走 user_id 过滤 + update_time 排序，与原单列索引的执行计划一致
ALTER TABLE `play_recording`
  ADD UNIQUE INDEX `uk_play_user_article_video`(`user_id`, `article_id`, `video_id`) USING BTREE,
  DROP INDEX `idx_play_user`;


-- =====================================================================
-- 第四段：升级后验证
-- =====================================================================

-- 4.1 应返回 0 行
SELECT user_id, article_id, video_id, COUNT(*) AS cnt
FROM play_recording
GROUP BY user_id, article_id, video_id
HAVING COUNT(*) > 1;

-- 4.2 新索引就位、idx_play_user 已移除
SHOW INDEX FROM `play_recording`;

-- 4.3 历史记录查询仍可走 user_id 前缀
EXPLAIN SELECT id, article_id, video_id, update_time
FROM play_recording WHERE user_id = 1 ORDER BY update_time DESC LIMIT 10;
