package com.platon.browser.enums;


/**
 *  返回前端微节点状态枚举
 *  @file StakingStatusEnum.java
 *  @description 
 *	@author zhangrj
 *  @data 2019年8月31日
 */
public enum MicroNodeStatusEnum {
	ALL("all", 0),//全部
	CANDIDATE("candidate" ,1),//候选中
	EXITED("exited",2);//已退出

	private String name;

	private Integer code;

	MicroNodeStatusEnum(String name, Integer code) {
		this.code = code;
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public Integer getCode() {
		return code;
	}

	public static MicroNodeStatusEnum getEnumByCodeValue(int code){
		MicroNodeStatusEnum[] allEnums = values();
		for(MicroNodeStatusEnum microNodeStatusEnum : allEnums){
			if(microNodeStatusEnum.getCode()==code)
				return microNodeStatusEnum;
		}
		return null;
	}

	public static MicroNodeStatusEnum getEnumByName(String name){
		MicroNodeStatusEnum[] allEnums = values();
		for(MicroNodeStatusEnum microNodeStatusEnum : allEnums){
			if(microNodeStatusEnum.getName().equalsIgnoreCase(name))
				return microNodeStatusEnum;
		}
		return null;
	}
}
