package com.platon.browser.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.platon.browser.v0152.bean.BubbleDetailDto;
import org.springframework.stereotype.Service;

/**
 * 获取缓存逻辑具体实现
 */
@Service
public class BubbleCacheService extends CacheBase {

	public BubbleDetailDto getBubbleCache(Long bubbleId) {
		Object addrGameCache = this.redisTemplate.opsForHash().get(redisKeyConfig.getBubbleInfo(), bubbleId);
		if(ObjectUtil.isNotNull(addrGameCache)){
			return JSONObject.parseObject((String) addrGameCache,BubbleDetailDto.class);
		}
		return null;

	}

	/**
	 * 添加bubble缓存
	 * @param bubbleDetailDto
	 */
	public void addBubbleCache(BubbleDetailDto bubbleDetailDto, Long bubbleId) {

		redisTemplate.opsForHash().put(redisKeyConfig.getBubbleInfo(), bubbleId,JSON.toJSONString(bubbleDetailDto));
	}

	/**
	 * 结束游戏退出缓存
	 *
	 * @param bubbleId
	 */
	public void delBubbleCache(Long bubbleId) {
		redisTemplate.opsForHash().delete(redisKeyConfig.getBubbleInfo(), bubbleId);
	}
}
