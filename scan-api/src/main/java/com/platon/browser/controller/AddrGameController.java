package com.platon.browser.controller;

import com.platon.browser.request.address.QueryAddrGameListReq;
import com.platon.browser.response.BaseResp;
import com.platon.browser.response.RespPage;
import com.platon.browser.response.address.AddrGameDetailResp;
import com.platon.browser.service.AddrGameService;
import com.platon.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;


@Slf4j
@RestController
public class AddrGameController {

    @Resource
    private AddrGameService addrGameService;

    /**
     * 查询地址参与的游戏列表详情
     */
    @PostMapping("addrGame/list")
    public Mono<BaseResp<List<AddrGameDetailResp>>> list(@Valid @RequestBody QueryAddrGameListReq req) {
        return Mono.just(addrGameService.getList(req));
    }

}
