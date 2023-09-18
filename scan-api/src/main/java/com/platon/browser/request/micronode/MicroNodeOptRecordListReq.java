package com.platon.browser.request.micronode;

import com.platon.browser.request.PageReq;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 验证人操作列表请求对象
 */
public class MicroNodeOptRecordListReq extends PageReq{
	@NotBlank(message = "{nodeId not null}")
	@Size(min = 130,max = 130)
    private String nodeId;

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
}