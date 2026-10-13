-- =====================================================================
-- TikTube 生产库结构升级脚本（增量迁移）
-- 目标：将存量库从旧表结构升级到 tik_tube.sql（优化后）的新结构
-- 适用 MySQL 5.7 / 8.0（RENAME INDEX 语法需 5.7+）
--
-- 执行前必读：
--   1. 先执行【第一段】只读检查，人工确认重复数据情况
--   2. 全量备份（DDL 无法回滚）：
--      mysqldump -u root -p --single-transaction tik_tube > tik_tube_backup.sql
--   3. 停机或业务低峰期按段顺序执行，不要跳段
--   4. article / comment / danmaku / like_table 等大表的 MODIFY 与
--      索引变更会触发表重建，预留足够执行窗口
--
-- 变更总览：
--   A. 唯一约束（防重复 + 匹配代码 getOne 语义）：
--      user(mail/username/phone)、invitation_code(code)、
--      like_table / dislike_table / favorites_table 行为去重
--   B. 索引修复：danmaku / play_recording 重复索引删除、
--      comment.comment_id / user_role.userId / advertisement 补索引
--   C. 字段修正：favorites_label.name bigint→varchar、
--      oss_config 去 ZEROFILL、ua 扩容防截断、NOT NULL / DEFAULT 补齐
--   D. 废弃表清理：temp_file（后端已无引用）
--
-- 列名保持不变（代码引用，不可改）：
--   favorites_table.artice_id、login_log.userId、user_role.userId
-- =====================================================================


-- =====================================================================
-- 第一段：执行前只读检查（逐条执行，结果应尽量为空）
-- =====================================================================

-- 1.1 user 表重复账号（重复邮箱/用户名会导致唯一索引创建失败，
--     同时会使后端 getOne 抛 TooManyResultsException）
SELECT mail, COUNT(*) AS cnt FROM `user` GROUP BY mail HAVING COUNT(*) > 1;
SELECT username, COUNT(*) AS cnt FROM `user` GROUP BY username HAVING COUNT(*) > 1;
SELECT phone, COUNT(*) AS cnt FROM `user` WHERE phone IS NOT NULL AND phone <> '' GROUP BY phone HAVING COUNT(*) > 1;
-- 空字符串手机号之间同样会触发唯一冲突
SELECT COUNT(*) AS empty_phone FROM `user` WHERE phone = '';

-- 1.2 invitation_code 重复邀请码
SELECT code, COUNT(*) AS cnt FROM `invitation_code` GROUP BY code HAVING COUNT(*) > 1;

-- 1.3 like_table 重复点赞（同一用户对同一对象重复记录）
SELECT user_id, type, like_obj_id, COUNT(*) AS cnt
FROM `like_table` GROUP BY user_id, type, like_obj_id HAVING COUNT(*) > 1;

-- 1.4 dislike_table 重复点踩
SELECT user_id, type, dislike_obj_id, COUNT(*) AS cnt
FROM `dislike_table` GROUP BY user_id, type, dislike_obj_id HAVING COUNT(*) > 1;

-- 1.5 favorites_table 重复收藏
SELECT user_id, artice_id, COUNT(*) AS cnt
FROM `favorites_table` GROUP BY user_id, artice_id HAVING COUNT(*) > 1;

-- 1.6 NOT NULL 变更前的 NULL 值检查（应返回 0）
SELECT COUNT(*) AS null_score FROM `article` WHERE `score` IS NULL OR `score_count` IS NULL;
SELECT COUNT(*) AS null_field FROM `danmaku` WHERE `type` IS NULL OR `status` IS NULL;
SELECT COUNT(*) AS null_field FROM `user` WHERE `otp` IS NULL OR `status` IS NULL OR `block_end_time` IS NULL;
SELECT COUNT(*) AS null_ct FROM `follow` WHERE `create_time` IS NULL;
SELECT COUNT(*) AS null_ct FROM `advertisement_table`
WHERE `create_time` IS NULL OR `update_time` IS NULL OR `create_user` IS NULL
   OR `status` IS NULL OR `type` IS NULL OR `start_time` IS NULL
   OR `end_time` IS NULL OR `view_count` IS NULL OR `position` IS NULL;
SELECT COUNT(*) AS null_ct FROM `opinion_table` WHERE `create_time` IS NULL;
SELECT COUNT(*) AS null_st FROM `oss_config` WHERE `status` IS NULL;

-- 1.7 收藏夹名称列现有数据确认（bigint 转 varchar 是无损的，
--     但若已有数据说明业务曾以数字写入，执行后注意核对）
SELECT * FROM `favorites_label` LIMIT 20;


-- =====================================================================
-- 第二段：数据修补（去重 + NULL 填充）
-- 说明：本段为 DML，可放入事务执行；下面每个 DELETE 都保留 id 最小
--       （最早）的一条记录
-- =====================================================================

-- 2.1 user.phone 空字符串统一转为 NULL（空串之间会触发唯一冲突，
--     NULL 不参与唯一索引比较）
UPDATE `user` SET phone = NULL WHERE phone = '';

-- 2.2 user 表去重
-- ★ 高危操作：删除的是重复注册的脏账号（保留最早注册的一条）。
--   若第一段查出重复，建议先人工核对是否需要合并账号数据
--   （其投稿、角色、关注等关联记录会变成孤立数据），确认后再执行。
--   可选的孤立角色清理见 2.7。
DELETE u1 FROM `user` u1
INNER JOIN `user` u2 ON u1.mail = u2.mail AND u1.id > u2.id;
DELETE u1 FROM `user` u1
INNER JOIN `user` u2 ON u1.username = u2.username AND u1.id > u2.id;
DELETE u1 FROM `user` u1
INNER JOIN `user` u2 ON u1.phone = u2.phone AND u1.id > u2.id;

-- 2.3 invitation_code 去重（保留最早生成的一条）
DELETE c1 FROM `invitation_code` c1
INNER JOIN `invitation_code` c2 ON c1.code = c2.code AND c1.id > c2.id;

-- 2.4 like_table / dislike_table / favorites_table 行为去重
--     （保留最早一条；去重后 article 冗余计数列可能略偏大，
--      属于既有口径问题，不影响约束正确性）
DELETE l1 FROM `like_table` l1
INNER JOIN `like_table` l2
  ON l1.user_id = l2.user_id AND l1.type = l2.type
 AND l1.like_obj_id = l2.like_obj_id AND l1.id > l2.id;

DELETE d1 FROM `dislike_table` d1
INNER JOIN `dislike_table` d2
  ON d1.user_id = d2.user_id AND d1.type = d2.type
 AND d1.dislike_obj_id = d2.dislike_obj_id AND d1.id > d2.id;

DELETE f1 FROM `favorites_table` f1
INNER JOIN `favorites_table` f2
  ON f1.user_id = f2.user_id AND f1.artice_id = f2.artice_id AND f1.id > f2.id;

-- 2.5 NOT NULL 变更前的 NULL 填充（统一填 0，时间 0 表示未知，
--     不参与按时间范围的统计）
UPDATE `article` SET `score` = 0 WHERE `score` IS NULL;
UPDATE `article` SET `score_count` = 0 WHERE `score_count` IS NULL;
UPDATE `danmaku` SET `type` = 0 WHERE `type` IS NULL;
UPDATE `danmaku` SET `status` = 0 WHERE `status` IS NULL;
UPDATE `user` SET `otp` = 0 WHERE `otp` IS NULL;
UPDATE `user` SET `status` = 0 WHERE `status` IS NULL;
UPDATE `user` SET `block_end_time` = 0 WHERE `block_end_time` IS NULL;
UPDATE `follow` SET `create_time` = 0 WHERE `create_time` IS NULL;
UPDATE `opinion_table` SET `create_time` = 0 WHERE `create_time` IS NULL;
UPDATE `oss_config` SET `status` = 0 WHERE `status` IS NULL;

UPDATE `advertisement_table` SET `create_time` = 0 WHERE `create_time` IS NULL;
UPDATE `advertisement_table` SET `update_time` = 0 WHERE `update_time` IS NULL;
UPDATE `advertisement_table` SET `create_user` = 0 WHERE `create_user` IS NULL;
UPDATE `advertisement_table` SET `status` = 0 WHERE `status` IS NULL;
UPDATE `advertisement_table` SET `type` = 0 WHERE `type` IS NULL;
UPDATE `advertisement_table` SET `start_time` = 0 WHERE `start_time` IS NULL;
UPDATE `advertisement_table` SET `end_time` = 0 WHERE `end_time` IS NULL;
UPDATE `advertisement_table` SET `view_count` = 0 WHERE `view_count` IS NULL;
UPDATE `advertisement_table` SET `position` = 0 WHERE `position` IS NULL;

-- 2.6 （可选，仅 2.2 删除过用户时执行）清理孤立的角色记录
-- DELETE ur FROM `user_role` ur
-- LEFT JOIN `user` u ON u.id = ur.`userId`
-- WHERE u.id IS NULL;


-- =====================================================================
-- 第三段：索引调整 + 字段约束变更（DDL，不可回滚，逐表一个 ALTER）
-- =====================================================================

-- 3.1 article：补默认值 + 索引重命名 + 删除低区分度索引
--     （type 单列被 idx_video_create_time 前缀覆盖）
ALTER TABLE `article`
  MODIFY COLUMN `score` bigint NOT NULL DEFAULT 0 COMMENT '评分总分',
  MODIFY COLUMN `score_count` bigint NOT NULL DEFAULT 0 COMMENT '评分总人数',
  MODIFY COLUMN `examine_status` int NOT NULL DEFAULT 0 COMMENT '审核情况 【0 暂未审核， 1 通过， 2 不通过】详见枚举类',
  RENAME INDEX `find_video_by_category` TO `idx_article_category`,
  RENAME INDEX `find_video_by_title` TO `idx_article_title`,
  RENAME INDEX `find_video_by_user` TO `idx_article_user`,
  RENAME INDEX `find_video_by_tag` TO `idx_article_tag`,
  DROP INDEX `find_video_by_type`;

-- 3.2 comment：补二级评论根索引 + 删除低区分度索引
ALTER TABLE `comment`
  RENAME INDEX `find_comment_by_artice` TO `idx_comment_article`,
  RENAME INDEX `find_comment_by_user` TO `idx_comment_user`,
  ADD INDEX `idx_comment_root`(`comment_id`) USING BTREE,
  DROP INDEX `find_comment_by_type`,
  DROP INDEX `find_comment_by_status`;

-- 3.3 danmaku：删除两个重复的 video_id 索引，补 author 索引
--     （原 find_danmaku_by_userID 实际建在 video_id 上，属建表笔误；
--      idx_danmaku_video_time 前缀已覆盖 video_id 单列查询）
ALTER TABLE `danmaku`
  MODIFY COLUMN `type` int NOT NULL DEFAULT 0 COMMENT '弹幕类型【0 滚动， 1 顶部， 2 底部】',
  MODIFY COLUMN `status` int NOT NULL DEFAULT 0 COMMENT '[0 正常， 1 删除]',
  ADD INDEX `idx_danmaku_author`(`author`) USING BTREE,
  DROP INDEX `find_danmaku_by_vido_id`,
  DROP INDEX `find_danmaku_by_userID`;

-- 3.4 dislike_table：唯一约束防重复点踩，替换原单列索引
ALTER TABLE `dislike_table`
  ADD UNIQUE INDEX `uk_dislike_user_obj`(`user_id`, `type`, `dislike_obj_id`) USING BTREE,
  RENAME INDEX `find_like_bu_artice` TO `idx_dislike_obj`,
  DROP INDEX `find_like_by_user`;

-- 3.5 favorites_table：唯一约束防重复收藏（toggle/check 接口 getOne 依赖）
ALTER TABLE `favorites_table`
  ADD UNIQUE INDEX `uk_favorites_user_article`(`user_id`, `artice_id`) USING BTREE,
  DROP INDEX `find_like_by_user`,
  DROP INDEX `find_like_bu_artice`;

-- 3.6 favorites_label：修复 name 列类型（bigint 根本存不了收藏夹名称）
ALTER TABLE `favorites_label`
  MODIFY COLUMN `name` varchar(255) NOT NULL COMMENT '收藏夹名称',
  RENAME INDEX `find_favorites_lable_by_user` TO `idx_favorites_label_user`;

-- 3.7 file_table：索引重命名 + 删除 save_location 低区分度索引
--     （save_location 只有 0/1 两个值，几乎无过滤价值）
ALTER TABLE `file_table`
  RENAME INDEX `find_file_by_artice_id` TO `idx_file_article`,
  RENAME INDEX `find_file_by_userid` TO `idx_file_upload_user`,
  RENAME INDEX `find_file_by_type` TO `idx_file_type`,
  RENAME INDEX `find_file_suffix` TO `idx_file_suffix`,
  RENAME INDEX `find_file_new_name` TO `idx_file_new_name`,
  DROP INDEX `find_file_save_location`;

-- 3.8 invitation_code：邀请码唯一（check 接口按 code getOne）
ALTER TABLE `invitation_code`
  ADD UNIQUE INDEX `uk_invitation_code`(`code`) USING BTREE,
  DROP INDEX `find_code_by_code`;

-- 3.9 like_table：唯一约束防重复点赞，替换原普通组合索引与单列索引
ALTER TABLE `like_table`
  ADD UNIQUE INDEX `uk_like_user_obj`(`user_id`, `type`, `like_obj_id`) USING BTREE,
  DROP INDEX `find_like_by_user`,
  DROP INDEX `find_like_bu_artice`,
  DROP INDEX `find_like_by_type`,
  DROP INDEX `idx_like_user_obj`;

-- 3.10 login_log：ua 扩容防长 UA 截断（varchar 只扩不缩，安全）
ALTER TABLE `login_log`
  MODIFY COLUMN `ua` varchar(999) NOT NULL COMMENT '浏览器UA（varchar(255) 会截断长 UA）',
  RENAME INDEX `find_user_log_by_id_index` TO `idx_login_log_user`;

-- 3.11 notification：receiver 单列索引被 (receiver,status) 前缀覆盖，删除
ALTER TABLE `notification`
  RENAME INDEX `find_notification_by_notifier` TO `idx_notification_notifier`,
  DROP INDEX `find_notification_by_receiver`;

-- 3.12 play_recording：ua 扩容 + 删除两个被覆盖/重复的索引
--      （find_play_by_artice 被 idx_play_article_update 前缀覆盖；
--        find_play_by_vido 与 find_play_by_user_id 完全重复，建表笔误）
ALTER TABLE `play_recording`
  MODIFY COLUMN `ua` varchar(999) NULL DEFAULT NULL COMMENT '浏览器UA',
  RENAME INDEX `find_play_by_user_id` TO `idx_play_user`,
  DROP INDEX `find_play_by_artice`,
  DROP INDEX `find_play_by_vido`;

-- 3.13 user：唯一约束（注册/登录均按 mail/username/phone 精确 getOne，
--      出现重复数据会直接抛 TooManyResultsException）+ 约束补齐
ALTER TABLE `user`
  MODIFY COLUMN `phone` varchar(45) NULL DEFAULT NULL COMMENT '手机号',
  MODIFY COLUMN `otp` int NOT NULL DEFAULT 0 COMMENT '是否开启totp认证【0 关闭， 1 开启】',
  MODIFY COLUMN `status` int NOT NULL DEFAULT 0 COMMENT '状态',
  MODIFY COLUMN `block_end_time` bigint NOT NULL DEFAULT 0 COMMENT '封禁截止日期（毫秒时间戳，0 表示未封禁）',
  DROP INDEX `find_user_by_mail_index`,
  ADD UNIQUE INDEX `uk_user_mail`(`mail`) USING BTREE,
  ADD UNIQUE INDEX `uk_user_username`(`username`) USING BTREE,
  DROP INDEX `find_user_by_phone_index`,
  ADD UNIQUE INDEX `uk_user_phone`(`phone`) USING BTREE;

-- 3.14 user_role：每次鉴权都按 userId 查角色（getOne），补索引
ALTER TABLE `user_role`
  ADD INDEX `idx_user_role_user`(`userId`) USING BTREE;

-- 3.15 follow：create_time 补 NOT NULL（涨粉按天统计依赖此列，
--      NULL 记录会被时间范围统计漏掉）
ALTER TABLE `follow`
  MODIFY COLUMN `create_time` bigint NOT NULL COMMENT '关注时间（毫秒时间戳，涨粉按天统计依赖此列）',
  RENAME INDEX `find_follow_user` TO `idx_follow_user`;

-- 3.16 advertisement_table：约束补齐 + 当前有效广告查询索引
--      （前台按 status + 投放时间段查询）
ALTER TABLE `advertisement_table`
  MODIFY COLUMN `create_time` bigint NOT NULL COMMENT '创建时间（毫秒时间戳）',
  MODIFY COLUMN `update_time` bigint NOT NULL COMMENT '更新时间（毫秒时间戳）',
  MODIFY COLUMN `create_user` bigint NOT NULL COMMENT '创建人',
  MODIFY COLUMN `status` int NOT NULL DEFAULT 0 COMMENT '【0 下架， 1 启用】',
  MODIFY COLUMN `type` int NOT NULL DEFAULT 0 COMMENT '类型',
  MODIFY COLUMN `start_time` bigint NOT NULL DEFAULT 0 COMMENT '投放开始时间（毫秒时间戳）',
  MODIFY COLUMN `end_time` bigint NOT NULL DEFAULT 0 COMMENT '投放结束时间（毫秒时间戳）',
  MODIFY COLUMN `view_count` bigint NOT NULL DEFAULT 0 COMMENT '点击次数',
  MODIFY COLUMN `position` int NOT NULL DEFAULT 0 COMMENT '投放位置',
  ADD INDEX `idx_ad_status_time`(`status`, `start_time`, `end_time`) USING BTREE;

-- 3.17 oss_config：去掉 UNSIGNED ZEROFILL（显示填充无意义）
ALTER TABLE `oss_config`
  MODIFY COLUMN `path_style_access` tinyint(1) NULL DEFAULT NULL COMMENT 'Endpoint 访问风格 【0 Path Style， 1 Virtual Hosted Style】',
  MODIFY COLUMN `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '【0 关闭， 1 启用】';

-- 3.18 opinion_table：create_time 补 NOT NULL + 索引重命名
ALTER TABLE `opinion_table`
  MODIFY COLUMN `create_time` bigint NOT NULL COMMENT '举报时间（毫秒时间戳）',
  RENAME INDEX `find_opinion_target_id` TO `idx_opinion_target`,
  RENAME INDEX `find_opinion_user_id` TO `idx_opinion_user`,
  RENAME INDEX `find_opinion_type` TO `idx_opinion_type`,
  RENAME INDEX `find_opinion_status` TO `idx_opinion_status`;

-- 3.19 article_text：索引重命名
ALTER TABLE `article_text`
  RENAME INDEX `find_article_text_by_article_id` TO `idx_article_text_article`,
  RENAME INDEX `find_article_text_by_user_id` TO `idx_article_text_user`;


-- =====================================================================
-- 第四段（可选）：ip 列统一缩短为 varchar(45)（IPv6 最长标准长度）
-- ★ 大表（article/comment/danmaku/login_log）执行 MODIFY 会走 COPY
--   算法锁表重建，仅在存储敏感且有空闲窗口时执行；跳过本段不影响
--   业务与代码运行，仅与新库建表脚本存在列长差异
-- =====================================================================

-- 4.1 前置检查：存在超长脏数据时先人工处理，否则 MODIFY 会失败
SELECT COUNT(*) AS too_long_ip FROM `article` WHERE CHAR_LENGTH(`ip`) > 45;
SELECT COUNT(*) AS too_long_ip FROM `comment` WHERE CHAR_LENGTH(`ip`) > 45;
SELECT COUNT(*) AS too_long_ip FROM `danmaku` WHERE CHAR_LENGTH(`ip`) > 45;
SELECT COUNT(*) AS too_long_ip FROM `file_table` WHERE CHAR_LENGTH(`ip`) > 45;
SELECT COUNT(*) AS too_long_ip FROM `login_log` WHERE CHAR_LENGTH(`ip`) > 45;
SELECT COUNT(*) AS too_long_ip FROM `web_config` WHERE CHAR_LENGTH(`ip`) > 45;
SELECT COUNT(*) AS too_long_ip FROM `opinion_table` WHERE CHAR_LENGTH(`ip`) > 45;

-- 4.2 确认检查全部为 0 后再执行
-- ALTER TABLE `article`     MODIFY COLUMN `ip` varchar(45) NULL DEFAULT NULL COMMENT '发布时IP地址';
-- ALTER TABLE `comment`     MODIFY COLUMN `ip` varchar(45) NULL DEFAULT NULL COMMENT '评论时IP地址';
-- ALTER TABLE `danmaku`     MODIFY COLUMN `ip` varchar(45) NULL DEFAULT NULL COMMENT '发送时IP地址';
-- ALTER TABLE `file_table`  MODIFY COLUMN `ip` varchar(45) NULL DEFAULT NULL COMMENT '上传时IP地址';
-- ALTER TABLE `login_log`   MODIFY COLUMN `ip` varchar(45) NOT NULL COMMENT '登录IP';
-- ALTER TABLE `web_config`  MODIFY COLUMN `ip` varchar(45) NULL DEFAULT NULL COMMENT '创建时IP地址';
-- ALTER TABLE `opinion_table` MODIFY COLUMN `ip` varchar(45) NULL DEFAULT NULL COMMENT '举报时IP地址';


-- =====================================================================
-- 第五段：废弃表清理
-- =====================================================================

-- temp_file：后端已无任何引用（临时文件清理走 file_table +
-- DeleteTempFileTasks），确认无自定义用途后删除
DROP TABLE IF EXISTS `temp_file`;

-- 历史遗留的旧拼写表（若存在）
DROP TABLE IF EXISTS `favorites_lable`;

-- web_setting 已在代码中弃用（配置改为 web_config JSON 整体存储），
-- 确认无其他系统读取后可放开注释清理
-- DROP TABLE IF EXISTS `web_setting`;


-- =====================================================================
-- 第六段：升级后验证
-- =====================================================================

-- 6.1 唯一索引全部就位
SHOW INDEX FROM `user` WHERE Key_name IN ('uk_user_mail', 'uk_user_username', 'uk_user_phone');
SHOW INDEX FROM `like_table` WHERE Key_name = 'uk_like_user_obj';
SHOW INDEX FROM `dislike_table` WHERE Key_name = 'uk_dislike_user_obj';
SHOW INDEX FROM `favorites_table` WHERE Key_name = 'uk_favorites_user_article';
SHOW INDEX FROM `invitation_code` WHERE Key_name = 'uk_invitation_code';

-- 6.2 新增索引就位
SHOW INDEX FROM `comment` WHERE Key_name = 'idx_comment_root';
SHOW INDEX FROM `user_role` WHERE Key_name = 'idx_user_role_user';
SHOW INDEX FROM `danmaku` WHERE Key_name = 'idx_danmaku_author';
SHOW INDEX FROM `advertisement_table` WHERE Key_name = 'idx_ad_status_time';

-- 6.3 NOT NULL 生效（均应返回 0）
SELECT COUNT(*) AS should_be_0 FROM `article` WHERE `score` IS NULL;
SELECT COUNT(*) AS should_be_0 FROM `follow` WHERE `create_time` IS NULL;
SELECT COUNT(*) AS should_be_0 FROM `user` WHERE `status` IS NULL;

-- 6.4 核心查询执行计划抽查（应命中对应索引，key 列不为 NULL）
-- EXPLAIN SELECT * FROM `comment` WHERE `comment_id` = 1 LIMIT 20;
-- EXPLAIN SELECT * FROM `user_role` WHERE `userId` = 1;
-- EXPLAIN SELECT * FROM `like_table` WHERE `user_id` = 1 AND `type` = 0 AND `like_obj_id` = 1;
