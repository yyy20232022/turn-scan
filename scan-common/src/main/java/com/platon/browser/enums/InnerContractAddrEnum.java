package com.platon.browser.enums;


import com.bubble.parameters.NetworkParameters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: dongqile
 * Date: 2019/8/12
 * Time: 16:48
 */
public enum InnerContractAddrEnum {
    RESTRICTING_PLAN_CONTRACT(NetworkParameters.getDposContractAddressOfRestrctingPlan(), "锁仓合约"),
    STAKING_CONTRACT(NetworkParameters.getDposContractAddressOfStaking(), "质押合约"),
    DELEGATE_CONTRACT(NetworkParameters.getDposContractAddressOfStaking(), "质押合约"),
    SLASH_CONTRACT(NetworkParameters.getDposContractAddressOfSlash(), "惩罚合约"),
    PROPOSAL_CONTRACT(NetworkParameters.getDposContractAddressOfProposal(), "治理(提案)合约"),
    INCENTIVE_POOL_CONTRACT(NetworkParameters.getDposContractAddressOfIncentivePool(), "激励池合约"),
    NODE_CONTRACT(NetworkParameters.getDposContractAddressOfStaking(), "节点相关合约"),
    REWARD_CONTRACT(NetworkParameters.getDposContractAddressOfReward(), "领取奖励合约"),
    STAKINGL2_CONTRACT(NetworkParameters.getDposContractAddressOfL2Staking(), "二层网络微节点质押合约"),
    BUBBLE_CONTRACT(NetworkParameters.getDposContractAddressOfBubble(), "气泡子网络管理合约");

    private String address;

    private String desc;

    InnerContractAddrEnum(String address, String desc) {
        this.address = address;
        this.desc = desc;
    }

    public String getAddress() {
        return address;
    }

    public String getDesc() {
        return desc;
    }

    private static final Set<String> ADDRESSES = new HashSet<>();

    public static Set<String> getAddresses() {
        return ADDRESSES;
    }

    static {
        Arrays.asList(InnerContractAddrEnum.values())
              .forEach(innerContractAddEnum -> ADDRESSES.add(innerContractAddEnum.address));
    }
}
