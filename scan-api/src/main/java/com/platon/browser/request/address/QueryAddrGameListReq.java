package com.platon.browser.request.address;

import com.platon.browser.request.PageReq;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 *  地址加入游戏列表请求对象
 */
public class QueryAddrGameListReq {
	@NotBlank(message = "{address not null}")
	@Size(min = 42,max = 42)
	private String address;

	private Long roundId;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address.toLowerCase();
	}

	public Long getTableId() {
		return roundId;
	}

	public void setTableId(Long tableId) {
		this.roundId = tableId;
	}
}