package com.smartx.core.coordinate;

import java.security.SignatureException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.utils.Numeric;

import com.smartx.block.Block;
import com.smartx.config.SystemProperties;
import com.smartx.crypto.ECKey;
import com.smartx.crypto.HashUtil;

@Component
public class RuleExecutor {
    private final static Logger logger = Logger.getLogger("rule");
    //@Autowired
    private SystemProperties config;
    private static long executeTime = 0;
    //缓存裁决层共私钥的List
    //TODO:后期应该使用map来存储该数据结构,list无法防止重复存储
    private List<ECKey> bftKeys;
    //TODO:用于测试的Map，用来存储收到过的Block
    private static final Map<String, Block> txMap = new HashMap<>();
    //缓存区块的列表
    private List<RuleTask> ruleTaskList;
    //区块难度比较
    private RuleDiffcultyComparator diffcultyComparator;
    public RuleExecutor() {
        // 单节点临时测试 后期去掉 TODO
        config = SystemProperties.getDefault();
        bftKeys = config.getBftPubKeys();
    }
    @Autowired
    public RuleExecutor(SystemProperties config) {
        //bftKeys = config.getBftPubKeys();
        ruleTaskList = new ArrayList<>();
        diffcultyComparator = new RuleDiffcultyComparator();
        //开启任务，每5秒钟获取一个胜出主块，然后广播
        ScheduledExecutorService mcBlockExecutor = Executors.newSingleThreadScheduledExecutor();
        mcBlockExecutor.scheduleWithFixedDelay(() -> {
            try {
                //logger.info("rule excutor run for the {} time",executeTime++);
                synchronized (this) {
                    if (ruleTaskList.size() > 0) {
                        logger.info("rule executor sort block list size {}" + ruleTaskList.size());
                        //根据难度排序区块
                        ruleTaskList.sort(this.diffcultyComparator);
                        //广播区块
                        RuleTask mcBlockTask = ruleTaskList.get(0);
                        Block mcBlock = mcBlockTask.block;
                        //                        logger.info("rule executor submit mc block {} difficulty {} ",mcBlock.getBlockHash());
                        sendMcBlock(mcBlockTask);
                        ruleTaskList.clear();
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
                logger.error("exception in mc block executor ", t);
            }
        }, 1, 5, TimeUnit.SECONDS);
    }
    private class RuleDiffcultyComparator implements Comparator<RuleTask> {
        @Override
        public int compare(RuleTask task1, RuleTask task2) {
            Block block1 = task1.block;
            Block block2 = task2.block;
            //            BigInteger difficulty1 = ByteUtil.bytesToBigInteger(block1.getDifficulty());
            //            BigInteger difficulty2 = ByteUtil.bytesToBigInteger(block2.getDifficulty());
            //            return difficulty2.compareTo(difficulty1);
            return 0;
        }
    }
    //添加区块到缓存
    public void addRuleTask(RuleTask ruleTask) {
        Block block = ruleTask.block;
        //        block.rlpParse();
        //        long timestampNew = block.getTimeStamp();
        //        String difficultyNew = Hex.toHexString(block.getDifficulty());
        //        String valueNew = Hex.toHexString(block.getValue());
        //        String blockHash = Hex.toHexString(block.getBlockHash());
        //        //判断区块是否已经收到过
        //        if(txMap.get(blockHash) == null){
        //            logger.info("get new block timestamp {} difficulty {} value {} hash {} ",timestampNew,difficultyNew,valueNew,blockHash);
        //            ruleTaskList.add(ruleTask);
        //            txMap.put(blockHash,block);
        //        }else{
        //            logger.info("block hash {} already received ignore it ",blockHash);
        //            return;
        //        }
    }
    private void sendMcBlock(RuleTask ruleTask) {
        //发送胜出主块
        if (ruleTask != null) {
            ruleTask.execute();
        }
    }
    public Block ruleSignBlock(Block block) {
        //区块当中的裁决层签名列表
        block.ruleSigns = Collections.synchronizedList(new ArrayList<RuleSign>());
        List<RuleSign> ruleSigns = block.ruleSigns;
        //使用裁决层共私钥签名该区块
        for (ECKey ecKey : bftKeys) {
            RuleSign ruleSign = new RuleSign();
            byte[] rawHash = HashUtil.sha3(block.header.hash.getBytes());
            ECKey.ECDSASignature ecdsaSignature = ecKey.sign(rawHash);
            ruleSign.signer = Numeric.toHexStringNoPrefix(ecKey.getPubKey());
            ruleSign.info = block.header.hash;
            ruleSign.sign = ecdsaSignature.toBase58();
            ruleSigns.add(ruleSign);
        }
        return block;
    }
    public boolean verifyRuleSignBlock(Block block) throws SignatureException {
        //把所有的裁决层节点的公钥放入到map里面，并且是否签名的标识位一律设置为0
        //如果找到有签名，则设置标识为为1
        //检查到重复的公钥签名的话，签名的计数不增加
        if (block.ruleSigns == null || block.ruleSigns.equals("")) return false;
        Map<String, Integer> signMap = new HashMap<String, Integer>();
        List<RuleSign> ruleSigns = block.ruleSigns;
        for (ECKey ecKey : bftKeys) {
            String hexPublicKey = Numeric.toHexStringNoPrefix(ecKey.getPubKey());
            signMap.put(hexPublicKey, 0);
        }
        int signNum = 0;
        byte[] rawHash = HashUtil.sha3(block.header.hash.getBytes());
        for (RuleSign ruleSign : ruleSigns) {
            ECKey.ECDSASignature recoverSig = ECKey.ECDSASignature.decodeFromBase58(ruleSign.sign);
            ECKey ecKey = ECKey.signatureToKey(rawHash, recoverSig);
            String hexPublicKey = Numeric.toHexStringNoPrefix(ecKey.getPubKey());
            if (signMap.get(hexPublicKey) == 0) {
                signNum++;
                signMap.replace(hexPublicKey, 1);
            }
        }
        //System.out.println("rule signers timenum is " + signNum);
        return signNum >= (bftKeys.size() * 2) / 3;
    }
}
