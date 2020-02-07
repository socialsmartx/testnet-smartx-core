package com.smartx.core.coordinate;

import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.web3j.utils.Numeric;

import com.smartx.api.TerminalServer;
import com.smartx.block.Block;
import com.smartx.config.SystemProperties;
import com.smartx.core.SmartxCore;
import com.smartx.core.blockchain.*;
import com.smartx.core.consensus.*;
import com.smartx.core.consensus.SatException;
import com.smartx.crypto.ECKey;
import com.smartx.crypto.Sha256;
import com.smartx.db.TransDB;
import com.smartx.mine.MineHelper;
import com.smartx.net.Channel;
import com.smartx.net.msg.MessageHandle;
import com.smartx.net.msg.SmartXMessage;
import com.smartx.util.Tools;

public class RuleThread implements Runnable {
    private static Logger logger = Logger.getLogger(RuleThread.class);
    public static long G_FRAME = 1000 / 30;
    public SystemProperties config = SystemProperties.getDefault();
    public HashMap<Integer, Block> hashMap = new HashMap<>();
    void DoResumeRuleTop() throws SignatureException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        RuleExecutor executor = SATObjFactory.GetExecutor();
        BlockMainTop top = SATObjFactory.GetMainTop();
        if (SmartxCore.G_INSTANCE == false) {
            Block blk = smartxdb.GetLatestMC();
            top.SetTopBlock(blk);
            top.SetMCTopBlock(blk);
            SmartxCore.G_INSTANCE = true;
        }
    }
    void TimeUpdate(long begin, long frame) {
        long curTm = SmartxEpochTime.get_timestamp();
        long tm = curTm - begin;
        if (tm < frame) {
            SmartxEpochTime.Sleep(frame - tm);
        }
    }
    public boolean cmpDiff(Block blk1, Block blk2) {
        if (MineHelper.cmpDiff(blk1.diff, blk2.diff)) {
            return true;
        }
        return false;
    }
    public long GetLastHeight(BlockMainTop top) throws SatException {
        Block topblk = top.GetTopBlock();
        if (topblk == null) throw new SatException(ErrCode.SAT_CHECKHEIGHT_ERROR, "the height is null");
        return topblk.height;
    }
    public List<Block> GetBackMCRefer(Block MC) throws SatException, SQLException {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        ArrayList<Block> blocks = tvblock.GetBackBlocks(MC, DataBase.SMARTX_BLOCK_EPOCH);
        if (blocks == null || blocks.size() == 0) {
            throw new SatException(ErrCode.SAT_RULEBACKERFER_ERROR, "can't find the block of backrefer mc");
        }
        return blocks;
    }
    public void SignMaxDiffBlock(Block blk, long height) throws SignatureException, SatException {
        RuleExecutor executor = SATObjFactory.GetExecutor();
        TransDB smartxdb = SATObjFactory.GetTxDB();
        blk = executor.ruleSignBlock(blk);
        blk.height = ++height;
        if (executor.verifyRuleSignBlock(blk)) logger.info(" rulesign is ok");
        smartxdb.SaveRuleSign(blk, DataBase.SMARTX_BLOCK_EPOCH);
    }
    public void run() {
        RuleExecutor executor = SATObjFactory.GetExecutor();
        TransDB smartxdb = SATObjFactory.GetTxDB();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        BlockMainTop top = SATObjFactory.GetMainTop();
        BlockCache cache = SATObjFactory.GetCache();
        MessageHandle msghandle = SATObjFactory.GetMessageHandle();
        Block blk = null;
        long height = 0;
        List<Block> tagblocks = null;
        while (true) {
            long begin = SmartxEpochTime.get_timestamp();
            try {
                DoResumeRuleTop();
                long tm = System.currentTimeMillis();
                String timeStamp = Tools.TimeStamp2DateEx(tm);
                int time = Integer.parseInt(timeStamp.substring(17)) % (SmartxEpochTime.MAIN_CHAIN_PERIOD / 1000);
                if (time > SmartxEpochTime.G_STARTS[1]) {
                    // cache.RefreshCache();
                }
                if (time > SmartxEpochTime.RULESIGN_TIME) {
                    blk = top.GetTopBlock();
                    height = GetLastHeight(top);
                    List<Block> blocks = GetBackMCRefer(blk);
                    tagblocks = tvblock.SelectAvailableBlock(blocks);
                    tvblock.CoordinateMCs(tagblocks, hashMap);
                    blk = hashMap.get((int) tagblocks.get(0).timenum);
                    SignMaxDiffBlock(blk, height);
                    msghandle.BroadCastMBlock(blk);
                    SATObjFactory.GetMainTop().SetTopBlock(blk);
                }
                TimeUpdate(begin, G_FRAME);
            } catch (SatException e) {
                if (ErrCode.SAT_RULEBACKERFER_ERROR == e.errcode || ErrCode.SAT_CHECKHEIGHT_ERROR == e.errcode) {
                    if (null != blk) logger.info("t-1:" + blk.header.hash + " Had not rule MC,wait!");
                    SmartxEpochTime.Sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Test
    public void testRuleThread() throws SatException, SignatureException, Exception {
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        core.ReadAccounts();
        core.ReadConfig();
        core.InitGenesisEpoch();
        TerminalServer.start();
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        RuleThread rule = new RuleThread();
        Thread rulethread = new Thread(rule, "rule");
        rulethread.setPriority(Thread.MAX_PRIORITY);
        rulethread.start();
        GeneralMine mine = new GeneralMine();
        TransDB smartxdb = SATObjFactory.GetTxDB();
        RuleExecutor executor = SATObjFactory.GetExecutor();
        Block blk = null;
        MessageHandle msghandle = SATObjFactory.GetMessageHandle();
        while (true) {
            long tm = System.currentTimeMillis();
            String timeStamp = Tools.TimeStamp2DateEx(tm);
            int time = Integer.parseInt(timeStamp.substring(17)) % (SmartxEpochTime.MAIN_CHAIN_PERIOD / 1000);
            if (blk == null && time > SmartxEpochTime.G_STARTS[1]) {
                blk = GeneralMine.CreateMainBlock();
                blk.header.random = "11";
                blk.header.hash = Sha256.getH256(blk);
                blk.diff = "22";
                blk.sign = blockdag.SignBlock(blk.ToSignString(), SmartxCore.G_Wallet);
                String rulesign = config.getRuleSignInfo2();
                Channel channel = SATObjFactory.GetChannelMrg().getChannels(rulesign);
                SmartXMessage message = new SmartXMessage();
                message.msg.txs = Collections.synchronizedList(new ArrayList<Block>());
                message.msg.txs.add(blk);
                channel.sendMessage(message);
                SATObjFactory.GetTxDB().SaveBlock(blk, DataBase.SMARTX_BLOCK_EPOCH);
            }
            if (blk != null && time > 25) {
                msghandle.RuleQuery(blk, 5000);
            }
        }
    }
    @Test
    public void InitRuleSign() throws SatException, SQLException {
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        core.ReadAccounts();
        core.ReadConfig();
        core.InitGenesisEpoch();
        RuleExecutor executor = SATObjFactory.GetExecutor();
        TransDB smartxdb = SATObjFactory.GetTxDB();
        String hash = "299e20c050b3f9c0d572c852978b2a241ce182725a1b38a05bde2e7e7df09ebe";
        Block blk = smartxdb.GetBlock(hash, DataBase.SMARTX_BLOCK_EPOCH);
        executor.ruleSignBlock(blk);
        smartxdb.SaveRuleSign(blk, DataBase.SMARTX_BLOCK_EPOCH);
    }
    @Test
    public void testEcKey() {
        String hexPrivKey = "899500de3f4a14c2403eb732cf8e7702bcd08038052a53651980f5b1399a98c2";
        byte[] priv = Numeric.hexStringToByteArray(hexPrivKey);
        ECKey ecKey = ECKey.fromPrivate(priv);
        List<ECKey> ecKeyList = new ArrayList<>();
        ecKeyList.add(ecKey);
    }
}
