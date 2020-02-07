package com.smartx.block;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.core.blockchain.DataBase;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.consensus.ErrCode;
import com.smartx.core.consensus.SatException;
import com.smartx.db.TransDB;

public class BlockRelation {
    public Block m_block = null;
    protected static Logger logger = Logger.getLogger(BlockRelation.class);
    public BlockRelation(Block blk) {
        m_block = blk;
    }
    public static boolean IsInArray(String hash, List<Block> blks) {
        for (int i = 0; i < blks.size(); i++) {
            if (hash.equals(blks.get(i).header.hash)) {
                return true;
            }
        }
        return false;
    }
    public void CheckLoopRef(Block blk, Block blkref) throws SatException {
        for (int i = 0; i < blkref.Flds.size(); i++) {
            String hash = blkref.Flds.get(i).hash;
            if (hash.equals(blk.header.hash)) {
                throw new SatException(ErrCode.SAT_LOOPREF_ERROR, "block ref loop error:" + blk.header.hash);
            }
        }
    }
    public static int FindInList(Block blk, List<Block> blks) {
        for (int i = 0; i < blks.size(); i++) {
            if (blk.header.hash.equals(blks.get(i).header.hash)) {
                return 1;
            }
        }
        return 0;
    }
    public static void AddBlock(List<Block> dst, Block curblk) {
        if (0 == FindInList(curblk, dst)) {
            dst.add(curblk);
        }
    }
    public static void AddList(List<Block> dst, List<Block> src) {
        for (int i = 0; i < src.size(); i++) {
            if (0 == FindInList(src.get(i), dst)) {
                dst.add(src.get(i));
            }
        }
    }
    public int IsRepeat() throws SatException, SQLException {
        TransDB txdb = SATObjFactory.GetTxDB();
        return txdb.IsRepeat(m_block);
    }
}
