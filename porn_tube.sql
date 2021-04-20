/*
 Navicat Premium Data Transfer

 Source Server         : mjm
 Source Server Type    : MySQL
 Source Server Version : 80021
 Source Host           : 192.168.1.107:3306
 Source Schema         : porn_tube

 Target Server Type    : MySQL
 Target Server Version : 80021
 File Encoding         : 65001

 Date: 10/09/2020 18:18:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `img_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片url',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `describes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '简介',
  `view_count` bigint NOT NULL DEFAULT 0 COMMENT '观看次数',
  `like_count` bigint NOT NULL DEFAULT 0 COMMENT '点赞次数',
  `favorite_count` bigint NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `dislike_count` bigint NOT NULL DEFAULT 0 COMMENT '不喜欢次数',
  `examine_status` int NOT NULL COMMENT '审核情况 【0 暂未审核， 1 通过， 2 不通过】向西见枚举类',
  `examine_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '审核意见',
  `category` int NOT NULL COMMENT '分区',
  `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签',
  `user_id` bigint NOT NULL COMMENT '发帖人ID',
  `type` int NOT NULL COMMENT '类型 【0 视频， 1 图片  2 文章】',
  `create_time` bigint NOT NULL COMMENT '发布时间',
  `update_time` bigint NOT NULL COMMENT '更新时间',
  `status` int NOT NULL DEFAULT 0 COMMENT '[0 正常， 1 删除]',
  `score` bigint NULL DEFAULT NULL COMMENT '评分总分',
  `score_count` bigint NULL DEFAULT NULL COMMENT '评分总人数',
  `comment_count` bigint NOT NULL DEFAULT 0 COMMENT '评论人数',
  `danmaku_count` bigint NOT NULL DEFAULT 0 COMMENT '弹幕数',
  `examine_user` bigint NULL DEFAULT NULL COMMENT '审核人id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `find_video_by_category`(`category`) USING BTREE,
  INDEX `find_video_by_title`(`title`) USING BTREE,
  INDEX `find_video_by_user`(`user_id`) USING BTREE,
  INDEX `find_video_by_type`(`type`) USING BTREE,
  INDEX `find_video_by_tag`(`tag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视频，图片，文章 发帖表\r\n\r\nTODO 回复消息可见，加密帖子，视频等' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分区名',
  `type` int NOT NULL COMMENT '分区级别【1 一级分区， 2 二级分区】',
  `father_id` int NULL DEFAULT NULL COMMENT '父级分区',
  `describes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '介绍',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `comment` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论',
  `parent_comment_id` bigint NULL DEFAULT NULL COMMENT '父级评论',
  `parent_user_id` bigint NULL DEFAULT NULL COMMENT '评论对象',
  `like_count` bigint NOT NULL DEFAULT 0 COMMENT '喜欢数',
  `comment_count` bigint NOT NULL COMMENT '评论数',
  `dislike_count` bigint NOT NULL DEFAULT 0 COMMENT '不喜欢',
  `type` int NOT NULL COMMENT '【1 一级评论  2 二级评论】',
  `status` int NOT NULL DEFAULT 0 COMMENT '[0 正常  1 删除]',
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `find_comment_by_artice`(`article_id`) USING BTREE,
  INDEX `find_comment_by_user`(`user_id`) USING BTREE,
  INDEX `find_comment_by_type`(`type`) USING BTREE,
  INDEX `find_comment_by_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for danmaku
-- ----------------------------
DROP TABLE IF EXISTS `danmaku`;
CREATE TABLE `danmaku`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` bigint NOT NULL COMMENT '视频ID',
  `color` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '弹幕颜色',
  `text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '弹幕内容',
  `time` double NOT NULL COMMENT '时间',
  `type` int NULL DEFAULT NULL COMMENT '类型',
  `author` bigint NOT NULL COMMENT '作者',
  `color_dec` bigint NOT NULL COMMENT '十进制颜色数据',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `find_danmaku_by_vido_id`(`video_id`) USING BTREE,
  INDEX `find_danmaku_by_userID`(`video_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '弹幕表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dislike_table
-- ----------------------------
DROP TABLE IF EXISTS `dislike_table`;
CREATE TABLE `dislike_table`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dislike_obj_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` bigint NOT NULL,
  `type` int NOT NULL COMMENT '【0 帖子视频图片， 1 评论】',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `find_like_by_user`(`user_id`) USING BTREE,
  INDEX `find_like_bu_artice`(`dislike_obj_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点踩' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for favorites_table
-- ----------------------------
DROP TABLE IF EXISTS `favorites_table`;
CREATE TABLE `favorites_table`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `artice_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `create_time` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `find_like_by_user`(`user_id`) USING BTREE,
  INDEX `find_like_bu_artice`(`artice_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏夹' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for file_table
-- ----------------------------
DROP TABLE IF EXISTS `file_table`;
CREATE TABLE `file_table`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NULL DEFAULT NULL COMMENT '视频，图片，文章ID， 需要后期更新，没有此项的文件后期需要清楚',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务器保存的文件地址',
  `file_original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件原始名',
  `file_new_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '新的文件名',
  `size` bigint NOT NULL COMMENT '文件大小',
  `upload_time` bigint NOT NULL COMMENT '上传时间',
  `upload_user_id` bigint NOT NULL COMMENT '上传人',
  `type` int NOT NULL COMMENT '文件类型 【0 视频， 1 图片， 2 其它附件, 3 头像数据， 4 顶部大图数据】',
  `suffix_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '后缀名',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `find_file_by_artice_id`(`article_id`) USING BTREE,
  INDEX `find_file_by_userid`(`upload_user_id`) USING BTREE,
  INDEX `find_file_by_type`(`type`) USING BTREE,
  INDEX `find_file_suffix`(`suffix_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for invitation_code
-- ----------------------------
DROP TABLE IF EXISTS `invitation_code`;
CREATE TABLE `invitation_code`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邀请码',
  `create_user` bigint NOT NULL COMMENT '生成邀请码的人',
  `use_user` bigint NULL DEFAULT NULL COMMENT '使用邀请吗的人',
  `use_status` int NOT NULL DEFAULT 0 COMMENT '【1 未被使用， 0 已经被使用】',
  `create_time` bigint NOT NULL COMMENT '生成时间',
  `use_time` bigint NULL DEFAULT NULL COMMENT '使用时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `find_code_by_code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for like_table
-- ----------------------------
DROP TABLE IF EXISTS `like_table`;
CREATE TABLE `like_table`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `like_obj_id` bigint NOT NULL COMMENT '喜欢的对象ID',
  `user_id` bigint NOT NULL,
  `create_time` bigint NOT NULL,
  `type` int NOT NULL COMMENT '【0 视频图片文章主帖子  1 评论】',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `find_like_by_user`(`user_id`) USING BTREE,
  INDEX `find_like_bu_artice`(`like_obj_id`) USING BTREE,
  INDEX `find_like_by_type`(`type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for login_log
-- ----------------------------
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `login_time` bigint NOT NULL COMMENT '登录时间',
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录IP',
  `ua` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '浏览器UA',
  `userId` bigint NOT NULL COMMENT '登录用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `find_user_log_by_id_index`(`userId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `notifier` bigint NOT NULL COMMENT '通知发送人ID',
  `receiver` bigint NOT NULL COMMENT '通知接收人ID',
  `outer_id` bigint NOT NULL COMMENT '外部ID，如主帖子ID',
  `comment_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评论内容',
  `comment_id` bigint NULL DEFAULT -1 COMMENT '评论目标ID',
  `type` int NOT NULL COMMENT '类型 【0 回复帖子， 1 回复评论，2 收到点赞  3 系统通知】',
  `create_time` bigint NOT NULL,
  `status` int NOT NULL DEFAULT 0 COMMENT '【0 未读， 1 已读】',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `find_notification_by_notifier`(`notifier`) USING BTREE,
  INDEX `find_notification_by_receiver`(`receiver`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for play_recording
-- ----------------------------
DROP TABLE IF EXISTS `play_recording`;
CREATE TABLE `play_recording`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL COMMENT '视频ID',
  `file_id` bigint NULL DEFAULT NULL COMMENT '观看到第几个视频',
  `video_time` double NULL DEFAULT NULL COMMENT '时间戳',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  `update_time` bigint NOT NULL COMMENT '更新时间',
  `video_id` bigint NOT NULL COMMENT '视频ID',
  `ua` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器ua',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `find_play_by_user_id`(`user_id`) USING BTREE,
  INDEX `find_play_by_artice`(`article_id`) USING BTREE,
  INDEX `find_play_by_vido`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '播放记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for temp_file
-- ----------------------------
DROP TABLE IF EXISTS `temp_file`;
CREATE TABLE `temp_file`  (
  `ID` bigint NOT NULL,
  `CREATE_TIME` bigint NULL DEFAULT NULL,
  `DATE` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `FILENAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `PATH` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `SIZE` bigint NULL DEFAULT NULL,
  `UPLOAD_FILENAME` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `mail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `create_time` bigint NOT NULL COMMENT '创建时间',
  `submit_count` bigint NOT NULL DEFAULT 0 COMMENT '提交视频，图片，文章数',
  `follow_count` bigint NOT NULL DEFAULT 0 COMMENT '关注数',
  `fans_count` bigint NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '/images/avatar/avatar.png' COMMENT '头像',
  `top_img_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '/images/top.png' COMMENT '首页大图url',
  `introduction` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简介',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `find_user_by_mail_index`(`mail`) USING BTREE COMMENT '使用邮箱查找用户',
  INDEX `find_user_by_phone_index`(`phone`) USING BTREE COMMENT '使用手机号查找用户'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `userId` bigint NOT NULL,
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色',
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `vip_start_time` bigint NULL DEFAULT NULL,
  `vip_stop_time` bigint NULL DEFAULT NULL,
  `modified` bigint NOT NULL COMMENT '修改人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for web_setting
-- ----------------------------
DROP TABLE IF EXISTS `web_setting`;
CREATE TABLE `web_setting`  (
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '网站名',
  `open_no_vip_limit` int NOT NULL DEFAULT 1 COMMENT '是否开启非vip每日观看次数限制 [0 关闭， 1 开启]',
  `no_vip_view_count` int NOT NULL DEFAULT 5 COMMENT '非vip 每日观看次数',
  `logo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '/logo.png' COMMENT '网页logo地址',
  `open_invitation_register` int NOT NULL DEFAULT 1 COMMENT '是否开启邀请码注册 【0 关闭， 1开启】',
  `describe` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '网页简短的描述',
  `open_upload_video_add_view_count` int NOT NULL DEFAULT 1 COMMENT '是否开启每日上传视频增加非会员观看次数 【0 关闭， 1开启】',
  `open_examine` int NOT NULL DEFAULT 1 COMMENT '是否开启视频，文章，图片审核 【0 关闭， 1 开启】',
  `id` int NOT NULL AUTO_INCREMENT,
  `create_time` bigint NOT NULL COMMENT '创建时间',
  `home_max_video_count` int NOT NULL DEFAULT 50 COMMENT '首页最大显示数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;


INSERT INTO `category` VALUES (1, '动画', 1, 0, '动画', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (2, '音乐', 1, 0, '音乐', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (3, '舞蹈', 1, 0, '舞蹈', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (4, '知识', 1, 0, '知识', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (5, '生活', 1, 0, '生活', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (6, '时尚', 1, 0, '时尚', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (7, '娱乐', 1, 0, '娱乐', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (8, '放映厅', 1, 0, '放映厅', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (9, '数码', 1, 0, '数码', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (10, '番剧', 2, 1, '番剧', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (11, '原创音乐', 2, 2, '个人或团队制作以音乐为主要原创因素的歌曲或纯音乐', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (12, '翻唱', 2, 2, '一切非官方的人声再演绎歌曲作品', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (13, '舞蹈综合', 2, 3, '收录无法定义到其他舞蹈子分区的舞蹈视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (14, '舞蹈教程', 2, 3, '镜面慢速，动作分解，基础教程等具有教学意义的舞蹈视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (15, '宅舞', 2, 3, '与ACG相关的翻跳、原创舞蹈', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (16, 'MV', 2, 2, '音乐录影带，为搭配音乐而拍摄的短片', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (17, '演奏', 2, 2, '传统或非传统乐器及器材的演奏作品', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (18, '音乐现场', 2, 2, '音乐实况表演视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (19, '说唱', 2, 2, '说唱', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (20, 'VOCALOID·UTAU', 2, 2, '以雅马哈Vocaloid和UTAU引擎为基础，包含其他调教引擎，运用各类音源进行的歌曲创作内容', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (21, '电音', 2, 2, '以电子合成器、音乐软体等产生的电子声响制作的音乐', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (22, '音乐融合', 2, 2, '收录无法定义到其他音乐子分区的音乐视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (23, 'MAD·AMV', 2, 1, '具有一定制作程度的动画或静画的二次创作视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (24, 'MMD·3D', 2, 1, '使用MMD（MikuMikuDance）和其他3D建模类软件制作的视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (25, '短片·手书·配音', 2, 1, '追求创新并具有强烈特色的短片、手书（绘）及ACG相关配音', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (26, '手办·模玩', 2, 1, '手办模玩的测评、改造或其他衍生内容', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (27, '特摄', 2, 1, '特摄相关衍生视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (28, '综合', 2, 1, '以动画及动画相关内容为素材，包括但不仅限于音频替换、杂谈、排行榜等内容', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (29, '街舞', 2, 3, '收录街舞相关内容，包括赛事现场、舞室作品、个人翻跳、FREESTYLE等', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (30, '明星舞蹈', 2, 3, '国内外明星发布的官方舞蹈及其翻跳内容', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (31, '中国舞', 2, 3, '传承中国艺术文化的舞蹈内容，包括古典舞、民族民间舞、汉唐舞、古风舞等', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (32, '科学科普', 2, 4, '回答你的十万个为什么', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (33, '社科人文', 2, 4, '聊聊互联网社会法律，看看历史趣闻艺术，品品文化心理人物', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (34, '财经', 2, 4, '宏观经济分析，证券市场动态，商业帝国故事，知识与财富齐飞~', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (35, '校园学习', 2, 4, '老师很有趣，同学多人才，我们都爱搞学习', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (36, '职业职场', 2, 4, '职场加油站，成为最有料的职场人', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (37, '野生技术协会', 2, 4, '炫酷技能大集合，是时候展现真正的技术了', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (38, '搞笑', 2, 5, '各种沙雕有趣的搞笑剪辑，挑战，表演，配音等视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (39, '日常', 2, 5, '记录日常生活，分享生活故事', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (40, '动物圈', 2, 5, '萌萌的动物都在这里哦', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (41, '手工', 2, 5, '手工制品的制作过程或成品展示、教程、测评类视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (42, '绘画', 2, 5, '绘画过程或绘画教程，以及绘画相关的所有视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (43, '运动', 2, 5, '运动相关的记录、教程、装备评测和精彩瞬间剪辑视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (44, '汽车', 2, 5, '汽车相关资讯、体验、测评、记录和车主生活视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (45, '其它', 2, 5, '对分区归属不明的视频进行归纳整合的特定分区', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (46, '美妆', 2, 6, '涵盖妆容、发型、美甲等教程，彩妆、护肤相关产品测评、分享等', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (47, '服饰', 2, 6, '服饰风格、搭配技巧相关的展示和教程视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (48, '健身', 2, 6, '器械、有氧、拉伸运动等，以达到强身健体、减肥瘦身、形体塑造目的', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (49, 'T台', 2, 6, '发布会走秀现场及模特相关时尚片、采访、后台花絮', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (50, '风尚标', 2, 6, '时尚明星专访、街拍、时尚购物相关知识科普', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (51, '综艺', 2, 7, '国内外有趣的综艺和综艺相关精彩剪辑', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (52, '明星', 2, 7, '娱乐圈动态、明星资讯相关', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (53, '游戏', 1, 0, '游戏', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (54, '单机游戏', 2, 53, '以所有平台（PC、主机、移动端）的单机或联机游戏为主的视频内容，包括游戏预告、CG、实况解说及相关的评测、杂谈与视频剪辑等', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (55, '电子竞技', 2, 53, '具有高对抗性的电子竞技游戏项目，其相关的赛事、实况、攻略、解说、短剧等视频。', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (56, '手机游戏', 2, 53, '以手机及平板设备为主要平台的游戏，其相关的实况、攻略、解说、短剧、演示等视频。', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (57, '网络游戏', 2, 53, '由网络运营商运营的多人在线游戏，以及电子竞技的相关游戏内容。包括赛事、攻略、实况、解说等相关视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (58, '桌游棋牌', 2, 53, '桌游、棋牌、卡牌对战等及其相关电子版游戏的实况、攻略、解说、演示等视频。', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (59, 'GMV', 2, 53, '由游戏素材制作的MV视频。以游戏内容或CG为主制作的，具有一定创作程度的MV类型的视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (60, '音游', 2, 53, '各个平台上，通过配合音乐与节奏而进行的音乐类游戏视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (61, '手机平板', 2, 9, '手机平板、app 和产品教程等相关视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (62, '电脑装机', 2, 9, '电脑、笔记本、装机配件、外设和软件教程等相关视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (63, '摄影摄像', 2, 9, '摄影摄像器材、拍摄剪辑技巧、拍摄作品分享等相关视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (64, '影音智能', 2, 9, '影音设备、智能硬件、生活家电等相关视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (65, '纪录片', 2, 8, '纪录片', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (66, '电影', 2, 8, '电影', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (67, '电视剧', 2, 8, '电视剧', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (68, '综艺', 2, 8, '综艺', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (69, '影视杂谈', 2, 8, '影视评论、解说、吐槽、科普等', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (70, '影视剪辑', 2, 8, '对影视素材进行剪辑再创作的视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (71, '短片', 2, 8, '追求自我表达且具有特色的短片', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (72, '预告·资讯', 2, 8, '影视类相关资讯，预告，花絮等视频', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (73, '资讯', 1, 0, '新闻资讯', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (74, '热点', 2, 73, '全民关注的时政热门资讯', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (75, '环球', 2, 73, '全球范围内发生的具有重大影响力的事件动态', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (76, '社会', 2, 73, '日常生活的社会事件、社会问题、社会风貌的报道', NULL, 0, 0, 0);
INSERT INTO `category` VALUES (77, '综合', 2, 73, '除上述领域外其它垂直领域的综合资讯', NULL, 0, 0, 0);

INSERT INTO `web_setting` VALUES ('PornTube', 1, 5, '/favicon.png', 1, '一个牛逼的视频网站', 1, 1, 1, 0, 50);
