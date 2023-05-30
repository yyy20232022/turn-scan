USE `scan_platon`;
DROP TABLE IF EXISTS `internal_address`;
CREATE TABLE `internal_address` (
                                    `address` VARCHAR(42) NOT NULL COMMENT '地址',
                                    `type` INT(11) NOT NULL DEFAULT '0' COMMENT '地址类型 :0-基金会账户  1-锁仓合约地址  2-质押合约  3-激励池合约  6-委托奖励池合约 ',
                                    `balance` DECIMAL(65,0) NOT NULL DEFAULT '0' COMMENT '余额(von)',
                                    `restricting_balance` DECIMAL(65,0) NOT NULL DEFAULT '0' COMMENT '锁仓余额(von)',
                                    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    PRIMARY KEY (`address`),
                                    KEY `type` (`type`) USING BTREE
);

-- 初始化数据
INSERT INTO `internal_address` (`address`,`type`)
VALUES ('lat1zqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqp7pn3ep', 1),
       ('lat1zqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqzsjx8h7', 2),
       ('lat1zqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqrdyjj2v', 3),
       ('lat1zqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqxlcypcy', 6);