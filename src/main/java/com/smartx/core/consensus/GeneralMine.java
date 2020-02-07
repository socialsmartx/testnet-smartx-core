package com.smartx.core.consensus;

import java.math.BigInteger;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.smartx.block.Account;
import com.smartx.block.Block;
import com.smartx.block.BlockRelation;
import com.smartx.config.SystemProperties;
import com.smartx.core.SmartxCore;
import com.smartx.core.blockchain.*;
import com.smartx.core.coordinate.BlockCache;
import com.smartx.core.syncmanager.MerkleTree;
import com.smartx.core.syncmanager.SyncThread;
import com.smartx.crypto.Sha256;
import com.smartx.db.AccountDB;
import com.smartx.db.BlockStats;
import com.smartx.db.TransDB;
import com.smartx.mine.MineHelper;
import com.smartx.mine.PoolThread;
import com.smartx.net.msg.MessageHandle;
import com.smartx.util.Tools;
import com.smartx.wallet.Key25519;
import com.smartx.wallet.SmartXWallet;

public class GeneralMine implements Runnable {
    private static Logger log = Logger.getLogger(GeneralMine.class);
    public SystemProperties config = SystemProperties.getDefault();
    public static boolean g_stop_general_mining = true;
    public static long G_FRAME = 1000 / 30;
    public static PoolThread poolThread = SATObjFactory.GetPoolThread();
    private Lock lock = new ReentrantLock(true);
    public static Block CreateMainBlock() throws SatException {
        long tm = SmartxEpochTime.get_timestamp();
        Block blk = new Block();
        blk.header.headtype = 1;
        blk.header.btype = Block.BLKType.SMARTX_MAIN;
        blk.header.timestamp = tm;
        blk.time = Tools.TimeStamp2DateEx(tm);
        blk.epoch = SmartxEpochTime.EpochTime(SmartxEpochTime.StrToStamp(blk.time));
        assert SmartxCore.G_Wallet != null;
        if (SmartXWallet.wallettype == SmartXWallet.SECP_WALLETTYPE) {
            blk.header.address = SmartxCore.G_Wallet.getAddress();
        } else if (SmartXWallet.wallettype == SmartXWallet.FAST_WALLETTYPE) {
            blk.header.address = SmartxCore.G_Wallet.baseKey.toAddressString();
        }
        blk.timenum = SmartxEpochTime.CalTimeEpochNum(tm);
        blk.header.nonce = Tools.getUUID();
        blk.header.amount = new BigInteger("1024");
        blk.nodename = DataBase.G_NAME;
        blk.diff = "1";
        blk.header.random = "1";
        log.info("------------------------------------------------------------------------------");
        log.info("main_create:[" + blk.time + "] " + blk.header.hash);
        return blk;
    }
    public static boolean CheckNTP() {
        return true;
    }
    public static String CreateAccount() throws SatException, SQLException {
        AccountDB accdb = SATObjFactory.GetAccDB();
        Account acc = Account.CreateAccount();
        accdb.SaveAccount(acc);
        return acc.address;
    }
    public Block LinkTxBlocks(Block curblk) throws SatException, SQLException {
        BlockCache cache = SATObjFactory.GetCache();
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        TransDB smartxdb = SATObjFactory.GetTxDB();
        for (int i = 0; i < cache.recvcache.size(); i++) {
            Block txblk = cache.recvcache.get(i);
            curblk = blockdag.InitRefField(curblk, txblk);
            smartxdb.SaveBlock(txblk, DataBase.SMARTX_BLOCK_EPOCH);
        }
        cache.recvcache.clear();
        return curblk;
    }
    public synchronized Block BlockReference(Block curblk, List<Block> links) throws SatException, SQLException, SignatureException {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        BlockMainTop maintop = SATObjFactory.GetMainTop();
        TransDB smartxdb = SATObjFactory.GetTxDB();
        Block topblk = maintop.GetTopBlock();
        Block MC = smartxdb.GetLatestMC();
        ArrayList<Block> blks_1 = new ArrayList<Block>();
        for (int i = 0; i < links.size(); i++) {
            ArrayList<Block> blocks = tvblock.GetBackBlocks(links.get(i), DataBase.SMARTX_BLOCK_EPOCH);
            BlockRelation.AddList(blks_1, blocks);
        }
        if (tvblock.CheckMoreCreate(blks_1)) return null;
        blockdag.RefEpochBlock(topblk, MC, curblk);
        log.info(" MC:" + MC.header.hash);
        return MC;
    }
    void DoReferTop() throws SatException, SQLException, SignatureException {
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        BlockMainTop top = SATObjFactory.GetMainTop();
        if (SmartxCore.G_INSTANCE == false) {
            TransDB smartxdb = SATObjFactory.GetTxDB();
            int num = (int) SmartxEpochTime.GetCurTimeNum();
            SmartxCore.G_INSTANCE = true;
            {
                ArrayList<Block> blks = blockdag.ReMoveMineTop();
                BlockRelation.AddList(DataBase.G_FRONTREF, blks);
            }
        }
    }
    public void SaveBlock(Block blk) throws SatException, SQLException {
        TransDB txdb = SATObjFactory.GetTxDB();
        txdb.SaveBlock(blk, DataBase.SMARTX_BLOCK_EPOCH);
    }
    public List<Block> MoveChainTop() throws SatException, SQLException, SignatureException {
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        List<Block> blks = blockdag.ReMoveMineTop();
        DataBase.G_FRONTREF.clear();
        BlockRelation.AddList(DataBase.G_FRONTREF, blks);
        return DataBase.G_FRONTREF;
    }
    public void IncrsHeight(Block curblk) {
        BlockMainTop top = SATObjFactory.GetMainTop();
        Block topblk = top.GetTopBlock();
        curblk.height = (topblk == null ? 1 : top.GetTopBlock().height + 1);
        log.info("settop:" + top.GetTopBlock().nodename + " " + top.GetTopBlock().header.hash + " " + curblk.epoch + " " + Tools.CalTimeEpoch2Num(curblk.epoch));
    }
    public Block DoMineBlock(int start) throws SatException, SQLException {
        Block curblk = null;
        List<Block> links = null;
        try {
            curblk = CreateMainBlock();
            links = MoveChainTop();
            Block MC = BlockReference(curblk, links);
            if (MC != null) {
                IncrsHeight(curblk);
                return curblk;
            }
            return null;
        } catch (SatException e) {
            if (e.errcode == ErrCode.SAT_GETMCBLOCK_ERROR) {
                return null;
            }
            throw e;
        } catch (Exception e) {
            log.error("errcode:" + e);
            g_stop_general_mining = true;
        }
        return curblk;
    }
    private boolean GetRunStatus() {
        if (true == g_stop_general_mining) {
            if (DataBase.G_Status.GetStatus() == SmartxStatus.STATUS.SMARTX_STATUS_MINING)
                DataBase.G_Status.SetStatus(SmartxStatus.STATUS.SMARTX_STATUS_NORMAL);
            return false;
        }
        DataBase.G_Status.SetStatus(SmartxStatus.STATUS.SMARTX_STATUS_MINING);
        return true;
    }
    void TimeUpdate(long begin, long frame) {
        long curTm = SmartxEpochTime.get_timestamp();
        long tm = curTm - begin;
        if (tm < frame) {
            SmartxEpochTime.Sleep(frame - tm);
        }
    }
    public SmartXWallet GetSignWallet() {
        assert SmartxCore.G_Wallet != null;
        return SmartxCore.G_Wallet;
    }
    public void SignBlock(Block blk) throws SignatureException {
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        BlockHash bhash = new BlockHash();
        blk.sign = blockdag.SignBlock(blk.header.hash, GetSignWallet());
        boolean result = false;
        if (SmartXWallet.wallettype == SmartXWallet.FAST_WALLETTYPE) {
            result = Key25519.verify2(blk.header.hash, blk.sign, blk.header.address);
        } else {
            result = SmartXWallet.verify(blk.ToSignStringBase58(), blk.sign, blk.header.address);
        }
        if (result != true) {
            log.error("eror: sign error");
        }
    }
    public void SetRandom(Block blk, String random, BlockDAG blockdag) {
        blk.header.random = random;
    }
    public void CreateBlkHash(Block block) {
        block.header.hash = Sha256.getH256(block);
        log.info("gen hash:" + block.header.hash);
    }
    public void DoSyncBlock() throws SatException, SQLException, SignatureException {
        try {
            SyncThread.SyncHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void DoRuleQuery(Block blk) throws Exception {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        Consensus cons = SATObjFactory.GetConsensus();
        MessageHandle msghandle = SATObjFactory.GetMessageHandle();
        assert (blk != null);
        Block MC = null;
        if ((MC = tvblock.GetMCBlock((int) blk.height)) == null) {
            MC = msghandle.RuleQuery(blk, 5000);
            if (null == MC) {
                if (cons.ReSendBlock()) return;
            }
            smartxdb.SaveRuleSign(MC, DataBase.SMARTX_BLOCK_EPOCH);
        }
        blockdag.AddBlock(MC);
    }
    public void QueryCoinBase() throws SatException {
        if (SmartXWallet.wallettype == SmartXWallet.SECP_WALLETTYPE) {
            assert (SmartxCore.G_Wallet != null);
        } else if (SmartXWallet.wallettype == SmartXWallet.FAST_WALLETTYPE) {
            if (null == SmartxCore.G_Wallet.baseKey) {
                throw new SatException(ErrCode.SAT_CHECKCOINBASE_ERROR, "the coinbase is empty");
            }
        }
    }
    public static MineHelper.MinePower minePowerOur = new MineHelper.MinePower();
    public static MineHelper.MinePower minePowerTotal = new MineHelper.MinePower();
    public void run() {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        MessageHandle msghandle = SATObjFactory.GetMessageHandle();
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        BlockMainTop top = SATObjFactory.GetMainTop();
        Block blk = null;
        while (true) {
            long begin = SmartxEpochTime.get_timestamp();
            if (!GetRunStatus()) {
                SmartxEpochTime.Sleep(500);
                continue;
            }
            try {
                QueryCoinBase();
                DoReferTop();
                if (DataBase.G_Status.GetStatus() == SmartxStatus.STATUS.SMARTX_STATUS_NORMAL || DataBase.G_Status.GetStatus() == SmartxStatus.STATUS.SMARTX_STATUS_MINING) {
                    long tm = System.currentTimeMillis();
                    String timeStamp = Tools.TimeStamp2DateEx(tm);
                    int time = Integer.parseInt(timeStamp.substring(17)) % (SmartxEpochTime.MAIN_CHAIN_PERIOD / 1000);
                    if (blk == null && time > SmartxEpochTime.G_STARTS[0] && time < SmartxEpochTime.G_STARTS[1]) {
                        DoSyncBlock();
                        DoRuleQuery(top.GetTopBlock());
                        blk = DoMineBlock(time);
                        BlockStats.blk = blk;
                        if (null == blk) {
                            log.warn("create block error, height:" + top.GetTopBlock().height + " mctop:" + top.GetMCTopBlock().header.hash + " top:" + top.GetTopBlock().header.hash);
                            SmartxEpochTime.Sleep(1000);
                            continue;
                        }
                        Block selftop = top.GetTopBlock();
                        Block mctop = tvblock.GetMCBlock(selftop, smartxdb.GetDbtype(selftop));
                        top.SetMCTopBlock(mctop);
                        poolThread.doMiningWork(blk);
                    }
                    if (blk != null && time < SmartxEpochTime.G_STARTS[1]) {
                        poolThread.doMiningWork(blk);
                        tvblock.UpdateCache();
                    }
                    if (blk != null && Tools.isEmpty(blk.header.hash) && time > SmartxEpochTime.G_STARTS[1]) {
                        String random = poolThread.getMiningWork(blk);
                        SetRandom(blk, random, blockdag);
                        CreateBlkHash(blk);
                        SignBlock(blk);
                        blk.mkl_hash = MerkleTree.ComputeRoot(blk);
                        SaveBlock(blk);
                        minePowerOur.computingPower(blk);
                        msghandle.BroadCastMBlock(blk);
                        top.SetTopBlock(blk);
                        blk = null;
                    }
                }
                TimeUpdate(begin, G_FRAME);
            } catch (SatException e) {
                e.printStackTrace();
                blk = null;
                g_stop_general_mining = false;
                log.warn(e.errcode + " " + e.res_info);
            } catch (Exception e) {
                e.printStackTrace();
                g_stop_general_mining = false;
                blk = null;
            }
        }
    }
}
