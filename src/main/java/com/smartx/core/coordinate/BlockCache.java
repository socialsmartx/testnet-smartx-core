package com.smartx.core.coordinate;

import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.block.Block;
import com.smartx.block.BlockRelation;
import com.smartx.block.Field;
import com.smartx.core.blockchain.BlockDAG;
import com.smartx.core.blockchain.DataBase;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.consensus.SatException;
import com.smartx.db.TransDB;

public class BlockCache {
    public ArrayList<Block> recvcache = new ArrayList<Block>();
    private static final Logger logger = Logger.getLogger("core");
    public synchronized void AddBlock(Block netblk) throws SatException, SignatureException {
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        blockdag.VerifySign(netblk);
        BlockRelation.AddBlock(recvcache, netblk);
        logger.info(" cache add:" + netblk.header.hash);
    }
    public synchronized boolean IsFldInCache(List<Field> referblocks) throws SatException, SQLException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        boolean flag = true;
        for (int i = 0; i < referblocks.size(); i++) {
            String hash = referblocks.get(i).hash;
            if (!BlockRelation.IsInArray(hash, recvcache)) {
                if (null == smartxdb.GetBlock(hash, smartxdb.GetDbtype(hash))) flag = false;
            }
        }
        return flag;
    }
    public synchronized Block GetBlock(String hash) throws SatException, SQLException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        for (int i = 0; i < recvcache.size(); i++) {
            if (recvcache.get(i).header.hash.equals(hash)) {
                return recvcache.get(i);
            }
        }
        return smartxdb.GetBlock(hash, smartxdb.GetDbtype(hash));
    }
    public synchronized void SaveRemoveBlock(Block delblk) throws SatException, SQLException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        Iterator<Block> it = recvcache.iterator();
        while (it.hasNext()) {
            Block tmpBlock = it.next();
            if (tmpBlock.header.hash.equals(delblk.header.hash)) {
                smartxdb.SaveBlock(tmpBlock, DataBase.SMARTX_BLOCK_EPOCH);
                it.remove();
                logger.info("   save and remove:" + tmpBlock.header.hash);
            }
        }
    }
    public synchronized void RefreshCache() throws SatException, SQLException {
        ArrayList<Block> saveblocks = new ArrayList<Block>();
        for (int i = 0; i < recvcache.size(); i++) {
            Block mblock = recvcache.get(i);
            if (mblock.header.btype == Block.BLKType.SMARTX_MAIN || mblock.header.btype == Block.BLKType.SMARTX_MAINREF) {
                if (IsFldInCache(mblock.Flds)) {
                    for (int j = 0; j < mblock.Flds.size(); j++) {
                        saveblocks.add(GetBlock(mblock.Flds.get(j).hash));
                    }
                    saveblocks.add(mblock);
                }
            }
        }
        for (int i = 0; i < saveblocks.size(); i++) {
            SaveRemoveBlock(saveblocks.get(i));
        }
    }
}
