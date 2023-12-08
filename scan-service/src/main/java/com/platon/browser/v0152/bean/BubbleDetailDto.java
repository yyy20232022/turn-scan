package com.platon.browser.v0152.bean;

import com.platon.browser.dao.entity.MicroNode;

import java.util.List;

/**
 * 查询地址的返回的对象
 */
public class BubbleDetailDto {


    private List<MicroNode> microNodes;

    private List<String> rpcUris;

    private Long releaseBubbleNum;

    public Long getReleaseBubbleNum() {
        return releaseBubbleNum;
    }

    public void setReleaseBubbleNum(Long releaseBubbleNum) {
        this.releaseBubbleNum = releaseBubbleNum;
    }

    public List<String> getRpcUris() {
        return rpcUris;
    }

    public void setRpcUris(List<String> rpcUris) {
        this.rpcUris = rpcUris;
    }

    public List<MicroNode> getMicroNodes() {
        return microNodes;
    }

    public void setMicroNodes(List<MicroNode> microNodes) {
        this.microNodes = microNodes;
    }
}
