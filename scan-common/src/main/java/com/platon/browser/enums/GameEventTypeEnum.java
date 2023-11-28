package com.platon.browser.enums;


/**
 * @Auther: dongqile
 * @Date: 2019/10/31
 * @Description:
 */
public enum GameEventTypeEnum {
    CREATE_GAME_EVENT("getCreateGameEvents"),
    JOIN_GAME_EVENT("getJoinGameEvents"),
    END_GAME_EVENT("getEndGameEvents");

    private String desc;

    GameEventTypeEnum(String desc) {
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
    public static GameEventTypeEnum getErcTypeEnum(String name) {
        for (GameEventTypeEnum e : GameEventTypeEnum.values()) {
            if (e.getDesc().equals(name)) {
                return e;
            }
        }
        return null;
    }

}