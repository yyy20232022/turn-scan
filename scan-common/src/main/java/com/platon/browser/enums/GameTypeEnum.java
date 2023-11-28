package com.platon.browser.enums;

/**
 * @Auther: dongqile
 * @Date: 2019/10/31
 * @Description:
 */
public enum GameTypeEnum {
    UNKNOWN("unknown"),
    GAME("game");

    private String desc;

    GameTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据名称获取枚举
     *
     * @param name
     * @return com.platon.browser.v0151.enums.ErcTypeEnum
     * @date 2021/1/19
     */
    public static GameTypeEnum getErcTypeEnum(String name) {
        for (GameTypeEnum e : GameTypeEnum.values()) {
            if (e.getDesc().equals(name)) {
                return e;
            }
        }
        return GameTypeEnum.UNKNOWN;
    }

}