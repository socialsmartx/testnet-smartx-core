package com.smartx.core.syncmanager;

import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.block.Block;
import com.smartx.core.blockchain.BlockDAG;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.SatPeerManager;
import com.smartx.core.blockchain.TraverBlock;
import com.smartx.core.consensus.SatException;
import com.smartx.core.consensus.SmartxEpochTime;
import com.smartx.db.TransDB;
import com.smartx.net.Channel;
import com.smartx.net.msg.MessageHandle;
import com.smartx.util.Tools;

public class SyncThread implements Runnable {
    private static Logger log = Logger.getLogger(SyncThread.class);
    public void run() {
        SmartxEpochTime.Sleep(3000);
        TransDB smartxdb = SATObjFactory.GetTxDB();
        SatPeerManager Peer = SATObjFactory.GetPeerMgr();
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        boolean isSync = true;
        while (true) {
            long begin = SmartxEpochTime.get_timestamp();
            try {
                {
                    SyncHeight();
                    SmartxEpochTime.Sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    static synchronized public boolean SyncHeight() throws Exception {
        TransDB txdb = SATObjFactory.GetTxDB();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        BlockDAG blkdag = SATObjFactory.GetBlockDAG();
        MessageHandle msghandle = SATObjFactory.GetMessageHandle();
        Channel channel = SATObjFactory.GetMessageHandle().GetNodeBest();
        if (channel == null) return false;
        long lcnum = tvblock.GetMineBlockHeight();
        long rmnum = Math.min(msghandle.QueryMineHeight(channel, 5000), lcnum + 10);
        if (lcnum < rmnum) {
            for (long height = lcnum; height <= rmnum; height++) {
                log.info("SyncHeight: " + height);
                List<Block> lists = msghandle.QueryAllBolck(channel, (int) height, 5000);
                for (int i = 0; i < lists.size(); i++) {
                    Block blk = lists.get(i);
                    blkdag.AddBlock(blk);
                }
            }
        }
        SyncMerkleTree(null);
        lcnum = tvblock.GetMineBlockHeight();
        List<String> hashs = CheckBlockRef(lcnum - 1);
        for (int i = 0; i < hashs.size(); i++) {
            log.info("SyncBolck: " + hashs.get(i));
            Block blk = msghandle.QueryBolck(channel, hashs.get(i), 5000);
            blkdag.AddBlock(blk);
        }
        return true;
    }
    public static boolean SyncMerkleTree(Channel channel) throws SatException, SQLException, Exception {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        BlockDAG blkdag = SATObjFactory.GetBlockDAG();
        MessageHandle msghandle = SATObjFactory.GetMessageHandle();
        if (channel == null) {
            channel = SATObjFactory.GetMessageHandle().GetNodeBest();
        }
        long lcnumOld = tvblock.GetMineBlockHeight();
        long lcnumNew = lcnumOld;
        for (lcnumNew = lcnumOld; lcnumNew > 0; lcnumNew--) {
            Block blk = tvblock.GetMCBlock((int) lcnumNew);
            if (blk == null) continue;
            Block rmblk = msghandle.QueryBlockMC(channel, (int) lcnumNew, 5000);
            if (rmblk == null) break;
            if (blk.header.hash.equals(rmblk.header.hash) && blk.mkl_hash.equals(rmblk.mkl_hash)) break;
        }
        for (long height = lcnumNew; height < lcnumOld - 1; height++) {
            List<Block> lists = msghandle.QueryAllBolck(channel, (int) height, 5000);
            if (lists == null) return false;
            for (int i = 0; i < lists.size(); i++) {
                Block blk = lists.get(i);
                blkdag.AddBlock(blk);
            }
        }
        return true;
    }
    static public List<String> CheckBlockRef(long height) throws SatException, SQLException, SignatureException {
        List<String> hashs = new ArrayList<String>();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        TransDB txdb = SATObjFactory.GetTxDB();
        Block mcBlock = tvblock.GetMCBlock((int) height);
        if (mcBlock == null) return hashs;
        for (int i = 0; i < mcBlock.Flds.size(); i++) {
            Block fldBlk = txdb.GetBlock(mcBlock.Flds.get(i).hash, txdb.GetDbtype(mcBlock.Flds.get(i).hash));
            if (fldBlk == null) {
                hashs.add(mcBlock.Flds.get(i).hash);
                continue;
            }
            if (fldBlk.header.btype == Block.BLKType.SMARTX_TXS) {
                continue;
            }
            //
            for (int j = 0; j < fldBlk.Flds.size(); j++) {
                Block tmp = txdb.GetBlock(fldBlk.Flds.get(j).hash, txdb.GetDbtype(fldBlk.Flds.get(j).hash));
                if (tmp == null) {
                    hashs.add(fldBlk.Flds.get(j).hash);
                    continue;
                }
            }
        }
        return hashs;
    }
}
