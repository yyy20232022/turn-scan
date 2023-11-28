package com.platon.browser.cache;

import com.platon.browser.dao.entity.AddrGame;
import com.platon.browser.elasticsearch.dto.Transaction;
import com.platon.browser.response.RespPage;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏桌缓存dto
 */
public class AddrGameCacheDto {

	public AddrGameCacheDto() {
		this.addrGameList = new ArrayList<>();
	}
	/**
	 * 交易构造初始方法
	 * @param page
	 */
	public AddrGameCacheDto(List<AddrGame> addrGameList, RespPage page) {
		this.addrGameList = addrGameList;
		this.page = page;
	}
	private List<AddrGame> addrGameList;
	
	private RespPage page;

	public List<AddrGame> getAddrGameList() {
		return addrGameList;
	}

	public void setAddrGameList(List<AddrGame> addrGameList) {
		this.addrGameList = addrGameList;
	}

	public RespPage getPage() {
		return page;
	}

	public void setPage(RespPage page) {
		this.page = page;
	}
}
