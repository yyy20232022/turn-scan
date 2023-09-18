package com.platon.browser.enums;

/**
 *  业务初始化枚举
 *  @file RetEnum.java
 *  @description 
 *	@author zhangrj
 *  @data 2019年8月31日
 */
public enum TransactionStatusEnum {

    /** 业务错误码定义 */

    SUCCESS(1,"成功"),
    FAIL(0,"失败");

	/** 描述 */
    private String name;
    /** 错误码 */
    private int code;

    TransactionStatusEnum(int code, String name){
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }
    public int getCode() {
        return code;
    }

    public static TransactionStatusEnum getEnumByCodeValue(int code){
        TransactionStatusEnum[] allEnums = values();
        for(TransactionStatusEnum enableStatus : allEnums){
            if(enableStatus.getCode()==code)
                return enableStatus;
        }
        return null;
    }
}
