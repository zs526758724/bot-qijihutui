/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : telegram-qjht

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 20/12/2024 17:38:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bad
-- ----------------------------
DROP TABLE IF EXISTS `bad`;
CREATE TABLE `bad`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链接',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '广告状态',
  `flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标记（1为头部，2为尾部）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bad
-- ----------------------------

-- ----------------------------
-- Table structure for bcd
-- ----------------------------
DROP TABLE IF EXISTS `bcd`;
CREATE TABLE `bcd`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '车队标题',
  `cddesc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '车队描述',
  `minisubscription` int(11) NULL DEFAULT NULL COMMENT '最低订阅量',
  `miniread` int(11) NULL DEFAULT NULL COMMENT '最低阅读',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '车队类型',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 112 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '车队列表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bcd
-- ----------------------------

-- ----------------------------
-- Table structure for bchannelht
-- ----------------------------
DROP TABLE IF EXISTS `bchannelht`;
CREATE TABLE `bchannelht`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
  `chatid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '频道id',
  `count` int(11) NULL DEFAULT NULL COMMENT '人数',
  `createtime` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请时间',
  `shtime` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核时间',
  `submitterid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提交者id',
  `reviewersid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核者id',
  `audit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否审核通过（0为待审核，1为审核通过）',
  `invitelink` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邀请链接',
  `cdid` int(11) NULL DEFAULT NULL COMMENT '车队id',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '普通链接',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1149 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '频道信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bchannelht
-- ----------------------------

-- ----------------------------
-- Table structure for bchannelmsg
-- ----------------------------
DROP TABLE IF EXISTS `bchannelmsg`;
CREATE TABLE `bchannelmsg`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `chatid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '频道id',
  `messageid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息id',
  `bestmessageid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最新消息id',
  `createdate` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发送消息时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1104 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '频道消息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bchannelmsg
-- ----------------------------

-- ----------------------------
-- Table structure for bcopywriter
-- ----------------------------
DROP TABLE IF EXISTS `bcopywriter`;
CREATE TABLE `bcopywriter`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `textkey` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文案key',
  `textcontent` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文案内容',
  `textentities` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文案格式',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bcopywriter
-- ----------------------------
INSERT INTO `bcopywriter` VALUES (1, 'start01', '频道免费引流日涨万粉不是梦\r\n\r\n使用说明：请将本机器人拉入你的|频道，并赋予管理员权限🔶\r\n📍邀请机器人进频道\r\n\r\n管理权限：发布消息/编辑其他人的消息/删除其他人的消息/邀请其他人权限，缺失权限机器人不能正常工作\r\n\r\n管理权限：发布消息/编辑其他人的消息/删除其他人的消息/邀请其他人权限，缺失权限机器人不能正常工作\r\n\r\n🚫严禁未成年/桖腥/曝力/重口/政治/敏感等内容参与互推‼️', NULL);
INSERT INTO `bcopywriter` VALUES (2, 'start02', '======🏆 车队首页 🏆======\n\n🔗河马互推自助涨粉机器人🔗\n\n使用说明：请将本机器人拉入你的频道，并赋予管理员权限🔶\n管理权限：发布消息/编辑其他人的消息/删除其他人的消息/邀请其他人权限，缺失权限机器人不能正常工作\n\n⚠️严谨发布幼童/人兽/男同/血腥/暴力/重口/政治/军火等活动的参与【详见公告】', NULL);
INSERT INTO `bcopywriter` VALUES (3, 'help', 'helptext', NULL);

-- ----------------------------
-- Table structure for bfansrecordsin
-- ----------------------------
DROP TABLE IF EXISTS `bfansrecordsin`;
CREATE TABLE `bfansrecordsin`  (
  `id` int(255) NOT NULL AUTO_INCREMENT,
  `chatid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `date` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `fanscount` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5605 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '进粉表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bfansrecordsin
-- ----------------------------

-- ----------------------------
-- Table structure for bmydata
-- ----------------------------
DROP TABLE IF EXISTS `bmydata`;
CREATE TABLE `bmydata`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `mykey` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '键',
  `myvalus` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '值',
  `mydesc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '其他数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bmydata
-- ----------------------------
INSERT INTO `bmydata` VALUES (7, 'cdname', '盒马互推', '互推名');
INSERT INTO `bmydata` VALUES (8, 'jlgroupurl', 'https://t.me/hemajiaoliu', '交流群');
INSERT INTO `bmydata` VALUES (9, 'kf', 'https://t.me/xiaoqijibot', '客服');
INSERT INTO `bmydata` VALUES (10, 'shqid', '-1002486589153', '审核群');
INSERT INTO `bmydata` VALUES (11, 'jlqid', '123', '审核通过消息发送交流群');
INSERT INTO `bmydata` VALUES (12, 'mybutton', '🔥加入互推🔥-t.me/hemahutuibot', '按钮广告');
INSERT INTO `bmydata` VALUES (13, 'resetmessagetime', '20', '发送频率（分钟为单位）');
INSERT INTO `bmydata` VALUES (14, 'cdrl', '25', '车队容量');
INSERT INTO `bmydata` VALUES (15, 'zd', '5', '消息最大置顶');
INSERT INTO `bmydata` VALUES (16, 'cdstatus', '1', '车队状态');
INSERT INTO `bmydata` VALUES (17, 'fzpagesize', '22', '分组管理分页');
INSERT INTO `bmydata` VALUES (18, 'pdpagesize', '15', '频道分页');
INSERT INTO `bmydata` VALUES (20, 'pdremove', 'bot was kicked from the channel chat', '频道报错移除条件');

-- ----------------------------
-- Table structure for bpermissions
-- ----------------------------
DROP TABLE IF EXISTS `bpermissions`;
CREATE TABLE `bpermissions`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `chatid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '群id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '公开url',
  `inviteurl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邀请链接',
  `groupstatus` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在群内状态',
  `createtime` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建时间',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型',
  `msgandinvqx` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否拥有消息管理与邀请权限',
  `inviteuserid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邀请者id',
  `count` int(11) NULL DEFAULT NULL COMMENT '人数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1148 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bpermissions
-- ----------------------------

-- ----------------------------
-- Table structure for bteadmin
-- ----------------------------
DROP TABLE IF EXISTS `bteadmin`;
CREATE TABLE `bteadmin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `adminid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '聊天id',
  `enable` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否启用',
  `isuperadmin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否是超级管理员',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bteadmin
-- ----------------------------
INSERT INTO `bteadmin` VALUES (1, '111', '1', '1');

-- ----------------------------
-- Table structure for bvisitors
-- ----------------------------
DROP TABLE IF EXISTS `bvisitors`;
CREATE TABLE `bvisitors`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `fwtime` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '访问时间精确到日期',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `firstname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名',
  `lastname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '姓',
  `userid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `botid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '访问机器人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 817 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '访客表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of bvisitors
-- ----------------------------

-- ----------------------------
-- Table structure for fzzfad
-- ----------------------------
DROP TABLE IF EXISTS `fzzfad`;
CREATE TABLE `fzzfad`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '链接',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '广告状态',
  `flag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标记（1为头部，2为尾部）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广告' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fzzfad
-- ----------------------------

-- ----------------------------
-- Table structure for fzzfcd
-- ----------------------------
DROP TABLE IF EXISTS `fzzfcd`;
CREATE TABLE `fzzfcd`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '车队标题',
  `cddesc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '车队描述',
  `minisubscription` int(11) NULL DEFAULT NULL COMMENT '最低订阅量',
  `miniread` int(11) NULL DEFAULT NULL COMMENT '最低阅读',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '车队类型',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 82 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '车队列表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fzzfcd
-- ----------------------------

-- ----------------------------
-- Table structure for fzzfchannelht
-- ----------------------------
DROP TABLE IF EXISTS `fzzfchannelht`;
CREATE TABLE `fzzfchannelht`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
  `chatid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '频道id',
  `count` int(11) NULL DEFAULT NULL COMMENT '人数',
  `createtime` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请时间',
  `shtime` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核时间',
  `submitterid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提交者id',
  `reviewersid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核者id',
  `audit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否审核通过（0为待审核，1为审核通过）',
  `invitelink` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邀请链接',
  `cdid` int(11) NULL DEFAULT NULL COMMENT '车队id',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '普通链接',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1097 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '频道信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fzzfchannelht
-- ----------------------------

-- ----------------------------
-- Table structure for fzzfchannelmsg
-- ----------------------------
DROP TABLE IF EXISTS `fzzfchannelmsg`;
CREATE TABLE `fzzfchannelmsg`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `chatid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '频道id',
  `messageid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息id',
  `bestmessageid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最新消息id',
  `createdate` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发送消息时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1060 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '频道消息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fzzfchannelmsg
-- ----------------------------

-- ----------------------------
-- Table structure for fzzfcopywriter
-- ----------------------------
DROP TABLE IF EXISTS `fzzfcopywriter`;
CREATE TABLE `fzzfcopywriter`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `textkey` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文案key',
  `textcontent` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文案内容',
  `textentities` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文案格式',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fzzfcopywriter
-- ----------------------------
INSERT INTO `fzzfcopywriter` VALUES (1, 'start01', '欢迎使用:奇迹互推\n\n打造全网审核快，流量最真实的互推机器人\n\n - 发送 /start 开始\n - 将机器人加入频道中，给予必要的权限，即可开启互推涨粉\n \n奇迹互推交流群: @QIJIHUTUI8\n奇迹赚钱交流群:  @qijizhuanqian\n广告投放联系： @xiaoqijibot', NULL);
INSERT INTO `fzzfcopywriter` VALUES (2, 'start02', '======🏆 车队首页 🏆======\n\n🔗奇迹互推自助涨粉机器人🔗\n\n使用说明：请将本机器人拉入你的频道，并赋予管理员权限🔶\n管理权限：发布消息/编辑其他人的消息/删除其他人的消息/邀请其他人权限，缺失权限机器人不能正常工作\n\n⚠️严谨发布幼童/人兽/男同/血腥/暴力/重口/政治/军火等活动的参与【详见公告】', NULL);
INSERT INTO `fzzfcopywriter` VALUES (3, 'help', 'helptext', NULL);

-- ----------------------------
-- Table structure for fzzffansrecordsin
-- ----------------------------
DROP TABLE IF EXISTS `fzzffansrecordsin`;
CREATE TABLE `fzzffansrecordsin`  (
  `id` int(255) NOT NULL AUTO_INCREMENT,
  `chatid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `date` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `fanscount` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5888 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '进粉表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fzzffansrecordsin
-- ----------------------------

-- ----------------------------
-- Table structure for fzzfmydata
-- ----------------------------
DROP TABLE IF EXISTS `fzzfmydata`;
CREATE TABLE `fzzfmydata`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `mykey` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '键',
  `myvalus` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '值',
  `mydesc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '其他数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fzzfmydata
-- ----------------------------
INSERT INTO `fzzfmydata` VALUES (7, 'cdname', '奇迹互推', '互推名');
INSERT INTO `fzzfmydata` VALUES (8, 'jlgroupurl', 'https://t.me/QIJIHUTUI8', '交流群');
INSERT INTO `fzzfmydata` VALUES (9, 'kf', 'https://t.me/xiaoqijibot', '客服');
INSERT INTO `fzzfmydata` VALUES (10, 'shqid', '-1002389567323', '审核群');
INSERT INTO `fzzfmydata` VALUES (11, 'jlqid', '123', '审核通过消息发送交流群');
INSERT INTO `fzzfmydata` VALUES (12, 'mybutton', '👉加入互推👈-https://t.me/qijihutuibot\n❤️匿名分配炮友❤️-https://t.me/+9FPwq2ytcDphZjJl&&❤️不良少女破处❤️-https://t.me/+IGTfXXbVpVlkZjll', '按钮广告');
INSERT INTO `fzzfmydata` VALUES (13, 'resetmessagetime', '20', '发送频率（分钟为单位）');
INSERT INTO `fzzfmydata` VALUES (14, 'cdrl', '25', '车队容量');
INSERT INTO `fzzfmydata` VALUES (15, 'zd', '5', '消息最大置顶');
INSERT INTO `fzzfmydata` VALUES (16, 'cdstatus', '1', '车队状态');
INSERT INTO `fzzfmydata` VALUES (17, 'fzpagesize', '22', '分组管理分页');
INSERT INTO `fzzfmydata` VALUES (18, 'pdpagesize', '15', '频道分页');
INSERT INTO `fzzfmydata` VALUES (20, 'pdremove', 'bot was kicked from the channel chat', '频道报错移除条件');

-- ----------------------------
-- Table structure for fzzfpermissions
-- ----------------------------
DROP TABLE IF EXISTS `fzzfpermissions`;
CREATE TABLE `fzzfpermissions`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `chatid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '群id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '公开url',
  `inviteurl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邀请链接',
  `groupstatus` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在群内状态',
  `createtime` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建时间',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型',
  `msgandinvqx` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否拥有消息管理与邀请权限',
  `inviteuserid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邀请者id',
  `count` int(11) NULL DEFAULT NULL COMMENT '人数',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1096 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fzzfpermissions
-- ----------------------------

-- ----------------------------
-- Table structure for fzzfteadmin
-- ----------------------------
DROP TABLE IF EXISTS `fzzfteadmin`;
CREATE TABLE `fzzfteadmin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `adminid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '聊天id',
  `enable` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否启用',
  `isuperadmin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否是超级管理员',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fzzfteadmin
-- ----------------------------

-- ----------------------------
-- Table structure for fzzfvisitors
-- ----------------------------
DROP TABLE IF EXISTS `fzzfvisitors`;
CREATE TABLE `fzzfvisitors`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `fwtime` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '访问时间精确到日期',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `firstname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名',
  `lastname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '姓',
  `userid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `botid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '访问机器人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 823 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '访客表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of fzzfvisitors
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
