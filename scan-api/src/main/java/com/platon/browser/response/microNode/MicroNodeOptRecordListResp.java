package com.platon.browser.response.microNode;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.platon.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;

/**
 * 验证人操作列表返回对象
 *
 * @author zhangrj
 * @file StakingOptRecordListResp.java
 * @description
 * @data 2019年8月31日
 */
public class MicroNodeOptRecordListResp {

    /**
     * 创建时间
     */
    private Long timestamp;

    /**
     * 所属交易
     */
    private String txHash;

    /**
     * 所属区块
     */
    private Long blockNumber;

    /**
     * 类型
     */
    private String type;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
