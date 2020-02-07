package com.smartx.core.consensus;

import java.security.SignatureException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.smartx.block.Block;
import com.smartx.config.SystemProperties;
import com.smartx.core.SmartxCore;
import com.smartx.core.blockchain.DataBase;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.TraverBlock;
import com.smartx.db.QueryDB;
import com.smartx.db.TransDB;
import com.smartx.util.Tools;

public class CacheBlock implements Runnable {
    protected static Logger logger = Logger.getLogger(CacheBlock.class);
    public SystemProperties config = SystemProperties.getDefault();
    @Test
    public void testInitLoading() throws SatException, SQLException, SignatureException {
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        //InitLoading();
    }
    @Test
    public void tesetBlochToChain() throws SatException, SQLException, SignatureException {
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        TransDB txdb = SATObjFactory.GetTxDB();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        Block blk = txdb.GetBlock("299e20c050b3f9c0d572c852978b2a241ce182725a1b38a05bde2e7e7df09ebe", DataBase.SMARTX_BLOCK_HISTORY);
        SATObjFactory.GetMainTop().SetTopBlock(blk);
        tvblock.UpdateCache();
    }
    public void run() {
        boolean isSync = true;
        QueryDB querydb = SATObjFactory.GetQueryDB();
        querydb.ClearCache(DataBase.SMARTX_BLOCK_EPOCH);
        while (true) {
            TraverBlock tvblock = SATObjFactory.GetTravBlock();
            try {
                long tm = System.currentTimeMillis();
                String timeStamp = Tools.TimeStamp2DateEx(tm);
                tvblock.UpdateGenensis();
                tvblock.UpdateCache();
                SmartxEpochTime.Sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
