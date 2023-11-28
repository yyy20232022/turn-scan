package com.platon.browser.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bubble.contracts.dpos.RestrictingPlanContract;
import com.bubble.contracts.dpos.dto.CallResponse;
import com.bubble.contracts.dpos.dto.resp.RestrictingItem;
import com.bubble.contracts.dpos.dto.resp.Reward;
import com.bubble.protocol.core.DefaultBlockParameterName;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.platon.browser.bean.CustomAddressDetail;
import com.platon.browser.bean.DlLock;
import com.platon.browser.bean.LockDelegate;
import com.platon.browser.bean.RestrictingBalance;
import com.platon.browser.cache.AddrGameCacheDto;
import com.platon.browser.client.PlatOnClient;
import com.platon.browser.client.SpecialApi;
import com.platon.browser.config.BlockChainConfig;
import com.platon.browser.dao.custommapper.CustomAddressMapper;
import com.platon.browser.dao.custommapper.CustomRpPlanMapper;
import com.platon.browser.dao.entity.*;
import com.platon.browser.dao.mapper.AddrGameMapper;
import com.platon.browser.dao.mapper.RpPlanMapper;
import com.platon.browser.elasticsearch.dto.Block;
import com.platon.browser.enums.I18nEnum;
import com.platon.browser.enums.RetEnum;
import com.platon.browser.enums.TokenTypeEnum;
import com.platon.browser.exception.BusinessException;
import com.platon.browser.request.address.QueryAddrGameListReq;
import com.platon.browser.request.address.QueryDetailRequest;
import com.platon.browser.request.address.QueryRPPlanDetailRequest;
import com.platon.browser.response.BaseResp;
import com.platon.browser.response.RespPage;
import com.platon.browser.response.address.AddrGameDetailResp;
import com.platon.browser.response.address.DetailsRPPlanResp;
import com.platon.browser.response.address.QueryDetailResp;
import com.platon.browser.response.address.QueryRPPlanDetailResp;
import com.platon.browser.response.microNode.AliveMicroNodeListResp;
import com.platon.browser.service.elasticsearch.EsBlockRepository;
import com.platon.browser.utils.ConvertUtil;
import com.platon.browser.utils.I18nUtil;
import com.platon.browser.v0152.bean.AddrGameDetailDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 地址参与游戏具体逻辑实现方法
 *
 * @author zhangrj
 * @file AddressServiceImpl.java
 * @description
 * @data 2019年8月31日
 */
@Service
public class AddrGameService {

    private final Logger logger = LoggerFactory.getLogger(AddrGameService.class);

    @Resource
    private AddrGameCacheService addrGameCacheService;

    @Resource
    private I18nUtil i18n;

    /**
     * 查询地址参与详情
     *
     * @param req
     * @return com.platon.browser.response.address.QueryDetailResp
     * @date 2021/4/15
     */
    public BaseResp<List<AddrGameDetailResp>> getList(QueryAddrGameListReq req) {
        BaseResp<List<AddrGameDetailResp>> baseResp = new BaseResp<>();
        // 如果查询0地址，直接返回
        if (StrUtil.isNotBlank(req.getAddress()) && com.platon.browser.utils.AddressUtil.isAddrZero(req.getAddress())) {
            return baseResp;
        }
        List<AddrGameDetailDto> addrGameList = addrGameCacheService.getAddrGameCache(req.getAddress());

        List<AddrGameDetailResp> lists = new LinkedList<>();
        for (AddrGameDetailDto addrGameDetailDto : addrGameList) {
            AddrGameDetailResp resp = new AddrGameDetailResp();
            BeanUtils.copyProperties(addrGameDetailDto, resp);
            lists.add(resp);
        }
        return BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS),lists);
    }

}
