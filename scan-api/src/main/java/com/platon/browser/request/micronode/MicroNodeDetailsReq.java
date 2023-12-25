package com.platon.browser.request.micronode;

import com.platon.browser.utils.HexUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 微节点详情请求对象
 */
public class MicroNodeDetailsReq {

    @NotBlank(message = "{nodeId not null}")
    @Size(min = 128, max = 128)
    private String nodeId;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

}