package com.platon.browser.v0152.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bubble.crypto.Credentials;
import com.bubble.crypto.Keys;
import com.bubble.parameters.NetworkParameters;
import com.bubble.protocol.core.DefaultBlockParameter;
import com.bubble.protocol.core.DefaultBlockParameterName;
import com.bubble.protocol.core.Response;
import com.bubble.protocol.core.methods.request.Transaction;
import com.bubble.protocol.core.methods.response.BubbleCall;
import com.bubble.protocol.core.methods.response.TransactionReceipt;
import com.bubble.tx.exceptions.BubbleCallTimeoutException;
import com.bubble.tx.exceptions.ContractCallException;
import com.bubble.tx.gas.ContractGasProvider;
import com.bubble.tx.gas.GasProvider;
import com.platon.browser.bean.CommonConstant;
import com.platon.browser.bean.Receipt;
import com.platon.browser.client.PlatOnClient;
import com.platon.browser.config.BlockChainConfig;
import com.platon.browser.contract.GameContract;
import com.platon.browser.enums.ErcTypeEnum;
import com.platon.browser.enums.GameTypeEnum;
import com.platon.browser.exception.BusinessException;
import com.platon.browser.v0152.bean.ErcContractId;
import com.platon.browser.v0152.bean.GameContractId;
import com.platon.browser.v0152.contract.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;

/**
 * Erc探测服务
 */
@Slf4j
@Service
public class GameDetectService {

    @Resource
    private BlockChainConfig chainConfig;

    public static Credentials CREDENTIALS;

    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(2104836);

    private static final BigInteger GAS_PRICE = BigInteger.valueOf(100000000000L);

    public static final ContractGasProvider GAS_PROVIDER = new ContractGasProvider(GAS_PRICE, GAS_LIMIT);

    @Resource
    private PlatOnClient platOnClient;

    @PostConstruct
    public void init() {
        NetworkParameters.init(chainConfig.getChainId());
        CREDENTIALS = Credentials.create("4484092b68df58d639f11d59738983e2b8b81824f3c0c759edd6773f9adadfe7");
    }

    /**
     * 检测输入数据--不带重试机制
     *
     * @param contractAddress
     * @param inputData
     * @return java.lang.String
     * @date 2021/4/30
     */
    @Retryable(value = BubbleCallTimeoutException.class, maxAttempts = CommonConstant.reTryNum)
    private String detectInputData(String contractAddress, String inputData) throws BubbleCallTimeoutException {
        Transaction transaction = null;
        BubbleCall platonCall = null;
        try {
            transaction = Transaction.createEthCallTransaction(Credentials.create(Keys.createEcKeyPair()).getAddress(),
                                                               contractAddress,
                                                               inputData);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            log.error(StrUtil.format("合约地址[{}]检测输入数据异常", contractAddress), e);
            throw new BusinessException(e.getMessage());
        }
        try {
            platonCall = platOnClient.getWeb3jWrapper()
                                     .getWeb3j()
                                     .bubbleCall(transaction, DefaultBlockParameterName.LATEST)
                                     .send();
            if (platonCall.hasError()) {
                Response.Error error = platonCall.getError();
                String message = error.getMessage();
                String lowMessage = !StrUtil.isBlank(message) ? message.toLowerCase() : null;
                // 包含timeout则抛超时异常，其他错误则直接抛出runtime异常
                if (!StrUtil.isBlank(lowMessage) && lowMessage.contains("timeout")) {
                    log.error("合约地址[{}]检测输入数据超时异常.error_code[{}],error_msg[{}]",
                              contractAddress,
                              error.getCode(),
                              error.getMessage());
                    throw new BubbleCallTimeoutException(error.getCode(), error.getMessage(), platonCall);
                }
            }
        } catch (BubbleCallTimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return platonCall.getResult();
    }

    /**
     * 检测输入数据--不带重试机制
     *
     * @param contractAddress
     * @param inputData
     * @return java.lang.String
     * @date 2021/4/30
     */
    @Retryable(value = BubbleCallTimeoutException.class, maxAttempts = CommonConstant.reTryNum)
    private String detectInputData(String contractAddress, String inputData, BigInteger blockNumber) throws
                                                                                                     BubbleCallTimeoutException {
        Transaction transaction = null;
        BubbleCall platonCall = null;
        try {
            transaction = Transaction.createEthCallTransaction(Credentials.create(Keys.createEcKeyPair()).getAddress(),
                                                               contractAddress,
                                                               inputData);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            log.error(StrUtil.format("合约地址[{}]检测输入数据异常", contractAddress), e);
            throw new BusinessException(e.getMessage());
        }
        try {
            platonCall = platOnClient.getWeb3jWrapper()
                                     .getWeb3j()
                                     .bubbleCall(transaction, DefaultBlockParameter.valueOf(blockNumber))
                                     .send();
            if (platonCall.hasError()) {
                Response.Error error = platonCall.getError();
                String message = error.getMessage();
                String lowMessage = !StrUtil.isBlank(message) ? message.toLowerCase() : null;
                // 包含timeout则抛超时异常，其他错误则直接抛出runtime异常
                if (!StrUtil.isBlank(lowMessage) && lowMessage.contains("timeout")) {
                    log.error("合约地址[{}]检测输入数据超时异常.error_code[{}],error_msg[{}]",
                              contractAddress,
                              error.getCode(),
                              error.getMessage());
                    throw new BubbleCallTimeoutException(error.getCode(), error.getMessage(), platonCall);
                }
            }
        } catch (BubbleCallTimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return platonCall.getResult();
    }

    /**
     * 重试完成还是不成功，会回调该方法
     *
     * @param e:
     * @return: void
     * @date: 2022/5/6
     */
    @Recover
    public String recoverDetectInputData(Exception e) {
        log.error("重试完成还是业务失败，请联系管理员处理");
        return null;
    }

    /**
     * 是否支持Erc165标准
     *
     * @param contractAddress:
     * @return: boolean
     * @date: 2021/9/18
     */
    private boolean isSupportErc165(String contractAddress) throws BubbleCallTimeoutException {
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a701ffc9a700000000000000000000000000000000000000000000000000000000");
        if (!"0x0000000000000000000000000000000000000000000000000000000000000001".equals(result)) {
            return false;
        }
        result = detectInputData(contractAddress,
                                 "0x01ffc9a7ffffffff00000000000000000000000000000000000000000000000000000000");
        return "0x0000000000000000000000000000000000000000000000000000000000000000".equals(result);
    }

    /**
     * 是否支持Erc165标准
     *
     * @param contractAddress:
     * @param blockNumber:
     * @return: boolean
     * @date: 2021/9/18
     */
    private boolean isSupportErc165(String contractAddress, BigInteger blockNumber) throws BubbleCallTimeoutException {
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a701ffc9a700000000000000000000000000000000000000000000000000000000",
                                        blockNumber);
        if (!"0x0000000000000000000000000000000000000000000000000000000000000001".equals(result)) {
            return false;
        }
        result = detectInputData(contractAddress,
                                 "0x01ffc9a7ffffffff00000000000000000000000000000000000000000000000000000000",
                                 blockNumber);
        return "0x0000000000000000000000000000000000000000000000000000000000000000".equals(result);
    }

    public boolean isSupportErc721Metadata(String contractAddress) throws BubbleCallTimeoutException {
        // 支持erc721，则必定要支持erc165
        if (!isSupportErc165(contractAddress)) {
            log.info("该合约[{}]不支持erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a75b5e139f00000000000000000000000000000000000000000000000000000000");
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    public boolean isSupportErc721Metadata(String contractAddress, BigInteger blockNumber) throws
                                                                                           BubbleCallTimeoutException {
        // 支持erc721，则必定要支持erc165
        if (!isSupportErc165(contractAddress, blockNumber)) {
            log.info("该合约[{}]不支持erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a75b5e139f00000000000000000000000000000000000000000000000000000000",
                                        blockNumber);
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    public boolean isSupportErc721Enumerable(String contractAddress) throws BubbleCallTimeoutException {
        // 支持erc721，则必定要支持erc165
        if (!isSupportErc165(contractAddress)) {
            log.info("该合约[{}]不支持erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a7780e9d6300000000000000000000000000000000000000000000000000000000");
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    public boolean isSupportErc721Enumerable(String contractAddress, BigInteger blockNumber) throws
                                                                                             BubbleCallTimeoutException {
        // 支持erc721，则必定要支持erc165
        if (!isSupportErc165(contractAddress, blockNumber)) {
            log.info("该合约[{}]不支持erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a7780e9d6300000000000000000000000000000000000000000000000000000000",
                                        blockNumber);
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    /**
     * 是否支持Erc721合约
     *
     * @param contractAddress:
     * @return: boolean
     * @date: 2022/1/14
     */
    private boolean isSupportErc721(String contractAddress) throws BubbleCallTimeoutException {
        // 支持erc721，则必定要支持erc165
        if (!isSupportErc165(contractAddress)) {
            log.info("该合约[{}]不支持erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a780ac58cd00000000000000000000000000000000000000000000000000000000");
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    /**
     * 是否支持Erc721合约
     *
     * @param contractAddress:
     * @param blockNumber:
     * @return: boolean
     * @date: 2022/1/14
     */
    private boolean isSupportErc721(String contractAddress, BigInteger blockNumber) throws BubbleCallTimeoutException {
        // 支持erc721，则必定要支持erc165
        if (!isSupportErc165(contractAddress, blockNumber)) {
            log.info("该合约[{}]不支持erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a780ac58cd00000000000000000000000000000000000000000000000000000000",
                                        blockNumber);
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    /**
     * 是否支持Erc1155合约
     *
     * @param contractAddress:
     * @return: boolean
     * @date: 2022/2/5
     */
    private boolean isSupportErc1155(String contractAddress) throws BubbleCallTimeoutException {
        // 支持erc1155，则必定要支持erc165
        if (!isSupportErc165(contractAddress)) {
            log.info("该合约[{}]不支持erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a7d9b67a2600000000000000000000000000000000000000000000000000000000");
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    /**
     * 是否支持Erc1155合约
     *
     * @param contractAddress:
     * @param blockNumber:
     * @return: boolean
     * @date: 2022/2/5
     */
    private boolean isSupportErc1155(String contractAddress, BigInteger blockNumber) throws BubbleCallTimeoutException {
        // 支持erc1155，则必定要支持erc165
        if (!isSupportErc165(contractAddress, blockNumber)) {
            log.info("该合约[{}]不支持erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a7d9b67a2600000000000000000000000000000000000000000000000000000000",
                                        blockNumber);
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    public Boolean isSupportErc1155Metadata(String contractAddress, BigInteger blockNumber) throws
                                                                                            BubbleCallTimeoutException {
        // 支持erc1155，则必定要支持erc165
        if (!isSupportErc165(contractAddress, blockNumber)) {
            log.info("该合约[{}]不支持erc165", contractAddress);
            return false;
        }
        String result = detectInputData(contractAddress,
                                        "0x01ffc9a70e89341c00000000000000000000000000000000000000000000000000000000",
                                        blockNumber);
        return "0x0000000000000000000000000000000000000000000000000000000000000001".equals(result);
    }

    private ErcContractId getErc20ContractId(String contractAddress) throws BubbleCallTimeoutException {
        ErcContract ercContract = Erc20Contract.load(contractAddress,
                                                     platOnClient.getWeb3jWrapper().getWeb3j(),
                                                     CREDENTIALS,
                                                     GAS_PROVIDER);
        ErcContractId contractId = resolveContractId(ercContract);
        contractId.setTypeEnum(ErcTypeEnum.ERC20);
        return contractId;
    }

    private ErcContractId getErc20ContractId(String contractAddress, BigInteger blockNumber) throws
                                                                                             BubbleCallTimeoutException {
        ErcContract ercContract = Erc20Contract.load(contractAddress,
                                                     platOnClient.getWeb3jWrapper().getWeb3j(),
                                                     CREDENTIALS,
                                                     GAS_PROVIDER,
                                                     blockNumber);
        ErcContractId contractId = resolveContractId(ercContract);
        contractId.setTypeEnum(ErcTypeEnum.ERC20);
        return contractId;
    }

    private ErcContractId getErc721ContractId(String contractAddress) throws BubbleCallTimeoutException {
        ErcContract ercContract = Erc721Contract.load(contractAddress,
                                                      platOnClient.getWeb3jWrapper().getWeb3j(),
                                                      CREDENTIALS,
                                                      GAS_PROVIDER);
        ErcContractId contractId = resolveContractId(ercContract);
        contractId.setTypeEnum(ErcTypeEnum.ERC721);
        return contractId;
    }

    private ErcContractId getErc721ContractId(String contractAddress, BigInteger blockNumber) throws
                                                                                              BubbleCallTimeoutException {
        ErcContract ercContract = Erc721Contract.load(contractAddress,
                                                      platOnClient.getWeb3jWrapper().getWeb3j(),
                                                      CREDENTIALS,
                                                      GAS_PROVIDER,
                                                      blockNumber);
        ErcContractId contractId = resolveContractId(ercContract);
        contractId.setTypeEnum(ErcTypeEnum.ERC721);
        return contractId;
    }

    private ErcContractId getErc1155ContractId(String contractAddress) throws BubbleCallTimeoutException {
        ErcContract ercContract = Erc721Contract.load(contractAddress,
                                                      platOnClient.getWeb3jWrapper().getWeb3j(),
                                                      CREDENTIALS,
                                                      GAS_PROVIDER);
        ErcContractId contractId = resolveContractId(ercContract);
        contractId.setTypeEnum(ErcTypeEnum.ERC1155);
        return contractId;
    }

    private ErcContractId getErc1155ContractId(String contractAddress, BigInteger blockNumber) throws
                                                                                               BubbleCallTimeoutException {
        ErcContract ercContract = Erc1155Contract.load(contractAddress,
                                                       platOnClient.getWeb3jWrapper().getWeb3j(),
                                                       CREDENTIALS,
                                                       GAS_PROVIDER,
                                                       blockNumber);
        ErcContractId contractId = resolveContractId(ercContract);
        contractId.setTypeEnum(ErcTypeEnum.ERC1155);
        return contractId;
    }

    // 检测Erc20合约标识
    private ErcContractId resolveContractId(ErcContract ercContract) throws BubbleCallTimeoutException {
        ErcContractId contractId = new ErcContractId();
        try {
            try {
                contractId.setName(ercContract.name().send());
            } catch (BubbleCallTimeoutException e) {
                log.error("ERC获取name超时异常", e);
                throw e;
            } catch (Exception e) {
                log.warn("erc get name error", e);
            }
            try {
                contractId.setSymbol(ercContract.symbol().send());
            } catch (BubbleCallTimeoutException e) {
                log.error("ERC获取symbol超时异常", e);
                throw e;
            } catch (Exception e) {
                log.warn("erc get symbol error", e);
            }
            try {
                contractId.setDecimal(ercContract.decimals().send().intValue());
            } catch (BubbleCallTimeoutException e) {
                log.error("ERC获取decimal超时异常", e);
                throw e;
            } catch (Exception e) {
                log.warn("erc get decimal error", e);
            }
            try {
                contractId.setTotalSupply(new BigDecimal(ercContract.totalSupply().send()));
            } catch (BubbleCallTimeoutException e) {
                log.error("ERC获取totalSupply超时异常", e);
                throw e;
            } catch (Exception e) {
                log.warn("erc get totalSupply error", e);
            }
        } catch (BubbleCallTimeoutException e) {
            throw e;
        } catch (ContractCallException e) {
            log.error(" not erc contract,{}", ercContract, e);
        }
        return contractId;
    }

    @Retryable(value = BubbleCallTimeoutException.class, maxAttempts = CommonConstant.reTryNum)
    public GameContractId getContractId(String contractAddress) throws BubbleCallTimeoutException {
        GameContractId gameContractId = null;
        try {
            gameContractId = isGameContract(contractAddress);

            if (StrUtil.isNotBlank(gameContractId.getName())) {
                // 取游戏合约信息
                log.info("该合约[{}]是游戏合约", contractAddress);
                gameContractId.setTypeEnum(GameTypeEnum.GAME);
                return gameContractId;
            }

        } catch (BubbleCallTimeoutException e) {
            log.error("获取合约[{}]id超时异常", contractAddress);
            throw e;
        } catch (Exception e) {
            log.error(StrUtil.format("获取合约[{}]id异常", contractAddress), e);
            throw e;
        }
        return gameContractId;
    }

    private GameContractId isGameContract(String contractAddress) throws
            BubbleCallTimeoutException {
        GameContract gameContract = GameContract.load(contractAddress,
                platOnClient.getWeb3jWrapper().getWeb3j(),
                CREDENTIALS,
                GAS_PROVIDER);
        return resolveContractIdGameContract(gameContract);
    }

    private GameContractId resolveContractIdGameContract(GameContract gameContract) throws BubbleCallTimeoutException {
        GameContractId gameContractId = new GameContractId();
        try {
            try {
                gameContractId.setName(gameContract.Name().send());
            } catch (BubbleCallTimeoutException e) {
                log.error("Game contract 获取name超时异常", e);
                throw e;
            } catch (Exception e) {
                log.warn("game contract get name error", e);
            }
            try {
                gameContractId.setWebsite(gameContract.Website().send());
            } catch (BubbleCallTimeoutException e) {
                log.error("game contract 获取website超时异常", e);
                throw e;
            } catch (Exception e) {
                log.warn("game contract get website error", e);
            }
            try {
                gameContractId.setIntroduce(gameContract.Introduce().send());
            } catch (BubbleCallTimeoutException e) {
                log.error("game contract 获取introduce超时异常", e);
                throw e;
            } catch (Exception e) {
                log.warn("game contract get introduce error", e);
            }
            try {
                gameContractId.setGameType(gameContract.GameType().send());
            } catch (BubbleCallTimeoutException e) {
                log.error("game contract 获取gameType超时异常", e);
                throw e;
            } catch (Exception e) {
                log.warn("game contract get gameType error", e);
            }
        } catch (BubbleCallTimeoutException e) {
            throw e;
        } catch (ContractCallException e) {
            log.error(" not game contract,{}", gameContract, e);
        }
        return gameContractId;
    }

    /**
     * 重试完成还是不成功，会回调该方法
     *
     * @param e:
     * @return: void
     * @date: 2022/5/6
     */
    @Recover
    public ErcContractId recover(Exception e) {
        log.error("重试完成还是业务失败，请联系管理员处理");
        return null;
    }

    public List<GameContract.GameTxEvent> getGameDetectTxEvents(TransactionReceipt receipt) {
        GameContract gameContract = GameContract.load(receipt.getContractAddress(),
                                                     platOnClient.getWeb3jWrapper().getWeb3j(),
                                                     CREDENTIALS,
                                                     GAS_PROVIDER);
        return gameContract.getTxEvents(receipt);
    }

    /**
     * 获取加入游戏事件
     * @param receipt
     * @return
     */
    public List<GameContract.JoinGameEventResponse> getJoinGameEvents(TransactionReceipt receipt) {
        GameContract gameContract = GameContract.load(receipt.getContractAddress(),
                platOnClient.getWeb3jWrapper().getWeb3j(),
                CREDENTIALS,
                GAS_PROVIDER);
        return gameContract.getJoinGameEvents(receipt);
    }

    /**
     * 获取创建游戏事件
     * @param receipt
     * @return
     */
    public List<GameContract.CreateGameEventResponse> getCreateGameEvents(TransactionReceipt receipt) {
        GameContract gameContract = GameContract.load(receipt.getContractAddress(),
                platOnClient.getWeb3jWrapper().getWeb3j(),
                CREDENTIALS,
                GAS_PROVIDER);
        return gameContract.getCreateGameEvents(receipt);
    }

    /**
     * 获取结束游戏事件
     * @param receipt
     * @return
     */
    public List<GameContract.EndGameEventResponse> getEndGameEvents(TransactionReceipt receipt) {
        GameContract gameContract = GameContract.load(receipt.getContractAddress(),
                platOnClient.getWeb3jWrapper().getWeb3j(),
                CREDENTIALS,
                GAS_PROVIDER);
        return gameContract.getEndGameEvents(receipt);
    }
}
