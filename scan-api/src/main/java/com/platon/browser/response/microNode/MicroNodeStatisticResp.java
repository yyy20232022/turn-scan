package com.platon.browser.response.microNode;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.platon.browser.config.json.CustomLatSerializer;

import java.math.BigDecimal;

/**
 * 微节点统计参数返回对象
 */
public class MicroNodeStatisticResp {


    /**
     * 质押总数
     */
    private BigDecimal stakingValue;


    @JsonSerialize(using = CustomLatSerializer.class)
    public BigDecimal getStakingValue() {
        return stakingValue;
    }

    public void setStakingValue(BigDecimal stakingValue) {
        this.stakingValue = stakingValue;
    }

}
