package com.platon.browser.v0152.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.bubble.tx.exceptions.BubbleCallException;
import com.platon.browser.bean.CommonConstant;
import com.platon.browser.client.PlatOnClient;
import com.platon.browser.config.BlockChainConfig;
import com.platon.browser.enums.ErcTypeEnum;
import com.platon.browser.exception.BusinessException;
import com.platon.browser.v0152.bean.ErcContractId;
import com.platon.browser.v0152.contract.*;
import com.bubble.crypto.Credentials;
import com.bubble.crypto.Keys;
import com.bubble.parameters.NetworkParameters;
import com.bubble.protocol.core.DefaultBlockParameter;
import com.bubble.protocol.core.DefaultBlockParameterName;
import com.bubble.protocol.core.Response;
import com.bubble.protocol.core.methods.request.Transaction;
import com.bubble.protocol.core.methods.response.BubbleCall;
import com.bubble.protocol.core.methods.response.TransactionReceipt;
import com.bubble.tx.exceptions.ContractCallException;
import com.bubble.tx.exceptions.BubbleCallTimeoutException;
import com.bubble.tx.gas.ContractGasProvider;
import com.bubble.tx.gas.GasProvider;
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
public class ErcDetectService {

    @Resource
    private BlockChainConfig chainConfig;

    public static Credentials CREDENTIALS;

    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(2104836);

    private static final BigInteger GAS_PRICE = BigInteger.valueOf(100000000000L);

    public static final GasProvider GAS_PROVIDER = new ContractGasProvider(GAS_PRICE, GAS_LIMIT);

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
    public ErcContractId getContractId(String contractAddress) throws BubbleCallTimeoutException {
        ErcContractId contractId = null;
        try {
            // 先检测是否支持ERC721
            boolean isErc721 = isSupportErc721(contractAddress);
            if (isErc721) {
                // 取ERC721合约信息
                log.info("该合约[{}]是721合约", contractAddress);
                return getErc721ContractId(contractAddress);
            }


            boolean isErc1155 = isSupportErc1155(contractAddress);
            if (isErc1155) {
                // 取ERC721合约信息
                log.info("该合约[{}]是1155合约", contractAddress);
                return getErc1155ContractId(contractAddress);
            }

            // 不是ERC721，则检测是否是ERC20
            log.info("该合约[{}]不是721合约，开始检测是否是ERC20", contractAddress);
            contractId = getErc20ContractId(contractAddress);
            if (StringUtils.isBlank(contractId.getName()) || StringUtils.isBlank(contractId.getSymbol()) | contractId.getDecimal() == null || contractId.getTotalSupply() == null) {
                // name/symbol/decimals/totalSupply 其中之一为空，则判定为未知类型
                contractId.setTypeEnum(ErcTypeEnum.UNKNOWN);
            }

        } catch (BubbleCallTimeoutException e) {
            log.error("获取合约[{}]id超时异常", contractAddress);
            throw e;
        } catch (Exception e) {
            log.error(StrUtil.format("获取合约[{}]id异常", contractAddress), e);
            throw e;
        }
        return contractId;
    }

    @Retryable(value = BubbleCallTimeoutException.class, maxAttempts = CommonConstant.reTryNum)
    public ErcContractId getContractId(String contractAddress, BigInteger blockNumber) throws
                                                                                       BubbleCallTimeoutException {
        ErcContractId contractId = null;
        try {
            // 先检测是否支持ERC721
            boolean isErc721 = isSupportErc721(contractAddress, blockNumber);
            if (isErc721) {
                // 取ERC721合约信息
                log.info("该合约[{}]是721合约", contractAddress);
                return getErc721ContractId(contractAddress, blockNumber);
            }

            boolean isErc1155 = isSupportErc1155(contractAddress, blockNumber);

            if (isErc1155) {
                // 取ERC1155合约信息
                log.info("该合约[{}]是1155合约", contractAddress);
                return getErc1155ContractId(contractAddress, blockNumber);
            }

            // 不是ERC721，则检测是否是ERC20
            log.info("该合约[{}]不是721合约，开始检测是否是ERC20", contractAddress);
            boolean isErc20 = isErc20Contract(contractAddress, blockNumber);
            if(isErc20){
                contractId = getErc20ContractId(contractAddress, blockNumber);
                if (StringUtils.isBlank(contractId.getName()) || StringUtils.isBlank(contractId.getSymbol()) | contractId.getDecimal() == null || contractId.getTotalSupply() == null) {
                    // name/symbol/decimals/totalSupply 其中之一为空，则判定为未知类型
                    contractId.setTypeEnum(ErcTypeEnum.UNKNOWN);
                }
            }else {
                contractId =new ErcContractId();
                contractId.setTypeEnum(ErcTypeEnum.UNKNOWN);
            }
        } catch (BubbleCallTimeoutException e) {
            log.error("获取合约[{}]id超时异常", contractAddress);
            throw e;
        } catch (Exception e) {
            log.error(StrUtil.format("获取合约[{}]id异常", contractAddress), e);
            throw e;
        }
        return contractId;
    }

    /**
     * 判断是不是erc20合约,是erc20返回true,不是返回false
     * @param contractAddress
     * @param blockNumber
     * @return
     */
    private boolean isErc20Contract(String contractAddress, BigInteger blockNumber) throws BubbleCallTimeoutException {
        ErcContract ercContract = Erc20Contract.load(contractAddress,
                platOnClient.getWeb3jWrapper().getWeb3j(),
                CREDENTIALS,
                GAS_PROVIDER,
                blockNumber);
        try {
            if(ObjectUtil.isNull(ercContract.name().send())){
                return false;
            }
        } catch (BubbleCallTimeoutException e) {
            log.error("ERC获取name超时异常", e);
            throw e;
        } catch (BubbleCallException e){
            log.info("该合约[{}]不是ERC20合约", contractAddress);
            return false;
        } catch (Exception e) {
            log.warn("erc get name error", e);
        }
        try {
            if(ObjectUtil.isNull(ercContract.symbol().send())){
                return false;
            }
        } catch (BubbleCallTimeoutException e) {
            log.error("ERC获取symbol超时异常", e);
            throw e;
        } catch (BubbleCallException e){
            log.info("该合约[{}]不是ERC20合约", contractAddress);
            return false;
        }  catch (Exception e) {
            log.warn("erc get symbol error", e);
        }
        try {
            if(ObjectUtil.isNull(ercContract.decimals().send())){
                return false;
            }
        } catch (BubbleCallTimeoutException e) {
            log.error("ERC获取decimal超时异常", e);
            throw e;
        } catch (BubbleCallException e){
            log.info("该合约[{}]不是ERC20合约", contractAddress);
            return false;
        }  catch (Exception e) {
            log.warn("erc get decimal error", e);
        }
        try {
            if(ObjectUtil.isNull(ercContract.totalSupply().send())){
                return false;
            }
        } catch (BubbleCallException e){
            log.info("该合约[{}]不是ERC20合约", contractAddress);
            return false;
        }  catch (BubbleCallTimeoutException e) {
            log.error("ERC获取totalSupply超时异常", e);
            throw e;
        } catch (Exception e) {
            log.warn("erc get totalSupply error", e);
        }
        return true;
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

    public List<ErcContract.ErcTxEvent> getErc20TxEvents(TransactionReceipt receipt, BigInteger blockNumber) {
        ErcContract ercContract = Erc20Contract.load(receipt.getContractAddress(),
                                                     platOnClient.getWeb3jWrapper().getWeb3j(),
                                                     CREDENTIALS,
                                                     GAS_PROVIDER,
                                                     blockNumber);
        return ercContract.getTxEvents(receipt);
    }

    public List<ErcContract.ErcTxEvent> getErc721TxEvents(TransactionReceipt receipt, BigInteger blockNumber) {
        ErcContract ercContract = Erc721Contract.load(receipt.getContractAddress(),
                                                      platOnClient.getWeb3jWrapper().getWeb3j(),
                                                      CREDENTIALS,
                                                      GAS_PROVIDER,
                                                      blockNumber);
        return ercContract.getTxEvents(receipt);
    }

    public List<ErcContract.ErcTxEvent> getErc1155TxEvents(TransactionReceipt receipt, BigInteger blockNumber) {
        ErcContract ercContract = Erc1155Contract.load(receipt.getContractAddress(),
                                                       platOnClient.getWeb3jWrapper().getWeb3j(),
                                                       CREDENTIALS,
                                                       GAS_PROVIDER,
                                                       blockNumber);
        return ercContract.getTxEvents(receipt);
    }

}
