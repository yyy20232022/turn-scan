package com.platon.browser.v0152.analyzer;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.platon.browser.bean.ErcToken;
import com.platon.browser.dao.entity.Game;
import com.platon.browser.dao.entity.Token;
import com.platon.browser.dao.mapper.GameMapper;
import com.platon.browser.dao.mapper.TokenMapper;
import com.platon.browser.enums.ErcTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class GameCache {

    Map<String, Game> gameMapCache = new ConcurrentHashMap<>();

    Set<String> gameAddressCache = new ConcurrentHashSet<>();

    @Resource
    private GameMapper gameMapper;

    /**
     * 初始化token地址到缓存
     *
     * @param
     * @return void
     * @date 2021/4/19
     */
    public void init() {
        log.info("初始化token地址到缓存");
        List<Game> games = gameMapper.selectByExample(null);
        games.forEach(game -> {
            gameAddressCache.add(game.getContractAddress());
            gameMapCache.put(game.getContractAddress(), game);
        });
    }
    public Collection<String> getGameAddressCache() {
        return Collections.unmodifiableCollection(gameAddressCache);
    }

}
