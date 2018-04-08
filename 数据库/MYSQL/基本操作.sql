-- 建表语句

DROP TABLE IF EXISTS `t_day_report_conf`;
CREATE TABLE `t_day_report_conf` (
  `re_conf_unid` bigint(255) NOT NULL  ,
  `re_conf_uuid` varchar(64) DEFAULT NULL,
  `re_conf_cdate` datetime DEFAULT NULL,
  `re_conf_udate` datetime DEFAULT NULL,
  `re_conf_keyword` varchar(255) DEFAULT NULL,
  `re_conf_score` int(11) DEFAULT NULL,
  `re_conf_info_type` varchar(64) DEFAULT NULL,
  `re_conf_danger_type` varchar(64) DEFAULT NULL,
  `re_conf_cuser` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`re_conf_unid`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

-- 修改表字段
ALTER TABLE t_visitor_assess MODIFY COLUMN as_content varchar(1024);

-- 新增表字段
ALTER TABLE jfp_account ADD COLUMN ac_type VARCHAR (32);


