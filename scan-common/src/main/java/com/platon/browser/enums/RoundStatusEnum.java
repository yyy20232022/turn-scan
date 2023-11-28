package com.platon.browser.enums;

/**
 * @Auther: Chendongming
 * @Date: 2019/8/12 19:53
 * @Description:
 */
public enum RoundStatusEnum {
    START(1, "开始"),
    END(0, "结束");

    private int code;
    private String desc;

    RoundStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
