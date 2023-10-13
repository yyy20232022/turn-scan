package com.platon.browser.request.micronode;

import com.platon.browser.request.PageReq;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 *  微节点列表请求对象
 *  @file AliveStakingListReq.java
 *  @description 
 *	@author zhangrj
 *  @data 2019年8月31日
 */
public class AliveMicroNodeListReq extends PageReq{
    private String key;
    @NotBlank(message = "{queryStatus not null}")
    @Pattern(regexp = "all|candidate|exited", message = "{queryStatus.illegal}")
    private String queryStatus;

	private Long bubbleId;

	public Long getBubbleId() {
		return bubbleId;
	}

	public void setBubbleId(Long bubbleId) {
		this.bubbleId = bubbleId;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getQueryStatus() {
		return queryStatus;
	}
	public void setQueryStatus(String queryStatus) {
		this.queryStatus = queryStatus;
	}
    
}