package com.platon.browser.v0152.bean;

import com.platon.browser.enums.GameTypeEnum;
import lombok.Data;


/**
 * Erc20合约标识
 */
@Data
public class GameContractId {

    private GameTypeEnum typeEnum = GameTypeEnum.UNKNOWN;

    /**
     * 游戏名称
     */
    private String name;

    /**
     * 游戏网址
     */
    private String website;

    /**
     * 游戏介绍
     */
    private String introduce;

    /**
     * 游戏类型
     */
    private String gameType;

}
