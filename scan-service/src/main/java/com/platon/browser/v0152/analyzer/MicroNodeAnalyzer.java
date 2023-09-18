package com.platon.browser.v0152.analyzer;

import com.alibaba.fastjson.JSONObject;
import com.platon.browser.bean.CollectionTransaction;
import com.platon.browser.bean.ComplementInfo;
import com.platon.browser.bean.CustomStaking;
import com.platon.browser.dao.entity.MicroNode;
import com.platon.browser.dao.entity.MicroNodeExample;
import com.platon.browser.dao.entity.MicroNodeOptBak;
import com.platon.browser.dao.mapper.MicroNodeMapper;
import com.platon.browser.dao.mapper.MicroNodeOptBakMapper;
import com.platon.browser.elasticsearch.dto.Transaction;
import com.platon.browser.enums.MicroNodeStatusEnum;
import com.platon.browser.enums.StakingStatusEnum;
import com.platon.browser.enums.TransactionStatusEnum;
import com.platon.browser.param.CreateStakeParam;
import com.platon.browser.param.EditCandidateParam;
import com.platon.browser.service.elasticsearch.EsMicroNodeOptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
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

    public void resolveTx(CollectionTransaction result, ComplementInfo ci, int status) {
        if(TransactionStatusEnum.FAIL.getCode() == status){
            return;
        }
        Transaction.TypeEnum typeEnum = Transaction.TypeEnum.getEnum(ci.getType());
        switch (typeEnum){
            case CREATE_STAKING: microNodeHandler(result, ci, MicroNodeStatusEnum.CANDIDATE);
            case EDIT_CANDIDATE: microNodeHandler(result, ci, MicroNodeStatusEnum.CANDIDATE);
            case WITHDREW_STAKING: microNodeHandler(result, ci, MicroNodeStatusEnum.EXITED);
        }
    }

    private void microNodeHandler(CollectionTransaction result, ComplementInfo ci, MicroNodeStatusEnum microNodeStatusEnum) {
        if(MicroNodeStatusEnum.CANDIDATE == microNodeStatusEnum){
            insert(result, ci,microNodeStatusEnum);
        }else {
            update(result, ci,microNodeStatusEnum);
        }
    }

    private void insert(CollectionTransaction result, ComplementInfo ci, MicroNodeStatusEnum microNodeStatusEnum) {
        CreateStakeParam createStakeParam = JSONObject.parseObject(ci.getInfo(), CreateStakeParam.class);
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.createCriteria().andNodeIdEqualTo(createStakeParam.getNodeId());
        List<MicroNode> microNodes = microNodeMapper.selectByExample(microNodeExample);
        // 节点未质押过
        if(CollectionUtils.isEmpty(microNodes)){
            MicroNode microNode = new MicroNode();
            microNode.setNodeId(createStakeParam.getNodeId());
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

            MicroNodeOptBak microNodeOptBak = new MicroNodeOptBak();
            microNodeOptBak.setNodeId(createStakeParam.getNodeId());
            microNodeOptBak.setType(OptTypeEnum.STAKE.code);
            microNodeOptBak.setbNum(result.getNum());
            microNodeOptBak.setTxHash(result.getHash());
            microNodeOptBak.setTime(result.getTime());
            microNodeOptBak.setCreTime(new Date());
            microNodeMapper.insert(microNode);
            int insert = microNodeOptBakMapper.insert(microNodeOptBak);

            try {
                esMicroNodeOptService.add(microNodeOptBak);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            update(result, ci, microNodeStatusEnum);
        }

    }

    private void update(CollectionTransaction result, ComplementInfo ci, MicroNodeStatusEnum microNodeStatusEnum) {
        EditCandidateParam editCandidateParam = JSONObject.parseObject(ci.getInfo(), EditCandidateParam.class);
        MicroNodeExample microNodeExample = new MicroNodeExample();
        microNodeExample.createCriteria().andNodeIdEqualTo(editCandidateParam.getNodeId());
        List<MicroNode> microNodes = microNodeMapper.selectByExample(microNodeExample);
        MicroNode microNode = microNodes.get(0);
        if(Objects.equals(MicroNodeStatusEnum.CANDIDATE.getCode(), microNodeStatusEnum.getCode())){
            microNode.setBeneficiary(editCandidateParam.getBeneficiary());
            microNode.setName(editCandidateParam.getName());
            microNode.setDetails(editCandidateParam.getDetails());
        }else {
            microNode.setNodeStatus(MicroNodeStatusEnum.EXITED.getCode());
        }
        microNode.setUpdateTime(new Date());
        microNodeMapper.updateByExample(microNode,microNodeExample);
        MicroNodeOptBak microNodeOptBak = new MicroNodeOptBak();
        microNodeOptBak.setNodeId(editCandidateParam.getNodeId());
        if(MicroNodeStatusEnum.CANDIDATE == microNodeStatusEnum){
            microNodeOptBak.setType(OptTypeEnum.UPDATE.code);
        }else {
            microNodeOptBak.setType(OptTypeEnum.WITHDRAW.code);
        }
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
