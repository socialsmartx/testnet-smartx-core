package com.smartx.core.consensus;

import java.math.BigInteger;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.block.Block;
import com.smartx.config.SystemProperties;
import com.smartx.core.SmartxCore;
import com.smartx.core.blockchain.*;
import com.smartx.core.syncmanager.MerkleTree;
import com.smartx.crypto.Sha256;
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
    public TransDB smartxdb = SATObjFactory.GetTxDB();
    public BlockMainTop top = SATObjFactory.GetMainTop();
    public TraverBlock tvblock = SATObjFactory.GetTravBlock();
    public BlockDAG blockdag = SATObjFactory.GetBlockDAG();
    public static Block CreateMainBlock() {
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
    public synchronized Block BlockReference(Block curblk, List<Block> links) throws SatException, SQLException, SignatureException {
        Block topblk = top.GetMCBlock();
        Block MC = tvblock.SelectMCBlock(links);
        assert (curblk.timenum > topblk.timenum);
        long nowheight = MC.height + 1;
        log.info("do refer nonce:" + curblk.header.nonce + " num:" + curblk.timenum + " time:" + curblk.time + " type:" + curblk.header.btype);
        log.info("front:" + topblk.timenum + " now:" + curblk.timenum + " now height:" + nowheight);
        blockdag.RefBlockLists(curblk,links);
        log.info(" MC:" + MC.header.hash);
        return MC;
    }
    public void SaveBlock(Block blk) throws SatException, SQLException {
        smartxdb.SaveBlock(blk, DataBase.SMARTX_BLOCK_HISTORY);
    }
    public List<Block> MoveChainTop() throws SatException, SQLException, SignatureException {
        Block MC = smartxdb.GetLatestMC();
        top.SetMCBlock(MC);
        List<Block> chain = smartxdb.GetAllHeight(MC.height,DataBase.SMARTX_BLOCK_HISTORY);
        List<Block> blockoks = tvblock.SelLinkBlock(chain);
        return blockoks;
    }
    public void IncrsHeight(Block curblk) {
        Block topblk = top.GetMCBlock();
        curblk.height = (topblk == null ? 1 : top.GetMCBlock().height + 1);
        log.info("settop:" + top.GetMCBlock().nodename + " " + top.GetMCBlock().header.hash + " " + curblk.epoch + " " + Tools.CalTimeEpoch2Num(curblk.epoch));
    }
    public Block DoMineBlock() throws SatException{
        Block curblk = null;
        List<Block> links = null;
        try {
            curblk = CreateMainBlock();
            links = MoveChainTop();
            for (int i=0; i<DataBase.G_TransactionList.size(); i++){
                Block tx = DataBase.G_TransactionList.get(i);
                if (!smartxdb.GetBackRefer(tx, DataBase.SMARTX_BLOCK_HISTORY)) {
                    links.add(tx);
                }
            }
            DataBase.G_TransactionList.clear();
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
            e.printStackTrace();
            log.error("errcode:" + e);
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
        log.info("gen hash:" + block.header.hash + " height:" + block.height + " clients:" +
                poolThread.clients.size() + " reward blocks:" + poolThread.powsForAddress.size());
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
        MessageHandle msghandle = SATObjFactory.GetMessageHandle();
        Block blk = null;
        while (true) {
            long begin = SmartxEpochTime.get_timestamp();
            if (!GetRunStatus()) {
                SmartxEpochTime.Sleep(500);
                continue;
            }
            try {
                QueryCoinBase();
                if (DataBase.G_Status.GetStatus() == SmartxStatus.STATUS.SMARTX_STATUS_NORMAL ||
                        DataBase.G_Status.GetStatus() == SmartxStatus.STATUS.SMARTX_STATUS_MINING) {
                    long tm = System.currentTimeMillis();
                    String timeStamp = Tools.TimeStamp2DateEx(tm);
                    int time = Integer.parseInt(timeStamp.substring(17)) % (SmartxEpochTime.MAIN_CHAIN_PERIOD / 1000);
                    if (blk == null && time > SmartxEpochTime.G_STARTS[0] && time < SmartxEpochTime.G_STARTS[1]) {
                        blk = DoMineBlock();
                        BlockStats.blk = blk;
                        if (null == blk) {
                            log.warn("mining block failure, not find the main block height:" + top.GetMCBlock().height + " mctop:" + top.GetMCBlock().header.hash + " top:" + top.GetMCBlock().header.hash);
                            SmartxEpochTime.Sleep(5000);
                            continue;
                        }
                        poolThread.doMiningWork(blk);
                    }
                    if (blk != null && time < SmartxEpochTime.G_STARTS[1]) {
                        poolThread.doMiningWork(blk);
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
                        poolThread.clients.clear();
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
