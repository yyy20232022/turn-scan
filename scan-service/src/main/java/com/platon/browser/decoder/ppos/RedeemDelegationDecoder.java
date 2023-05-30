package com.platon.browser.decoder.ppos;

import cn.hutool.core.collection.CollUtil;
import com.platon.browser.param.RedeemDelegationParm;
import com.platon.browser.param.TxParam;
import com.bubble.protocol.core.methods.response.Log;
import com.bubble.rlp.solidity.RlpDecoder;
import com.bubble.rlp.solidity.RlpList;
import com.bubble.rlp.solidity.RlpString;
import com.bubble.rlp.solidity.RlpType;
import com.bubble.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * 领取解锁的委托解码
 *
 * @date: 2022/8/30
 */
public class RedeemDelegationDecoder extends AbstractPPOSDecoder {

    public RedeemDelegationDecoder() {
    }

    public static TxParam decode(RlpList rootList, List<Log> logs) {
        RedeemDelegationParm redeemDelegationParm = new RedeemDelegationParm();
        if (CollUtil.isNotEmpty(logs)) {
            String logData = logs.get(0).getData();
            RlpList rlp = RlpDecoder.decode(Numeric.hexStringToByteArray(logData));
            List<RlpType> rlpList = ((RlpList) (rlp.getValues().get(0))).getValues();
            String status = new String(((RlpString) rlpList.get(0)).getBytes());
            BigInteger released = ((RlpString) ((RlpList) RlpDecoder.decode(((RlpString) rlpList.get(1)).getBytes())).getValues().get(0)).asPositiveBigInteger();
            BigInteger restrictingPlan = ((RlpString) ((RlpList) RlpDecoder.decode(((RlpString) rlpList.get(2)).getBytes())).getValues().get(0)).asPositiveBigInteger();
            redeemDelegationParm.setStatus(status).setReleased(new BigDecimal(released)).setRestrictingPlan(new BigDecimal(restrictingPlan)).setRestrictingPlan(new BigDecimal(restrictingPlan));
            redeemDelegationParm.setValue(redeemDelegationParm.getReleased().add(redeemDelegationParm.getRestrictingPlan()));
        }
        return redeemDelegationParm;
    }

}
