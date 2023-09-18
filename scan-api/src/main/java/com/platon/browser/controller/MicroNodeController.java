package com.platon.browser.controller;

import com.platon.browser.enums.I18nEnum;
import com.platon.browser.enums.RetEnum;
import com.platon.browser.request.micronode.AliveMicroNodeListReq;
import com.platon.browser.request.micronode.MicroNodeDetailsReq;
import com.platon.browser.request.micronode.MicroNodeOptRecordListReq;
import com.platon.browser.response.BaseResp;
import com.platon.browser.response.RespPage;
import com.platon.browser.response.microNode.AliveMicroNodeListResp;
import com.platon.browser.response.microNode.MicroNodeDetailsResp;
import com.platon.browser.response.microNode.MicroNodeOptRecordListResp;
import com.platon.browser.response.microNode.MicroNodeStatisticResp;
import com.platon.browser.service.MicroNodeService;
import com.platon.browser.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 微节点控制器
 */
@Slf4j
@RestController
public class MicroNodeController {

    @Resource
    private I18nUtil i18n;

    @Resource
    private MicroNodeService microNodeService;

    /**
     * 汇总数据
     *
     * @param
     * @return reactor.core.publisher.Mono<com.platon.browser.response.BaseResp < com.platon.browser.response.staking.StakingStatisticNewResp>>
     * @date 2021/5/25
     */
    @SubscribeMapping("topic/micronode/statistic/new")
    @PostMapping("micronode/statistic")
    public Mono<BaseResp<MicroNodeStatisticResp>> microNodeStatisticNew() {
        return Mono.create(sink -> {
            MicroNodeStatisticResp resp = microNodeService.stakingStatistic();
            sink.success(BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), resp));
        });
    }

    /**
     * 实时微节点列表
     */
    @PostMapping("micronode/aliveNodeList")
    public Mono<RespPage<AliveMicroNodeListResp>> aliveNodeList(@Valid @RequestBody AliveMicroNodeListReq req) {
        return Mono.just(microNodeService.aliveMicroNodeList(req));
    }

    /**
     * 微节点详情
     *
     * @param req
     * @return reactor.core.publisher.Mono<com.platon.browser.response.BaseResp < com.platon.browser.response.staking.MicroNodeDetailsResp>>
     * @date 2021/5/25
     */
    @PostMapping("micronode/microNodeDetails")
    public Mono<BaseResp<MicroNodeDetailsResp>> microNodeDetails(@Valid @RequestBody MicroNodeDetailsReq req) {
        return Mono.just(microNodeService.microNodeDetails(req));
    }

    /**
     * 微节点操作记录
     * @param req
     * @return
     */
    @PostMapping("micronode/microNodeOptRecordList")
    public Mono<RespPage<MicroNodeOptRecordListResp>> microNodeOptRecordList(@Valid @RequestBody MicroNodeOptRecordListReq req) {
        return Mono.just(microNodeService.microNodeOptRecordList(req));
    }

}
