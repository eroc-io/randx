/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50067
Source Host           : localhost:3306
Source Database       : randx

Target Server Type    : MYSQL
Target Server Version : 50067
File Encoding         : 65001

Date: 2018-12-07 17:54:16
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for proofs
-- ----------------------------
DROP TABLE IF EXISTS `proofs`;
CREATE TABLE `proofs` (
  `id` bigint(20) NOT NULL auto_increment,
  `proof` varchar(255) default '' COMMENT '抽牌的证明',
  `pk` varchar(255) default '' COMMENT '玩家公钥',
  `deckId` varchar(255) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6837 DEFAULT CHARSET=utf8;
