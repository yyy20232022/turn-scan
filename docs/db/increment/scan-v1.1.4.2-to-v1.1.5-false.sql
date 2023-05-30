USE
`scan_platon`;
alter table address
    add column `erc1155_tx_qty` int(11)   NOT NULL DEFAULT '0' COMMENT 'erc1155 token对应的交易数' after `proposal_qty`;

alter table network_stat
    add column `erc1155_tx_qty` int(11)          NOT NULL DEFAULT '0' COMMENT 'erc1155 token对应的交易数' after  `avg_pack_time`;

alter table token
    add column `is_support_erc1155` tinyint(1)  NOT NULL COMMENT '是否支持erc1155接口： 0-不支持 1-支持' after `is_support_erc721_metadata`;

alter table token
    add column `is_support_erc1155_metadata` tinyint(1)  NOT NULL COMMENT '是否支持erc1155 metadata接口： 0-不支持 1-支持' after `is_support_erc1155`;

alter table tx_bak
    add column `erc1155_tx_info` longtext COMMENT 'erc1155交易列表信息' after  `info`;

alter table tx_erc_721_bak
    add column `token_id` varchar(255) NOT NULL COMMENT 'tokenId' after `contract`;

DROP TABLE IF EXISTS `token_1155_holder`;
CREATE TABLE `token_1155_holder`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `token_address`      varchar(64)  NOT NULL COMMENT '合约地址',
    `token_id`           varchar(128) NOT NULL COMMENT 'ERC1155的tokenId',
    `address`            varchar(64)  NOT NULL COMMENT '用户地址',
    `balance`            varchar(128)          DEFAULT NULL COMMENT '地址代币余额，job更新',
    `token_owner_tx_qty` int(11) DEFAULT '0' COMMENT 'owner的交易次数，agent更新',
    `create_time`        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY uk_tokenAddress_tokenId_address (`token_address`, `token_id`, `address`)
);

DROP TABLE IF EXISTS `token_1155_inventory`;
CREATE TABLE `token_1155_inventory`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `token_address` varchar(64)  NOT NULL COMMENT '合约地址',
    `token_id`      varchar(128) NOT NULL COMMENT 'token id',
    `token_url`     longtext COMMENT 'url',
    `decimal`       int(20) DEFAULT NULL COMMENT '精度',
    `name`          varchar(256)          DEFAULT NULL COMMENT 'Identifies the asset to which this NFT represents',
    `description`   longtext COMMENT 'Describes the asset to which this NFT represents',
    `image`         varchar(256)          DEFAULT NULL COMMENT 'A URI pointing to a resource with mime type image/* representing the asset to which this NFT represents. Consider making any images at a width between 320 and 1080 pixels and aspect ratio between 1.91:1 and 4:5 inclusive.',
    `small_image`   varchar(256)          DEFAULT NULL COMMENT '缩略图',
    `medium_image`  varchar(256)          DEFAULT NULL COMMENT '中等缩略图',
    `token_tx_qty`  int(11) NOT NULL DEFAULT '0' COMMENT 'tokenId的交易次数，同合约累加起来就是合约的交易次数，agent更新',
    `retry_num`     int(10) NOT NULL DEFAULT '0' COMMENT '重试次数',
    `create_time`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tokenAddress_tokenId` (`token_address`, `token_id`)
);

DROP TABLE IF EXISTS `tx_erc_1155_bak`;
CREATE TABLE `tx_erc_1155_bak`
(
    `id`        bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `seq`       bigint(20) NOT NULL COMMENT '序号ID',
    `contract`  varchar(42)  NOT NULL COMMENT '合约地址',
    `token_id`  varchar(255) NOT NULL COMMENT 'tokenId',
    `hash`      varchar(72)  NOT NULL COMMENT '交易哈希',
    `from`      varchar(42)  NOT NULL COMMENT 'from地址',
    `from_type` int(1) NOT NULL COMMENT '发送方类型',
    `to`        varchar(42)  NOT NULL COMMENT 'to地址',
    `to_type`   int(1) NOT NULL COMMENT '接收方类型',
    `value`     varchar(255) NOT NULL COMMENT '交易value',
    `bn`        bigint(20) DEFAULT NULL COMMENT '区块高度',
    `b_time`    datetime     DEFAULT NULL COMMENT '区块时间',
    `tx_fee`    varchar(255) DEFAULT NULL COMMENT '手续费',
    `remark`    longtext COMMENT '备注',
    PRIMARY KEY (`id`)
) COMMENT ='erc1155交易备份表';


INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (8, 1, 'token_1155_holder', '统计TokenHolder持有者数的断点记录', '0', '2021-12-06 02:47:41', '2021-12-06 02:47:41');
INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (9, 1, 'token_1155_inventory', '增量更新token1155库存信息断点记录', '0', '2021-12-10 02:44:32', '2021-12-10 02:44:32');
INSERT INTO `point_log`(`id`, `type`, `name`, `desc`, `position`, `create_time`, `update_time`)
VALUES (10, 1, 'tx_1155_bak', '从erc1155交易备份表统计地址表和token表交易数的断点记录', '0', '2021-12-10 02:44:32', '2021-12-10 02:44:32');
