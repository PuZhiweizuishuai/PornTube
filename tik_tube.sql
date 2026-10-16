-- ============================================================
-- TikTube 数据库表结构脚本
-- 字符集 utf8mb4 / 排序规则 utf8mb4_general_ci（兼容 MySQL 5.7 / 8.0）
--
-- 注意：
-- 1. favorites_table.artice_id、login_log.userId、user_role.userId
--    等列名保留原有拼写/驼峰，后端代码（QueryWrapper 列名、Mapper XML）
--    直接引用这些列名，改名需要同步修改代码。
-- 2. 计数类字段冗余存储在业务表中（view_count 等），由定时任务异步维护。
-- 3. 全库无外键约束（FOREIGN_KEY_CHECKS=0），关联完整性由应用层保证。
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `img_url` varchar(255) NOT NULL COMMENT '封面图url',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `describes` text NOT NULL COMMENT '简介',
  `view_count` bigint NOT NULL DEFAULT 0 COMMENT '观看次数',
  `like_count` bigint NOT NULL DEFAULT 0 COMMENT '点赞次数',
  `favorite_count` bigint NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `dislike_count` bigint NOT NULL DEFAULT 0 COMMENT '不喜欢次数',
  `examine_status` int NOT NULL DEFAULT 0 COMMENT '审核情况 【0 暂未审核， 1 通过， 2 不通过】详见枚举类',
  `examine_message` text NULL COMMENT '审核意见',
  `category` int NOT NULL COMMENT '分区',
  `tag` varchar(255) NOT NULL COMMENT '标签（JSON 数组字符串）',
  `user_id` bigint NOT NULL COMMENT '发帖人ID',
  `type` int NOT NULL COMMENT '类型 【0 视频， 1 图片， 2 文章】',
  `create_time` bigint NOT NULL COMMENT '发布时间（毫秒时间戳）',
  `update_time` bigint NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `status` int NOT NULL DEFAULT 0 COMMENT '[0 正常， 1 删除]',
  `score` bigint NOT NULL DEFAULT 0 COMMENT '评分总分',
  `score_count` bigint NOT NULL DEFAULT 0 COMMENT '评分总人数',
  `comment_count` bigint NOT NULL DEFAULT 0 COMMENT '评论人数',
  `danmaku_count` bigint NOT NULL DEFAULT 0 COMMENT '弹幕数',
  `examine_user` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `duration` double NULL DEFAULT NULL COMMENT '视频长度（秒）',
  `pixels_number` bigint NULL DEFAULT NULL COMMENT '像素数，超过1080*1920即为高清',
  `frame_rate` double NULL DEFAULT NULL COMMENT '帧率',
  `ua` varchar(999) NULL DEFAULT NULL COMMENT '发布时浏览器UA',
  `ip` varchar(45) NULL DEFAULT NULL COMMENT '发布时IP地址',
  `city` varchar(255) NULL DEFAULT NULL COMMENT '发布时所在城市',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article_category`(`category`) USING BTREE,
  INDEX `idx_article_title`(`title`) USING BTREE,
  INDEX `idx_article_user`(`user_id`) USING BTREE,
  INDEX `idx_article_tag`(`tag`) USING BTREE,
  -- 热门榜、首页 feed、标签索引构建都按 type+status+examine_status 过滤再按 create_time 排序/取范围
  INDEX `idx_video_create_time`(`type`, `status`, `examine_status`, `create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '视频，图片，文章 发帖表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '分区名',
  `type` int NOT NULL COMMENT '分区级别【1 一级分区， 2 二级分区】',
  `father_id` int NULL DEFAULT NULL COMMENT '父级分区',
  `describes` text NOT NULL COMMENT '介绍',
  `icon` varchar(255) NULL DEFAULT NULL COMMENT '图标',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL COMMENT '所属稿件ID',
  `user_id` bigint NOT NULL COMMENT '评论人ID',
  `comment` longtext NOT NULL COMMENT '评论内容',
  `comment_id` bigint NULL DEFAULT NULL COMMENT '二级评论所属的根评论ID',
  `parent_comment_id` bigint NULL DEFAULT NULL COMMENT '直接父级评论ID',
  `parent_user_id` bigint NULL DEFAULT NULL COMMENT '被回复的评论对象用户ID',
  `like_count` bigint NOT NULL DEFAULT 0 COMMENT '喜欢数',
  `comment_count` bigint NOT NULL DEFAULT 0 COMMENT '评论数',
  `dislike_count` bigint NOT NULL DEFAULT 0 COMMENT '不喜欢数',
  `type` int NOT NULL COMMENT '【1 一级评论， 2 二级评论】',
  `status` int NOT NULL DEFAULT 0 COMMENT '[0 正常， 1 删除]',
  `create_time` bigint NOT NULL COMMENT '评论时间（毫秒时间戳）',
  `update_time` bigint NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `ua` varchar(999) NULL DEFAULT NULL COMMENT '评论时浏览器UA',
  `ip` varchar(45) NULL DEFAULT NULL COMMENT '评论时IP地址',
  `city` varchar(255) NULL DEFAULT NULL COMMENT '评论时所在城市',
  `ai_examine_message` varchar(999) NULL DEFAULT NULL COMMENT 'AI审核意见',
  `ai_examine_token` bigint NULL DEFAULT NULL COMMENT 'AI审核消耗token数',
  `ai_examine_id` int NULL DEFAULT NULL COMMENT '使用的模型ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_comment_article`(`article_id`) USING BTREE,
  INDEX `idx_comment_user`(`user_id`) USING BTREE,
  -- 二级评论列表按根评论ID分页查询（CommentServiceImpl#querySecondComment）
  INDEX `idx_comment_root`(`comment_id`) USING BTREE,
  -- 创作中心按天统计评论新增：(稿件, 正常评论, 时间)
  INDEX `idx_comment_article_time`(`article_id`, `status`, `create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for danmaku
-- ----------------------------
DROP TABLE IF EXISTS `danmaku`;
CREATE TABLE `danmaku`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` bigint NOT NULL COMMENT '视频ID（分P文件ID）',
  `color` varchar(255) NOT NULL COMMENT '弹幕颜色（十六进制）',
  `text` varchar(255) NOT NULL COMMENT '弹幕内容',
  `time` double NOT NULL COMMENT '弹幕出现时间（秒）',
  `type` int NOT NULL DEFAULT 0 COMMENT '弹幕类型【0 滚动， 1 顶部， 2 底部】',
  `author` bigint NOT NULL COMMENT '发送者用户ID',
  `color_dec` bigint NOT NULL COMMENT '十进制颜色数据',
  `status` int NOT NULL DEFAULT 0 COMMENT '[0 正常， 1 删除]',
  `create_time` bigint NOT NULL COMMENT '发送时间（毫秒时间戳）',
  `ua` varchar(999) NULL DEFAULT NULL COMMENT '发送时浏览器UA',
  `ip` varchar(45) NULL DEFAULT NULL COMMENT '发送时IP地址',
  `city` varchar(255) NULL DEFAULT NULL COMMENT '发送时所在城市',
  `ai_examine_message` varchar(999) NULL DEFAULT NULL COMMENT 'AI审核意见',
  `ai_examine_token` bigint NULL DEFAULT NULL COMMENT 'AI审核消耗token数',
  `ai_examine_id` int NULL DEFAULT NULL COMMENT '使用的模型ID',
  PRIMARY KEY (`id`) USING BTREE,
  -- 视频弹幕列表 + 创作中心按天统计弹幕新增，(video_id, create_time) 前缀同时覆盖两处
  INDEX `idx_danmaku_video_time`(`video_id`, `create_time`) USING BTREE,
  INDEX `idx_danmaku_author`(`author`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '弹幕表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dislike_table
-- ----------------------------
DROP TABLE IF EXISTS `dislike_table`;
CREATE TABLE `dislike_table`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dislike_obj_id` bigint NOT NULL COMMENT '点踩对象ID',
  `user_id` bigint NOT NULL COMMENT '点踩用户ID',
  `create_time` bigint NOT NULL COMMENT '点踩时间（毫秒时间戳）',
  `type` int NOT NULL COMMENT '【0 帖子视频图片， 1 评论】',
  PRIMARY KEY (`id`) USING BTREE,
  -- (用户, 类型, 对象) 唯一，防止重复点踩；状态查询按这三列等值命中
  UNIQUE INDEX `uk_dislike_user_obj`(`user_id`, `type`, `dislike_obj_id`) USING BTREE,
  INDEX `idx_dislike_obj`(`dislike_obj_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '点踩' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for favorites_table
-- ----------------------------
DROP TABLE IF EXISTS `favorites_table`;
CREATE TABLE `favorites_table`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `artice_id` bigint NOT NULL COMMENT '收藏的稿件ID（列名拼写保留，代码引用此名）',
  `user_id` bigint NOT NULL COMMENT '收藏用户ID',
  `favorites_label_id` bigint NULL DEFAULT NULL COMMENT '所属收藏夹ID',
  `create_time` bigint NOT NULL COMMENT '收藏时间（毫秒时间戳）',
  PRIMARY KEY (`id`) USING BTREE,
  -- (用户, 稿件) 唯一，toggleFavorites / checkFavorites 的 getOne 查询与防重复收藏
  UNIQUE INDEX `uk_favorites_user_article`(`user_id`, `artice_id`) USING BTREE,
  -- 创作中心按天统计收藏新增
  INDEX `idx_fav_article_time`(`artice_id`, `create_time`) USING BTREE,
  -- 公开收藏夹内容列表 / 删除收藏夹时迁移收藏
  INDEX `idx_favorites_label`(`favorites_label_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '收藏' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for favorites_label
-- 收藏夹分组
-- ----------------------------
DROP TABLE IF EXISTS `favorites_label`;
CREATE TABLE `favorites_label`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '收藏夹名称',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `is_public` int NOT NULL DEFAULT 0 COMMENT '是否公开【0 私密， 1 公开，公开收藏夹在用户首页可见】',
  `is_default` int NOT NULL DEFAULT 0 COMMENT '是否默认收藏夹【0 否， 1 是，每个用户至多一个，不可删除】',
  `create_time` bigint NOT NULL COMMENT '创建时间（毫秒时间戳）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_favorites_label_user`(`user_id`) USING BTREE,
  -- 用户首页展示公开收藏夹
  INDEX `idx_favorites_label_public`(`user_id`, `is_public`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '收藏夹分组' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for file_table
-- ----------------------------
DROP TABLE IF EXISTS `file_table`;
CREATE TABLE `file_table`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NULL DEFAULT NULL COMMENT '关联稿件ID；未关联的临时文件后期会被定时任务清理',
  `file_url` varchar(255) NOT NULL COMMENT '服务器保存的文件地址',
  `file_original_name` varchar(255) NOT NULL COMMENT '文件原始名',
  `file_new_name` varchar(255) NOT NULL COMMENT '新的文件名',
  `size` bigint NOT NULL COMMENT '文件大小（字节）',
  `upload_time` bigint NOT NULL COMMENT '上传时间（毫秒时间戳）',
  `upload_user_id` bigint NOT NULL COMMENT '上传人',
  `type` int NOT NULL COMMENT '文件类型 【0 视频， 1 图片， 2 其它附件， 3 头像数据， 4 顶部大图数据】',
  `suffix_name` varchar(255) NOT NULL COMMENT '后缀名',
  `duration` double NULL DEFAULT NULL COMMENT '视频长度（秒）',
  `status` int NOT NULL DEFAULT 0 COMMENT '文件存储状态 【0 未保存的临时文件，后期删除， 1 保存成功并发布的文件】',
  `height` int NULL DEFAULT NULL COMMENT '视频高度',
  `width` int NULL DEFAULT NULL COMMENT '视频宽度',
  `pixels_number` bigint NULL DEFAULT NULL COMMENT '像素数',
  `frame_rate` double NULL DEFAULT NULL COMMENT '帧率',
  `info` varchar(2000) NULL DEFAULT NULL COMMENT '其他信息',
  `ua` varchar(999) NULL DEFAULT NULL COMMENT '上传时浏览器UA',
  `ip` varchar(45) NULL DEFAULT NULL COMMENT '上传时IP地址',
  `city` varchar(255) NULL DEFAULT NULL COMMENT '上传时所在城市',
  `save_location` int NOT NULL DEFAULT 0 COMMENT '存储位置 【0 默认本地】',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_file_article`(`article_id`) USING BTREE,
  INDEX `idx_file_upload_user`(`upload_user_id`) USING BTREE,
  INDEX `idx_file_type`(`type`) USING BTREE,
  INDEX `idx_file_suffix`(`suffix_name`) USING BTREE,
  INDEX `idx_file_new_name`(`file_new_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for invitation_code
-- ----------------------------
DROP TABLE IF EXISTS `invitation_code`;
CREATE TABLE `invitation_code`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL COMMENT '邀请码',
  `create_user` bigint NOT NULL COMMENT '生成邀请码的人',
  `use_user` bigint NULL DEFAULT NULL COMMENT '使用邀请码的人',
  `use_status` int NOT NULL DEFAULT 0 COMMENT '【1 未被使用， 0 已经被使用】',
  `create_time` bigint NOT NULL COMMENT '生成时间（毫秒时间戳）',
  `use_time` bigint NULL DEFAULT NULL COMMENT '使用时间（毫秒时间戳）',
  PRIMARY KEY (`id`) USING BTREE,
  -- 邀请码按值精确查找（check 接口 getOne），必须唯一
  UNIQUE INDEX `uk_invitation_code`(`code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '注册邀请码' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for like_table
-- ----------------------------
DROP TABLE IF EXISTS `like_table`;
CREATE TABLE `like_table`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `like_obj_id` bigint NOT NULL COMMENT '喜欢的对象ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID',
  `create_time` bigint NOT NULL COMMENT '点赞时间（毫秒时间戳）',
  `type` int NOT NULL COMMENT '【0 视频图片文章主帖子， 1 评论】',
  PRIMARY KEY (`id`) USING BTREE,
  -- (用户, 类型, 对象) 唯一，防止重复点赞；单条与评论列表批量点赞状态查询按这三列等值命中
  UNIQUE INDEX `uk_like_user_obj`(`user_id`, `type`, `like_obj_id`) USING BTREE,
  -- 创作中心按天统计点赞新增：(对象, 稿件/评论, 时间)
  INDEX `idx_like_obj_time`(`like_obj_id`, `type`, `create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '点赞' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for login_log
-- ----------------------------
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `login_time` bigint NOT NULL COMMENT '登录时间（毫秒时间戳）',
  `ip` varchar(45) NOT NULL COMMENT '登录IP',
  `ua` varchar(999) NOT NULL COMMENT '浏览器UA（varchar(255) 会截断长 UA）',
  `userId` bigint NOT NULL COMMENT '登录用户ID（列名驼峰保留，代码引用此名）',
  `city` varchar(255) NULL DEFAULT NULL COMMENT '登录城市',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_login_log_user`(`userId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '登录日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `notifier` bigint NOT NULL COMMENT '通知发送人ID',
  `receiver` bigint NOT NULL COMMENT '通知接收人ID',
  `title` varchar(999) NULL DEFAULT NULL COMMENT '标题',
  `outer_id` bigint NOT NULL COMMENT '外部ID，如主帖子ID',
  `link_message` text NULL COMMENT '消息链接',
  `content` text NULL COMMENT '内容',
  `comment_id` bigint NULL DEFAULT -1 COMMENT '评论目标ID，-1 表示非评论通知',
  `type` int NOT NULL COMMENT '类型 【0 回复帖子， 1 回复评论， 2 收到点赞】',
  `article_id` bigint NULL DEFAULT NULL COMMENT '文章ID',
  `create_time` bigint NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `read_time` bigint NULL DEFAULT NULL COMMENT '已读时间，NULL 表示未读',
  `status` int NOT NULL DEFAULT 0 COMMENT '【0 未读， 1 已读】',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notification_notifier`(`notifier`) USING BTREE,
  -- 通知列表（receiver + status 过滤）与未读数统计
  INDEX `idx_receiver_status`(`receiver`, `status`) USING BTREE,
  INDEX `idx_receiver_create_time`(`receiver`, `create_time`) USING BTREE,
  INDEX `idx_outer_id`(`outer_id`) USING BTREE,
  INDEX `idx_comment_id`(`comment_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for play_recording
-- ----------------------------
DROP TABLE IF EXISTS `play_recording`;
CREATE TABLE `play_recording`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL COMMENT '稿件ID',
  `file_id` bigint NULL DEFAULT NULL COMMENT '观看到第几个视频（分P文件ID）',
  `video_time` double NULL DEFAULT NULL COMMENT '观看进度（秒）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` bigint NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `update_time` bigint NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `video_id` bigint NOT NULL COMMENT '视频ID（分P文件ID）',
  `ua` varchar(999) NULL DEFAULT NULL COMMENT '浏览器UA',
  PRIMARY KEY (`id`) USING BTREE,
  -- 用户 x 稿件 x 分P 唯一：播放记录落库走 INSERT ... ON DUPLICATE KEY UPDATE，靠这个约束去重
  UNIQUE INDEX `uk_play_user_article_video`(`user_id`, `article_id`, `video_id`) USING BTREE,
  -- 创作中心按天统计观看人数：(稿件, 更新时间)，前缀同时覆盖按稿件查询
  INDEX `idx_play_article_update`(`article_id`, `update_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '播放记录（用户 x 分P 唯一，作为观看人数去重依据）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `mail` varchar(255) NOT NULL COMMENT '邮箱',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt 加密后存储）',
  `phone` varchar(45) NULL DEFAULT NULL COMMENT '手机号',
  `create_time` bigint NOT NULL COMMENT '注册时间（毫秒时间戳）',
  `submit_count` bigint NOT NULL DEFAULT 0 COMMENT '提交视频，图片，文章数',
  `follow_count` bigint NOT NULL DEFAULT 0 COMMENT '关注数',
  `fans_count` bigint NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `avatar_url` varchar(255) NOT NULL DEFAULT '/images/avatar/avatar.png' COMMENT '头像',
  `top_img_url` varchar(255) NOT NULL DEFAULT '/images/top.png' COMMENT '首页大图url',
  `introduction` varchar(255) NULL DEFAULT NULL COMMENT '简介',
  `last_publish_time` bigint NULL DEFAULT NULL COMMENT '上次投稿时间',
  `otp` int NOT NULL DEFAULT 0 COMMENT '是否开启totp认证【0 关闭， 1 开启】',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态',
  `block_end_time` bigint NOT NULL DEFAULT 0 COMMENT '封禁截止日期（毫秒时间戳，0 表示未封禁）',
  `otp_secret` varchar(2000) NULL DEFAULT NULL COMMENT 'otp密钥',
  `otp_recovery` varchar(2000) NULL DEFAULT NULL COMMENT 'otp恢复密钥',
  PRIMARY KEY (`id`) USING BTREE,
  -- 注册/登录均按 mail、username、phone 精确查找（getOne），唯一索引防止重复数据导致查询报错
  UNIQUE INDEX `uk_user_mail`(`mail`) USING BTREE,
  UNIQUE INDEX `uk_user_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_user_phone`(`phone`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `userId` bigint NOT NULL COMMENT '用户ID（列名驼峰保留，代码引用此名）',
  `role` varchar(255) NOT NULL COMMENT '角色',
  `create_time` bigint NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `update_time` bigint NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `vip_start_time` bigint NULL DEFAULT NULL COMMENT 'VIP开始时间',
  `vip_stop_time` bigint NULL DEFAULT NULL COMMENT 'VIP结束时间',
  `modified` bigint NOT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE,
  -- 每次鉴权都按 userId 查角色（getOne），必须有索引
  INDEX `idx_user_role_user`(`userId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for follow
-- ----------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `follow_user` bigint NOT NULL COMMENT '被关注的用户ID',
  `create_user` bigint NOT NULL COMMENT '发起关注的用户ID',
  `create_time` bigint NOT NULL COMMENT '关注时间（毫秒时间戳，涨粉按天统计依赖此列）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_follow_user`(`follow_user`) USING BTREE,
  -- (关注人, 被关注人) 唯一，防止重复关注
  UNIQUE INDEX `uk_follow_create_user`(`create_user`, `follow_user`) USING BTREE,
  -- 创作中心按天统计涨粉
  INDEX `idx_follow_user_time`(`follow_user`, `create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '关注关系' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for web_config
-- ----------------------------
DROP TABLE IF EXISTS `web_config`;
CREATE TABLE `web_config`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `json_text` text NULL COMMENT '配置json文件（站点配置以JSON整体存储，取最新一条）',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建用户',
  `create_time` bigint NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `ip` varchar(45) NULL DEFAULT NULL COMMENT '创建时IP地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '配置设置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for advertisement_table
-- ----------------------------
DROP TABLE IF EXISTS `advertisement_table`;
CREATE TABLE `advertisement_table`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL COMMENT '标题',
  `url` varchar(999) NULL DEFAULT NULL COMMENT '链接地址',
  `image_url` varchar(999) NULL DEFAULT NULL COMMENT '图片',
  `content` varchar(999) NULL DEFAULT NULL COMMENT '细节描述',
  `video_url` varchar(999) NULL DEFAULT NULL COMMENT '视频广告地址',
  `create_time` bigint NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `update_time` bigint NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `create_user` bigint NOT NULL COMMENT '创建人',
  `status` int NOT NULL DEFAULT 0 COMMENT '【0 下架， 1 启用】',
  `type` int NOT NULL DEFAULT 0 COMMENT '类型',
  `start_time` bigint NOT NULL DEFAULT 0 COMMENT '投放开始时间（毫秒时间戳）',
  `end_time` bigint NOT NULL DEFAULT 0 COMMENT '投放结束时间（毫秒时间戳）',
  `view_count` bigint NOT NULL DEFAULT 0 COMMENT '点击次数',
  `position` int NOT NULL DEFAULT 0 COMMENT '投放位置',
  PRIMARY KEY (`id`) USING BTREE,
  -- 前台按 (状态, 投放时间段) 查询当前有效广告
  INDEX `idx_ad_status_time`(`status`, `start_time`, `end_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告以及系统公告' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for oss_config
-- ----------------------------
DROP TABLE IF EXISTS `oss_config`;
CREATE TABLE `oss_config`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `config_name` varchar(255) NOT NULL COMMENT '配置名称',
  `bucket_name` varchar(255) NOT NULL COMMENT '存储桶名字',
  `endpoint` varchar(999) NOT NULL COMMENT '端点',
  `access_key` varchar(999) NOT NULL COMMENT '访问密钥',
  `secret_key` varchar(999) NOT NULL COMMENT '私有密钥',
  `region` varchar(255) NULL DEFAULT NULL COMMENT '地区',
  `url_prefix` varchar(255) NULL DEFAULT NULL COMMENT '自定义域名',
  `path_style_access` tinyint(1) NULL DEFAULT NULL COMMENT 'Endpoint 访问风格 【0 Path Style， 1 Virtual Hosted Style】',
  `other` varchar(5000) NULL DEFAULT NULL COMMENT '其它参数配置',
  `create_time` bigint NULL DEFAULT NULL COMMENT '创建时间（毫秒时间戳）',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '【0 关闭， 1 启用】',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updater_id` bigint NULL DEFAULT NULL COMMENT '更新人',
  `update_time` bigint NULL DEFAULT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '对象存储配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for opinion_table
-- 举报以及意见反馈
-- ----------------------------
DROP TABLE IF EXISTS `opinion_table`;
CREATE TABLE `opinion_table`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_id` bigint NOT NULL COMMENT '被举报的目标',
  `user_id` bigint NOT NULL COMMENT '举报人',
  `user_opinion` text NULL COMMENT '用户举报原因，意见建议',
  `type` int NOT NULL COMMENT '举报类型 【0 稿件， 1 评论， 2 弹幕】',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态 【0 未处理， 1 已处理】',
  `opinion` varchar(999) NULL DEFAULT NULL COMMENT '处理意见',
  `appeal_status` int NULL DEFAULT NULL COMMENT '申诉处理意见 【0 通过， 1 不通过】',
  `opinion_user` bigint NULL DEFAULT NULL COMMENT '处理人',
  `create_time` bigint NOT NULL COMMENT '举报时间（毫秒时间戳）',
  `opinion_time` bigint NULL DEFAULT NULL COMMENT '处理日期（毫秒时间戳）',
  `other_info` text NULL COMMENT '其它信息',
  `ua` varchar(999) NULL DEFAULT NULL COMMENT '举报时浏览器UA',
  `ip` varchar(45) NULL DEFAULT NULL COMMENT '举报时IP地址',
  `city` varchar(255) NULL DEFAULT NULL COMMENT '举报时所在城市',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_opinion_target`(`target_id`) USING BTREE,
  INDEX `idx_opinion_user`(`user_id`) USING BTREE,
  INDEX `idx_opinion_type`(`type`) USING BTREE,
  -- 后台按处理状态分页处理举报
  INDEX `idx_opinion_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '举报以及意见反馈表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_config
-- 大模型API配置
-- ----------------------------
DROP TABLE IF EXISTS `ai_config`;
CREATE TABLE `ai_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL COMMENT '配置名称',
  `api_url` varchar(999) NOT NULL COMMENT 'API地址',
  `api_key` varchar(255) NOT NULL COMMENT 'API密钥',
  `model` varchar(255) NOT NULL COMMENT '模型名',
  `prompt` text NULL COMMENT '审核使用的Prompt（评论与弹幕审核暂时只能使用默认Prompt）',
  `type` int NULL DEFAULT NULL COMMENT '用途 【0 默认， 1 评论弹幕审核】',
  `create_time` bigint NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `create_user` bigint NOT NULL COMMENT '创建人',
  `update_time` bigint NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `update_user` bigint NOT NULL COMMENT '更新人',
  `status` int NOT NULL DEFAULT 0 COMMENT '启用状态 【0 未启用， 1 启用】',
  `use_tokens` bigint NOT NULL DEFAULT 0 COMMENT '该配置已经使用的token数量',
  `max_tokens` bigint NOT NULL DEFAULT 0 COMMENT '最大可用token',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '大模型API配置' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for article_text
-- 保存与稿件关联的文章信息
-- ----------------------------
DROP TABLE IF EXISTS `article_text`;
CREATE TABLE `article_text`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL COMMENT '关联的文章信息',
  `content` longtext NOT NULL COMMENT '文本内容',
  `user_id` bigint NOT NULL COMMENT '作者',
  `type` int NOT NULL DEFAULT 0 COMMENT '文章类型 【0 普通文章， 1 回复可见， 2 加密文章】',
  `password` varchar(255) NULL DEFAULT NULL COMMENT '加密文章密码',
  `create_time` bigint NOT NULL COMMENT '发布时间（毫秒时间戳）',
  `update_time` bigint NOT NULL COMMENT '更新时间（毫秒时间戳）',
  `sort` int NOT NULL DEFAULT 0 COMMENT '段落顺序，越小越靠前',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态 【0 正常， 1 删除】',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article_text_article`(`article_id`) USING BTREE,
  INDEX `idx_article_text_user`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '保存与稿件关联的文章' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 默认分区数据
-- ----------------------------
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (1, '动画', 1, 0, '动画', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (2, '音乐', 1, 0, '音乐', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (3, '舞蹈', 1, 0, '舞蹈', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (4, '知识', 1, 0, '知识', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (5, '生活', 1, 0, '生活', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (6, '时尚', 1, 0, '时尚', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (7, '娱乐', 1, 0, '娱乐', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (8, '放映厅', 1, 0, '放映厅', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (9, '数码', 1, 0, '数码', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (10, '番剧', 2, 1, '番剧', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (11, '原创音乐', 2, 2, '个人或团队制作以音乐为主要原创因素的歌曲或纯音乐', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (12, '翻唱', 2, 2, '一切非官方的人声再演绎歌曲作品', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (13, '舞蹈综合', 2, 3, '收录无法定义到其他舞蹈子分区的舞蹈视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (14, '舞蹈教程', 2, 3, '镜面慢速，动作分解，基础教程等具有教学意义的舞蹈视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (15, '宅舞', 2, 3, '与ACG相关的翻跳、原创舞蹈', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (16, 'MV', 2, 2, '音乐录影带，为搭配音乐而拍摄的短片', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (17, '演奏', 2, 2, '传统或非传统乐器及器材的演奏作品', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (18, '音乐现场', 2, 2, '音乐实况表演视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (19, '说唱', 2, 2, '说唱', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (20, 'VOCALOID·UTAU', 2, 2, '以雅马哈Vocaloid和UTAU引擎为基础，包含其他调教引擎，运用各类音源进行的歌曲创作内容', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (21, '电音', 2, 2, '以电子合成器、音乐软件等产生的电子声响制作的音乐', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (22, '音乐融合', 2, 2, '收录无法定义到其他音乐子分区的音乐视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (23, 'MAD·AMV', 2, 1, '具有一定制作程度的动画或静画的二次创作视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (24, 'MMD·3D', 2, 1, '使用MMD（MikuMikuDance）和其他3D建模类软件制作的视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (25, '短片·手书·配音', 2, 1, '追求创新并具有强烈特色的短片、手书（绘）及ACG相关配音', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (26, '手办·模玩', 2, 1, '手办模玩的测评、改造或其他衍生内容', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (27, '特摄', 2, 1, '特摄相关衍生视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (28, '综合', 2, 1, '以动画及动画相关内容为素材，包括但不仅限于音频替换、杂谈、排行榜等内容', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (29, '街舞', 2, 3, '收录街舞相关内容，包括赛事现场、舞室作品、个人翻跳、FREESTYLE等', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (30, '明星舞蹈', 2, 3, '国内外明星发布的官方舞蹈及其翻跳内容', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (31, '中国舞', 2, 3, '传承中国艺术文化的舞蹈内容，包括古典舞、民族民间舞、汉唐舞、古风舞等', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (32, '科学科普', 2, 4, '回答你的十万个为什么', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (33, '社科人文', 2, 4, '聊聊互联网社会法律，看看历史趣闻艺术，品品文化心理人物', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (34, '财经', 2, 4, '宏观经济分析，证券市场动态，商业帝国故事，知识与财富齐飞~', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (35, '校园学习', 2, 4, '老师很有趣，同学多人才，我们都爱搞学习', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (36, '职业职场', 2, 4, '职场加油站，成为最有料的职场人', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (37, '野生技术协会', 2, 4, '炫酷技能大集合，是时候展现真正的技术了', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (38, '搞笑', 2, 5, '各种沙雕有趣的搞笑剪辑，挑战，表演，配音等视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (39, '日常', 2, 5, '记录日常生活，分享生活故事', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (40, '动物圈', 2, 5, '萌萌的动物都在这里哦', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (41, '手工', 2, 5, '手工制品的制作过程或成品展示、教程、测评类视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (42, '绘画', 2, 5, '绘画过程或绘画教程，以及绘画相关的所有视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (43, '运动', 2, 5, '运动相关的记录、教程、装备评测和精彩瞬间剪辑视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (44, '汽车', 2, 5, '汽车相关资讯、体验、测评、记录和车主生活视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (45, '其它', 2, 5, '对分区归属不明的视频进行归纳整合的特定分区', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (46, '美妆', 2, 6, '涵盖妆容、发型、美甲等教程，彩妆、护肤相关产品测评、分享等', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (47, '服饰', 2, 6, '服饰风格、搭配技巧相关的展示和教程视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (48, '健身', 2, 6, '器械、有氧、拉伸运动等，以达到强身健体、减肥瘦身、形体塑造目的', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (49, 'T台', 2, 6, '发布会走秀现场及模特相关时尚片、采访、后台花絮', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (50, '风尚标', 2, 6, '时尚明星专访、街拍、时尚购物相关知识科普', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (51, '综艺', 2, 7, '国内外有趣的综艺和综艺相关精彩剪辑', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (52, '明星', 2, 7, '娱乐圈动态、明星资讯相关', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (53, '游戏', 1, 0, '游戏', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (54, '单机游戏', 2, 53, '以所有平台（PC、主机、移动端）的单机或联机游戏为主的视频内容，包括游戏预告、CG、实况解说及相关的评测、杂谈与视频剪辑等', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (55, '电子竞技', 2, 53, '具有高对抗性的电子竞技游戏项目，其相关的赛事、实况、攻略、解说、短剧等视频。', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (56, '手机游戏', 2, 53, '以手机及平板设备为主要平台的游戏，其相关的实况、攻略、解说、短剧、演示等视频。', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (57, '网络游戏', 2, 53, '由网络运营商运营的多人在线游戏，以及电子竞技的相关游戏内容。包括赛事、攻略、实况、解说等相关视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (58, '桌游棋牌', 2, 53, '桌游、棋牌、卡牌对战等及其相关电子版游戏的实况、攻略、解说、演示等视频。', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (59, 'GMV', 2, 53, '由游戏素材制作的MV视频。以游戏内容或CG为主制作的，具有一定创作程度的MV类型的视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (60, '音游', 2, 53, '各个平台上，通过配合音乐与节奏而进行的音乐类游戏视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (61, '手机平板', 2, 9, '手机平板、app 和产品教程等相关视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (62, '电脑装机', 2, 9, '电脑、笔记本、装机配件、外设和软件教程等相关视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (63, '摄影摄像', 2, 9, '摄影摄像器材、拍摄剪辑技巧、拍摄作品分享等相关视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (64, '影音智能', 2, 9, '影音设备、智能硬件、生活家电等相关视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (65, '纪录片', 2, 8, '纪录片', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (66, '电影', 2, 8, '电影', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (67, '电视剧', 2, 8, '电视剧', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (68, '综艺', 2, 8, '综艺', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (69, '影视杂谈', 2, 8, '影视评论、解说、吐槽、科普等', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (70, '影视剪辑', 2, 8, '对影视素材进行剪辑再创作的视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (71, '短片', 2, 8, '追求自我表达且具有特色的短片', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (72, '预告·资讯', 2, 8, '影视类相关资讯，预告，花絮等视频', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (73, '资讯', 1, 0, '新闻资讯', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (74, '热点', 2, 73, '全民关注的时政热门资讯', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (75, '环球', 2, 73, '全球范围内发生的具有重大影响力的事件动态', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (76, '社会', 2, 73, '日常生活的社会事件、社会问题、社会风貌的报道', NULL, 0, 0, 0);
INSERT INTO `category` (`id`, `name`, `type`, `father_id`, `describes`, `icon`, `sort`, `create_time`, `update_time`) VALUES (77, '综合', 2, 73, '除上述领域外其它垂直领域的综合资讯', NULL, 0, 0, 0);
