package com.platon.browser.v0152.analyzer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.platon.browser.bean.CollectionTransaction;
import com.platon.browser.bean.ComplementInfo;
import com.platon.browser.bean.CustomStaking;
import com.platon.browser.client.PlatOnClient;
import com.platon.browser.dao.entity.MicroNode;
import com.platon.browser.dao.entity.MicroNodeExample;
import com.platon.browser.dao.entity.MicroNodeOptBak;
import com.platon.browser.dao.mapper.MicroNodeMapper;
import com.platon.browser.dao.mapper.MicroNodeOptBakMapper;
import com.platon.browser.elasticsearch.dto.Transaction;
import com.platon.browser.enums.MicroNodeStatusEnum;
import com.platon.browser.enums.TransactionStatusEnum;
import com.platon.browser.param.CreateStakeParam;
import com.platon.browser.param.EditCandidateParam;
import com.platon.browser.service.BubbleCacheService;
import com.platon.browser.service.ReleaseBubbleCacheService;
import com.platon.browser.service.elasticsearch.EsMicroNodeOptService;
import com.platon.browser.v0152.bean.BubbleDetailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Slf4j
@Service
public class MicroNodeAnalyzer {

    @Resource
    private MicroNodeMapper microNodeMapper;

    @Resource
    private MicroNodeOptBakMapper microNodeOptBakMapper;

    @Resource
    private EsMicroNodeOptService esMicroNodeOptService;

    @Resource
    private PlatOnClient platOnClient;

    @Resource
    private BubbleCacheService bubbleCacheService;

    @Resource
    private ReleaseBubbleCacheService releaseBubbleCacheService;

    public void resolveTx(CollectionTransaction result, ComplementInfo ci, int status) {
        if(TransactionStatusEnum.FAIL.getCode() == status){
            return;
        }
        Transaction.TypeEnum typeEnum = Transaction.TypeEnum.getEnum(ci.getType());
        switch (typeEnum){
            case CREATE_STAKING: microNodeHandler(result, ci, MicroNodeStatusEnum.CANDIDATE);break;
            case EDIT_CANDIDATE: microNodeHandler(result, ci, null);break;
            case WITHDREW_STAKING: microNodeHandler(result, ci, MicroNodeStatusEnum.EXITED);break;
        }
    }

    /**
     * bubble释放，节点的bubbleId需要重置
     */
    @Transactional(rollbackFor = Exception.class)
    public void releaseBubble(Long releaseBubbleNum) {
        List<Long> bubbleReleaseCache = releaseBubbleCacheService.getBubbleReleaseCache(releaseBubbleNum);
        if(CollUtil.isNotEmpty(bubbleReleaseCache)){
            MicroNode microNode = new MicroNode();
            microNode.setBubbleId(0L);
            MicroNodeExample microNodeExample = new MicroNodeExample();
            microNodeExample.createCriteria().andBubbleIdIn(bubbleReleaseCache);
            microNodeMapper.updateByExampleSelective(microNode,microNodeExample);
            bubbleReleaseCache.forEach(item->bubbleCacheService.delBubbleCache(item));;
            releaseBubbleCacheService.delReleaseBubbleCache(releaseBubbleNum);
        }

    }

    /**
     * 创建bubble
     */
    @Transactional(rollbackFor = Exception.class)
    public void createBubble(Long bubbleId) {
        String bubbleInfo = platOnClient.getBubbleInfo(BigInteger.valueOf(bubbleId));
        JSONObject info = JSONObject.parseObject(bubbleInfo);
        if(ObjectUtil.isNull(info)){
            log.error("{} info not exist",bubbleId);
            return;
        }
        JSONArray microNodes = info.getJSONArray("MicroNodes");
        List<String> result = new ArrayList<>(microNodes.size());
        List<String> rpcUris = new ArrayList<>(microNodes.size());
        for (Object microNode : microNodes) {
            JSONObject microNodeJson = (JSONObject)microNode;
            if(StrUtil.isNotBlank(microNodeJson.getString("RPCURI"))){
                rpcUris.add(microNodeJson.getString("RPCURI"));
            }
            result.add(microNodeJson.getString("NodeId"));
        }
        Long releaseBlock = info.getLong("ReleaseBlock")+1;
        MicroNode microNode = new MicroNode();
        microNode.setBubbleId(bubbleId);
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.createCriteria().andNodeIdIn(result);
        microNodeMapper.updateByExampleSelective(microNode,microNodeExample);
        List<MicroNode> microNodeList = microNodeMapper.selectByExample(microNodeExample);
        BubbleDetailDto bubbleDetailDto = new BubbleDetailDto();
        bubbleDetailDto.setRpcUris(rpcUris);
        bubbleDetailDto.setReleaseBubbleNum(releaseBlock);
        bubbleDetailDto.setMicroNodes(microNodeList);
        bubbleCacheService.addBubbleCache(bubbleDetailDto,bubbleId);
        List<Long> bubbleReleaseCache = releaseBubbleCacheService.getBubbleReleaseCache(releaseBlock);
        bubbleReleaseCache.add(bubbleId);
        releaseBubbleCacheService.addReleaseBubbleCache(releaseBlock,bubbleReleaseCache);
    }

    private void microNodeHandler(CollectionTransaction result, ComplementInfo ci, MicroNodeStatusEnum microNodeStatusEnum) {
        if(MicroNodeStatusEnum.CANDIDATE == microNodeStatusEnum){
            createStaking(result, ci,microNodeStatusEnum);
        }else {
            editWithdrew(result, ci,microNodeStatusEnum);
        }
    }

    /**
     * 微节点质押处理
     * @param result
     * @param ci
     * @param microNodeStatusEnum
     */
    @Transactional(rollbackFor = Exception.class)
    public void createStaking(CollectionTransaction result, ComplementInfo ci, MicroNodeStatusEnum microNodeStatusEnum) {
        CreateStakeParam createStakeParam = JSONObject.parseObject(ci.getInfo(), CreateStakeParam.class);
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.createCriteria().andNodeIdEqualTo(createStakeParam.getNodeId().substring(2));
        List<MicroNode> microNodes = microNodeMapper.selectByExample(microNodeExample);
        // 节点未质押过
        if(CollectionUtils.isEmpty(microNodes)){
            MicroNode microNode = new MicroNode();
            microNode.setNodeId(createStakeParam.getNodeId().substring(2));
            microNode.setAmount(new BigDecimal(createStakeParam.getAmount()));
            microNode.setBeneficiary(createStakeParam.getBeneficiary());
            microNode.setDetails(createStakeParam.getDetails());
            microNode.setElectronUri(createStakeParam.getElectronURI());
            microNode.setIsOperator(createStakeParam.getIsOperator());
            microNode.setName(createStakeParam.getName());
            microNode.setNodeStatus(microNodeStatusEnum.getCode());
            microNode.setP2pUri(createStakeParam.getP2pURI());
            microNode.setVersion(createStakeParam.getVersion());
            microNode.setOperationAddr(result.getFrom());
            microNode.setCreateTime(new Date());
            microNode.setRpcUri(createStakeParam.getRpcUri());
            microNodeMapper.insert(microNode);
        }else {
            // 质押过重新质押
            MicroNode microNode = microNodes.get(0);
            microNode.setNodeStatus(microNodeStatusEnum.getCode());
            microNode.setAmount(new BigDecimal(createStakeParam.getAmount()));
            microNode.setBeneficiary(createStakeParam.getBeneficiary());
            microNode.setDetails(createStakeParam.getDetails());
            microNode.setElectronUri(createStakeParam.getElectronURI());
            microNode.setIsOperator(createStakeParam.getIsOperator());
            microNode.setNodeStatus(microNodeStatusEnum.getCode());
            microNode.setP2pUri(createStakeParam.getP2pURI());
            microNode.setVersion(createStakeParam.getVersion());
            microNode.setOperationAddr(result.getFrom());
            microNode.setUpdateTime(new Date());
            microNode.setName(createStakeParam.getName());
            microNode.setRpcUri(createStakeParam.getRpcUri());
            microNodeMapper.updateByPrimaryKey(microNode);
        }
        MicroNodeOptBak microNodeOptBak = new MicroNodeOptBak();
        microNodeOptBak.setNodeId(createStakeParam.getNodeId().substring(2));
        microNodeOptBak.setType(OptTypeEnum.STAKE.code);
        microNodeOptBak.setbNum(result.getNum());
        microNodeOptBak.setTxHash(result.getHash());
        microNodeOptBak.setTime(result.getTime());
        microNodeOptBak.setCreTime(new Date());

        microNodeOptBakMapper.insert(microNodeOptBak);

        try {
            esMicroNodeOptService.add(microNodeOptBak);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 更新节点信息、解质押，处理
     * @param result
     * @param ci
     * @param microNodeStatusEnum
     */
    @Transactional(rollbackFor = Exception.class)
    public void editWithdrew(CollectionTransaction result, ComplementInfo ci, MicroNodeStatusEnum microNodeStatusEnum) {
        EditCandidateParam editCandidateParam = JSONObject.parseObject(ci.getInfo(), EditCandidateParam.class);
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.createCriteria().andNodeIdEqualTo(editCandidateParam.getNodeId().substring(2));
        List<MicroNode> microNodes = microNodeMapper.selectByExample(microNodeExample);
        MicroNode microNode = microNodes.get(0);
        if(ObjectUtil.isNull(microNodeStatusEnum)){
            microNode.setBeneficiary(editCandidateParam.getBeneficiary());
            microNode.setName(editCandidateParam.getName());
            microNode.setDetails(editCandidateParam.getDetails());
            microNode.setRpcUri(editCandidateParam.getRpcUri());
        }
        if(ObjectUtil.isNotNull(microNodeStatusEnum)){
            microNode.setAmount(BigDecimal.ZERO);
            microNode.setNodeStatus(microNodeStatusEnum.getCode());
        }

        microNode.setUpdateTime(new Date());
        microNodeMapper.updateByExample(microNode,microNodeExample);
        MicroNodeOptBak microNodeOptBak = new MicroNodeOptBak();
        microNodeOptBak.setNodeId(editCandidateParam.getNodeId().substring(2));
        if(ObjectUtil.isNotNull(microNodeStatusEnum)){
            microNodeOptBak.setType(OptTypeEnum.WITHDRAW.code);
        }else {
            microNodeOptBak.setType(OptTypeEnum.UPDATE.code);
        }
        microNodeOptBak.setCreTime(new Date());
        microNodeOptBak.setbNum(result.getNum());
        microNodeOptBak.setTxHash(result.getHash());
        microNodeOptBak.setTime(result.getTime());
        microNodeOptBakMapper.insert(microNodeOptBak);

        try {
            esMicroNodeOptService.add(microNodeOptBak);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public enum OptTypeEnum{
        STAKE(1, "质押"),
        UPDATE(2, "修改"),
        WITHDRAW(3, "解质押")
        ;
        private int code;
        private String desc;
        OptTypeEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        public int getCode(){return code;}
        public String getDesc(){return desc;}
        private static final Map<Integer, OptTypeEnum> ENUMS = new HashMap<>();
        static {
            Arrays.asList(OptTypeEnum.values()).forEach(en->ENUMS.put(en.code,en));}
        public static OptTypeEnum getEnum(Integer code){
            return ENUMS.get(code);
        }
        public static boolean contains(int code){return ENUMS.containsKey(code);}
        public static boolean contains(CustomStaking.StatusEnum en){return ENUMS.containsValue(en);}
    }
}
