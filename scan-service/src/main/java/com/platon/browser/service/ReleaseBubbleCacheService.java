package com.platon.browser.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 获取缓存逻辑具体实现
 */
@Service
public class ReleaseBubbleCacheService extends CacheBase {

	/**
	 * 获取bubble释放的缓存信息
	 * @param releaseBubbleBlockNumber
	 * @return
	 */
	public List<Long>  getBubbleReleaseCache(Long releaseBubbleBlockNumber) {
		Object bubbleIdListStr = this.redisTemplate.opsForHash().get(redisKeyConfig.getBubbleInfo(), releaseBubbleBlockNumber);
		List<Long> bubbleIdList = new LinkedList<>();
		if(ObjectUtil.isNotNull(bubbleIdListStr)){
			CopyOnWriteArrayList bubbleIdLists = JSONObject.parseObject((String) bubbleIdListStr,CopyOnWriteArrayList.class);
			bubbleIdLists.forEach(item-> bubbleIdList.add(Long.parseLong(String.valueOf(item))));
		}
		return bubbleIdList;
	}

	/**
	 * 添加bubble释放缓存
	 */
	public void addReleaseBubbleCache(Long releaseBubbleBlockNumber, List<Long> bubbleIdList) {
		redisTemplate.opsForHash().put(redisKeyConfig.getBubbleInfo(), releaseBubbleBlockNumber, JSONObject.toJSONString(bubbleIdList));
	}

	/**
	 * 释放清除缓存
	 * @param releaseBubbleBlockNumber
	 */
	public void delReleaseBubbleCache(Long releaseBubbleBlockNumber) {
		redisTemplate.opsForHash().delete(redisKeyConfig.getBubbleInfo(), releaseBubbleBlockNumber);
	}
}
