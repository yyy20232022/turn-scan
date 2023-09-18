package com.platon.browser.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bubble.contracts.dpos.dto.resp.Reward;
import com.bubble.utils.Convert;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.platon.browser.bean.*;
import com.platon.browser.bean.CustomDelegation.YesNoEnum;
import com.platon.browser.bean.CustomStaking.StatusEnum;
import com.platon.browser.client.PlatOnClient;
import com.platon.browser.config.BlockChainConfig;
import com.platon.browser.constant.Browser;
import com.platon.browser.dao.custommapper.CustomDelegationMapper;
import com.platon.browser.dao.custommapper.CustomNodeMapper;
import com.platon.browser.dao.custommapper.CustomVoteMapper;
import com.platon.browser.dao.entity.*;
import com.platon.browser.dao.mapper.AddressMapper;
import com.platon.browser.dao.mapper.MicroNodeMapper;
import com.platon.browser.dao.mapper.NodeMapper;
import com.platon.browser.dao.mapper.ProposalMapper;
import com.platon.browser.elasticsearch.dto.NodeOpt;
import com.platon.browser.enums.*;
import com.platon.browser.request.micronode.AliveMicroNodeListReq;
import com.platon.browser.request.micronode.MicroNodeDetailsReq;
import com.platon.browser.request.micronode.MicroNodeOptRecordListReq;
import com.platon.browser.request.staking.*;
import com.platon.browser.response.BaseResp;
import com.platon.browser.response.RespPage;
import com.platon.browser.response.microNode.AliveMicroNodeListResp;
import com.platon.browser.response.microNode.MicroNodeDetailsResp;
import com.platon.browser.response.microNode.MicroNodeOptRecordListResp;
import com.platon.browser.response.microNode.MicroNodeStatisticResp;
import com.platon.browser.response.staking.*;
import com.platon.browser.service.elasticsearch.EsMicroNodeOptRepository;
import com.platon.browser.service.elasticsearch.EsNodeOptRepository;
import com.platon.browser.service.elasticsearch.bean.ESResult;
import com.platon.browser.service.elasticsearch.query.ESQueryBuilderConstructor;
import com.platon.browser.service.elasticsearch.query.ESQueryBuilders;
import com.platon.browser.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.bubble.utils.Convert.Unit.KPVON;

/**
 * 微节点模块方法
 */
@Service
public class MicroNodeService {

    private final Logger logger = LoggerFactory.getLogger(MicroNodeService.class);

    @Resource
    private MicroNodeMapper microNodeMapper;

    @Resource
    private EsMicroNodeOptRepository esMicroNodeOptRepository;

    @Resource
    private I18nUtil i18n;

    public MicroNodeStatisticResp stakingStatistic() {
        /** 获取统计信息 */
        MicroNodeStatisticResp stakingStatisticNewResp = new MicroNodeStatisticResp();
        long stakingAmount = microNodeMapper.countStakingAmount();
        stakingStatisticNewResp.setStakingValue(BigDecimal.valueOf(stakingAmount));
        return stakingStatisticNewResp;
    }

    public RespPage<AliveMicroNodeListResp> aliveMicroNodeList(AliveMicroNodeListReq req) {
        PageHelper.startPage(req.getPageNo(), req.getPageSize());
        Integer status = MicroNodeStatusEnum.getEnumByName(req.getQueryStatus()).getCode();


        RespPage<AliveMicroNodeListResp> respPage = new RespPage<>();
        List<AliveMicroNodeListResp> lists = new LinkedList<>();
        /** 根据条件和状态进行查询列表 */
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.setOrderByClause(" create_time desc");
        MicroNodeExample.Criteria criteria1 = microNodeExample.createCriteria();
        if(!MicroNodeStatusEnum.ALL.getCode().equals(status)){
            criteria1.andNodeStatusEqualTo(status);
        }
        if (StringUtils.isNotBlank(req.getKey())) {
            criteria1.andNameLike("%" + req.getKey() + "%");
        }

        Page<MicroNode> microNodePage = microNodeMapper.selectListByExample(microNodeExample);
        List<MicroNode> microNodeList = microNodePage.getResult();

        int i = (req.getPageNo() - 1) * req.getPageSize();
        for (MicroNode microNode : microNodeList) {
            AliveMicroNodeListResp aliveMicroNodeListResp = new AliveMicroNodeListResp();
            BeanUtils.copyProperties(microNode, aliveMicroNodeListResp);
            aliveMicroNodeListResp.setAmount(microNode.getAmount().toPlainString());
            /** 设置排行 */
            aliveMicroNodeListResp.setRanking(i + 1);
            lists.add(aliveMicroNodeListResp);
            i++;
        }
        Page<?> page = new Page<>(req.getPageNo(), req.getPageSize());
        page.setTotal(microNodePage.getTotal());
        respPage.init(page, lists);
        return respPage;
    }

    public BaseResp<MicroNodeDetailsResp> microNodeDetails(MicroNodeDetailsReq req) {
        /**
         * 先查询是否活跃节点，查不到再查询是否历史汇总
         */
        MicroNode microNode = microNodeMapper.selectByPrimaryKey(req.getNodeId());
        MicroNodeDetailsResp microNodeDetailsResp = new MicroNodeDetailsResp();
        // 只有一条数据
        if (microNode != null) {
            BeanUtils.copyProperties(microNode, microNodeDetailsResp);
            microNodeDetailsResp.setTotalValue(microNode.getAmount());
        }
        return BaseResp.build(RetEnum.RET_SUCCESS.getCode(), i18n.i(I18nEnum.SUCCESS), microNodeDetailsResp);
    }

    public RespPage<MicroNodeOptRecordListResp> microNodeOptRecordList(MicroNodeOptRecordListReq req) {
        RespPage<MicroNodeOptRecordListResp> respPage = new RespPage<>();
        ESQueryBuilderConstructor constructor = new ESQueryBuilderConstructor();
        constructor.must(new ESQueryBuilders().term("nodeId", req.getNodeId()));
        ESResult<MicroNodeOptBak> items = new ESResult<>();
        constructor.setDesc("id");
        try {
            items = esMicroNodeOptRepository.search(constructor, MicroNodeOptBak.class, req.getPageNo(), req.getPageSize());
        } catch (Exception e) {
            logger.error("获取节点操作错误。", e);
            return respPage;
        }
        List<MicroNodeOptBak> microNodeOptBaks = items.getRsData();
        List<MicroNodeOptRecordListResp> lists = new LinkedList<>();
        for (MicroNodeOptBak microNodeOptBak : microNodeOptBaks) {
            MicroNodeOptRecordListResp microNodeOptRecordListResp = new MicroNodeOptRecordListResp();
            BeanUtils.copyProperties(microNodeOptBak, microNodeOptRecordListResp);
            microNodeOptRecordListResp.setType(String.valueOf(microNodeOptBak.getType()));
            microNodeOptRecordListResp.setTimestamp(microNodeOptBak.getTime().getTime());
            microNodeOptRecordListResp.setBlockNumber(microNodeOptBak.getbNum());
            lists.add(microNodeOptRecordListResp);
        }
        /** 查询分页总数 */
        Page<?> page = new Page<>(req.getPageNo(), req.getPageSize());
        page.setTotal(items.getTotal());
        respPage.init(page, lists);
        return respPage;
    }

}
