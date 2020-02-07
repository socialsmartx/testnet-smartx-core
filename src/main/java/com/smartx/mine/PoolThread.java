package com.smartx.mine;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.smartx.block.Block;
import com.smartx.cli.SmartxCommands;
import com.smartx.core.SmartxCore;
import com.smartx.core.blockchain.BlockDAG;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.TraverBlock;
import com.smartx.core.consensus.GeneralMine;
import com.smartx.core.consensus.SatException;
import com.smartx.core.consensus.SmartxEpochTime;
import com.smartx.core.coordinate.RuleExecutor;
import com.smartx.core.ledger.GetHeightEvent;
import com.smartx.crypto.Sha256;
import com.smartx.db.TransDB;
import com.smartx.event.PubSub;
import com.smartx.event.PubSubEvent;
import com.smartx.event.PubSubFactory;
import com.smartx.event.PubSubSubscriber;
import com.smartx.message.Message;

public class PoolThread implements Runnable, PubSubSubscriber {
    private static Logger log = Logger.getLogger(PoolThread.class);
    public static boolean g_do_mining = true;
    public class PowsForAddress {
        public String random = "";
        public String address = "";
        public long diffCur = 0;
        public String hash = "";
    }
    public PowsForAddress CurDiffAddress = new PowsForAddress();
    public String Miningblk;
    public Map<Long, PowsForAddress> powsForAddress = new HashMap<Long, PowsForAddress>();
    public PoolThread() {
        PubSub pubSub = PubSubFactory.getDefault();
        pubSub.subscribe(this, GetHeightEvent.class);
    }
    public void onPubSubEvent(PubSubEvent event) {
        String json = event.GetMessage();
        Message message = Message.FromJson(json);
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        if (message.args.get("command").equals(Message.MESSAGE_GET_LATESTHEIGHT)) {
            long height = Long.parseLong(message.args.get("height"));
            BlockDAG blockDAG = SATObjFactory.GetBlockDAG();
            TransDB txDB = SATObjFactory.GetTxDB();
            RuleExecutor ruleExecutor = SATObjFactory.GetExecutor();
            synchronized (this) {
                Iterator<Map.Entry<Long, PowsForAddress>> it = powsForAddress.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Long, PowsForAddress> entry = it.next();
                    if (entry.getKey() + 3 < height) {
                        try {
                            PowsForAddress value = entry.getValue();
                            int dbtype = txDB.GetDbtype(value.hash);
                            Block mcBlk = txDB.GetBlock(value.hash, dbtype);
                            if (!ruleExecutor.verifyRuleSignBlock(mcBlk)) continue;
                            String PoolAddress = SmartxCore.G_Wallet.getRealAddress();
                            if (mcBlk != null && mcBlk.header.address.equals(PoolAddress) && mcBlk.header.random.equals(value.random)) {
                                log.info("PoolThread miner: " + value.address + " Reward: " + blockdag.GetRewards(mcBlk.height).longValue());
                                Block blk = SmartxCommands.MakeTransfer(value.address, blockdag.GetRewards(mcBlk.height).longValue());
                                blockDAG.AddBlock(blk);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        it.remove();
                    }
                }
            }
        }
    }
    public String find_nonce(String content, int start, int attempts, int step) {
        return "";
    }
    public void mining() {
        java.util.Random temp = new java.util.Random(System.currentTimeMillis());
        long maxValue = Long.MAX_VALUE;
        long value = (long) (temp.nextLong() % maxValue);
        String hex = Long.toHexString(value);
        String hash = Sha256.getH256(Miningblk + hex);
        long diff = MineHelper.getHashDiff(hash);
        if (CurDiffAddress.diffCur == 0 || MineHelper.cmpDiff(diff, CurDiffAddress.diffCur)) {
            CurDiffAddress.diffCur = diff;
            CurDiffAddress.random = hex;
            CurDiffAddress.address = "";
        }
    }
    public boolean SetRandom(String address, String hex) {
        String hash = Sha256.getH256(Miningblk + hex);
        long diff = MineHelper.getHashDiff(hash);
        synchronized (this) {
            if (CurDiffAddress.diffCur == 0 || MineHelper.cmpDiff(diff, CurDiffAddress.diffCur)) {
                CurDiffAddress.diffCur = diff;
                CurDiffAddress.random = hex;
                CurDiffAddress.address = address;
                return true;
            }
        }
        return false;
    }
    Block miningBlock = null;
    public void doMiningWork(Block blk) {
        if (null == blk) return;
        miningBlock = blk;
        Miningblk = Sha256.getH256(blk.ToSignString());
        CurDiffAddress.diffCur = 0;
        mining();
    }
    public String getMiningWork(Block blk) {
        synchronized (this) {
            blk.header.random = CurDiffAddress.random;
            blk.diff = MineHelper.DiffLong2String(CurDiffAddress.diffCur);
            log.info("getMiningWork address: " + CurDiffAddress.address + " random: " + CurDiffAddress.random + " diff: " + blk.diff);
            TraverBlock tvblock = SATObjFactory.GetTravBlock();
            long height = blk.height;
            PowsForAddress temp = new PowsForAddress();
            temp.random = CurDiffAddress.random;
            temp.address = CurDiffAddress.address;
            temp.diffCur = CurDiffAddress.diffCur;
            temp.hash = Sha256.getH256(blk);
            powsForAddress.remove(height);
            powsForAddress.put(height, temp);
            Miningblk = null;
            CurDiffAddress.random = "";
            CurDiffAddress.address = "";
            CurDiffAddress.diffCur = 0;
        }
        return blk.header.random;
    }
    public void NewMineTask() {
    }
    public Message OnGetMineTask(Message message) throws SatException, SQLException {
        Message resp = new Message();
        long taskid = Long.parseLong(message.args.get("taskid"));
        long newtaskid = taskid + 1;
        String mineBlockHeight = "";
        if (miningBlock != null) {
            mineBlockHeight = String.valueOf(miningBlock.height);
        }
        String random = message.args.get("random");
        String address = message.args.get("address");
        String height = message.args.get("height");
        if (random != null && !random.equals("") && height.equals(mineBlockHeight)) {
            if (SetRandom(address, random)) log.info("OnGetMineTask address: " + address + " random: " + random);
        }
        resp.args = new HashMap<String, String>();
        resp.args.put("height", mineBlockHeight);
        resp.args.put("miningblk", Miningblk);
        resp.args.put("taskid", String.valueOf(newtaskid));
        resp.args.put("start", String.valueOf((newtaskid - 1) * 10000));
        resp.args.put("end", String.valueOf((newtaskid - 0) * 10000));
        resp.args.put("powerTotal", GeneralMine.minePowerTotal.GetPower());
        return resp;
    }
    public void run() {
        while (true) {
            if (true == g_do_mining) {
                try {
                    if (Miningblk != null) {
                        mining();
                        SmartxEpochTime.Sleep(5000);
                    } else {
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                continue;
            }
        }
    }
}
