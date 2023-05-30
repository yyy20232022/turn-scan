package com.platon.browser.handler;

import com.platon.browser.AgentTestBase;
import com.platon.browser.analyzer.BlockAnalyzer;
import com.platon.browser.bean.BlockEvent;
import com.platon.browser.bean.EpochMessage;
import com.platon.browser.bean.ReceiptResult;
import com.platon.browser.cache.AddressCache;
import com.platon.browser.client.PlatOnClient;
import com.platon.browser.exception.BeanCreateOrUpdateException;
import com.platon.browser.exception.BlankResponseException;
import com.platon.browser.exception.ContractInvokeException;
import com.platon.browser.publisher.CollectionEventPublisher;
import com.bubble.protocol.core.methods.response.BubbleBlock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @description: MySQL/ES/Redis启动一致性自检服务测试
 * @author: chendongming@matrixelements.com
 * @create: 2019-11-13 11:41:00
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class BlockEventHandlerTest extends AgentTestBase {

    @Mock
    private CollectionEventPublisher collectionEventPublisher;

    @Mock
    private PlatOnClient platOnClient;

    @Mock
    private AddressCache addressCache;

    @Spy
    private BlockAnalyzer blockAnalyzer;

    @InjectMocks
    private BlockEventHandler target;

    private ReceiptResult receiptResult;

    @Before
    public void setup() {
        receiptResult = receiptResultList.get(0);
    }

    @Test
    public void test() throws InterruptedException, ExecutionException, BeanCreateOrUpdateException, IOException, ContractInvokeException, BlankResponseException {
        CompletableFuture<BubbleBlock> blockCF = getBlockAsync(7000L);
        CompletableFuture<ReceiptResult> receiptCF = getReceiptAsync(7000L);
        BlockEvent blockEvent = new BlockEvent();
        blockEvent.setBlockCF(blockCF);
        blockEvent.setReceiptCF(receiptCF);
        blockEvent.setEpochMessage(EpochMessage.newInstance());

        target.onEvent(blockEvent, 1, false);

        //verify(target, times(1)).onEvent(any(), anyLong(), anyBoolean());
    }

    /**
     * 异步获取区块
     */
    public CompletableFuture<BubbleBlock> getBlockAsync(Long blockNumber) {
        return CompletableFuture.supplyAsync(() -> {
            BubbleBlock pb = new BubbleBlock();
            BubbleBlock.Block block = rawBlockMap.get(receiptResult.getResult().get(0).getBlockNumber());
            pb.setResult(block);
            return pb;
        });
    }

    /**
     * 异步获取区块
     */
    public CompletableFuture<ReceiptResult> getReceiptAsync(Long blockNumber) {
        return CompletableFuture.supplyAsync(() -> receiptResult);
    }

}
