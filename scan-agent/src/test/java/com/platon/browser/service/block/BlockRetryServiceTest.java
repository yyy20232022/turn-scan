package com.platon.browser.service.block;

import com.platon.browser.AgentTestBase;
import com.platon.browser.client.PlatOnClient;
import com.platon.browser.client.Web3jWrapper;
import com.platon.browser.exception.CollectionBlockException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import com.bubble.protocol.Web3j;
import com.bubble.protocol.core.DefaultBlockParameter;
import com.bubble.protocol.core.Request;
import com.bubble.protocol.core.methods.response.BubbleBlock;
import com.bubble.protocol.core.methods.response.BubbleBlockNumber;

import java.io.IOException;
import java.math.BigInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @description: MySQL/ES/Redis启动一致性自检服务测试
 * @author: chendongming@matrixelements.com
 * @create: 2019-11-13 11:41:00
 **/
@RunWith(MockitoJUnitRunner.Silent.class)
public class BlockRetryServiceTest extends AgentTestBase {
    @Mock
    private PlatOnClient platOnClient;
    @Mock
    private Web3jWrapper web3jWrapper;
    @Mock
    private Web3j web3j;
    @Mock
    private Request request;
    @InjectMocks
    @Spy
    private BlockRetryService target;

    @Before
    public void setup() throws IOException {
        ReflectionTestUtils.setField(target, "latestBlockNumber", BigInteger.TEN);
        when(platOnClient.getWeb3jWrapper()).thenReturn(web3jWrapper);
        when(web3jWrapper.getWeb3j()).thenReturn(web3j);
    }

    @Test
    public void testNormal() throws IOException, CollectionBlockException {
        when(web3j.bubbleGetBlockByNumber(any(DefaultBlockParameter.class),anyBoolean())).thenReturn(request);
        BubbleBlock platonBlock = mock(BubbleBlock.class);
        when(request.send()).thenReturn(platonBlock);
        target.getBlock(1L);

        when(web3j.bubbleBlockNumber()).thenReturn(request);
        BubbleBlockNumber pbn = mock(BubbleBlockNumber.class);
        when(request.send()).thenReturn(pbn);
        when(pbn.getBlockNumber()).thenReturn(BigInteger.ONE);
        target.checkBlockNumber(1L);

        verify(target, times(1)).getBlock(any());
        verify(target, times(1)).checkBlockNumber(any());
    }

    @Test(expected = RuntimeException.class)
    public void getBlockException() throws IOException, CollectionBlockException {
        when(platOnClient.getWeb3jWrapper().getWeb3j()).thenThrow(new RuntimeException());
        target.getBlock(1L);
    }

    @Test(expected = RuntimeException.class)
    public void checkBlockNumberException() throws IOException, CollectionBlockException {
        when(platOnClient.getWeb3jWrapper().getWeb3j()).thenThrow(new RuntimeException());
        target.checkBlockNumber(null);
    }

    @Test(expected = CollectionBlockException.class)
    public void checkBlockNumberException2() throws IOException, CollectionBlockException {
        when(web3j.bubbleBlockNumber()).thenReturn(request);
        BubbleBlockNumber pbn = mock(BubbleBlockNumber.class);
        when(request.send()).thenReturn(pbn);
        when(pbn.getBlockNumber()).thenReturn(BigInteger.ONE);

        target.checkBlockNumber(50L);
    }
}
