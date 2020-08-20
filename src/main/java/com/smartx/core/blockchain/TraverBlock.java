package com.smartx.core.blockchain;

import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.block.Block;
import com.smartx.config.SystemProperties;
import com.smartx.core.SmartxCore;
import com.smartx.core.consensus.ErrCode;
import com.smartx.core.consensus.SatException;
import com.smartx.core.coordinate.RuleExecutor;
import com.smartx.core.coordinate.RuleThread;
import com.smartx.db.QueryDB;
import com.smartx.db.TransDB;
import com.smartx.mine.MineHelper;
import com.smartx.util.Tools;
import com.smartx.wallet.SmartXWallet;

public class TraverBlock {
    public SystemProperties config = SystemProperties.getDefault();
    private static final Logger logger = Logger.getLogger("core");
    protected TransDB txdb = SATObjFactory.GetTxDB();
    protected RuleExecutor executor = SATObjFactory.GetExecutor();

    public long ShowMcRefers(Block blk) {
        TransDB txdb = SATObjFactory.GetTxDB();
        long height = 0;
        String resp = ("hash:" + blk.header.hash + " height:" + blk.height + " node:" + blk.nodename + " address:" + blk.header.address + " rulelen:" + Tools.ToRuleSignListByList(blk.ruleSigns).length() + " diff: " + blk.diff + " num:" + blk.timenum + " timestamp:" + blk.header.timestamp + " btype:" + blk.header.btype + " time:" + blk.time + " epoch:" + blk.epoch + " nodename:" + blk.nodename);
        System.out.println(resp);

        for (int i = 0; i < blk.Flds.size(); i++) {
            Block refblk = txdb.GetBlock(blk.Flds.get(i).hash, DataBase.SMARTX_BLOCK_HISTORY);
            System.out.println("	hash:" + refblk.header.hash + " height:" + refblk.height + " node:" + refblk.nodename + " " + refblk.time + " " + refblk.header.btype);
        }
        return height;
    }
    public List<Block> GetAllBolck(int height) throws SatException, SQLException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        int Latestheight = smartxdb.GetLatestHeight(DataBase.SMARTX_BLOCK_HISTORY);
        if (height > Latestheight) {
            return new ArrayList<Block>();
        } else {
            return smartxdb.GetAllHeight(height, DataBase.SMARTX_BLOCK_HISTORY);
        }
    }
    public long GetMineBlockHeight() {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        try {
            Block MC = smartxdb.GetLatestMC();
            if (null == MC) return 1;
            return MC.height + 1;
        } catch (SignatureException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public void SortBlockBack(int count) {
        try {
            long height = txdb.GetLatestHeight(DataBase.SMARTX_BLOCK_HISTORY);
            ArrayList<Block> blks = txdb.GetAllMainHeight(height);
            List<Block> tmpblocks = new ArrayList<Block>();
            Block MC = SelectMCBlock(blks);
            String path = Tools.GetSortPath();
            while(count > 0 && MC != null) {
                //ShowMcRefers(MC);
                String str = QueryDB.ShowBlockEx(MC.header.hash);
                Tools.WriteFile(path, str);
                for (int j=0; j<MC.Flds.size(); j++){
                    Block block = txdb.GetBlock(MC.Flds.get(j).hash, DataBase.SMARTX_BLOCK_HISTORY);
                    tmpblocks.add(block);
                }
                if (tmpblocks == null) break;
                MC = SelectMCBlock(tmpblocks);
                count = count -1;
                tmpblocks.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getRealAddress() {
        if (SmartXWallet.wallettype == SmartXWallet.FAST_WALLETTYPE)
            return SmartxCore.G_Wallet.getFastAddress();
        return SmartxCore.G_Wallet.getAddress();
    }

    public boolean CheckMoreCreate(List<Block> blks_1) {
        assert SmartxCore.G_Wallet != null;
        if (blks_1 == null || blks_1.size() == 0) return false;
        String address = getRealAddress();
        // must sort
        long timenum = blks_1.get(0).timenum;
        for (int i = 0; i < blks_1.size(); i++) {
            if (blks_1.get(i).header.address.equals(address) && blks_1.get(i).timenum != timenum) {
                return true;
            }
        }
        return false;
    }
    public List<Block> GetBackBlockHash(String hash, int dbtype) throws SatException, SQLException {
        return txdb.GetBlockHashBack(hash, dbtype);
    }
    public Block GetNextMC(Block MC, int dbtype) {
        return txdb.GetNextMC(MC, dbtype);
    }
    public void SortBlockFront(int count) throws SatException, SQLException, SignatureException {
        String genesishash = config.getGenesisHash();
        Block gsblk = txdb.GetBlock(genesishash, DataBase.SMARTX_BLOCK_HISTORY);
        if (null == gsblk) {
            System.out.println("unexpect genesis block");
            return;
        }
        String path = Tools.GetSortPath();
        String str = QueryDB.ShowBlockEx(gsblk.header.hash);
        Tools.WriteFile(path, str);
        int lcount = 1;
        Block blk = gsblk;
        for (; ; ) {
            blk = txdb.GetNextRealMC(blk, DataBase.SMARTX_BLOCK_HISTORY);
            if (blk == null) break;
            if (lcount >= count) {
                str = QueryDB.ShowBlockEx(blk.header.hash);
                Tools.WriteFile(path, str);
            }
            ++lcount;
        }
    }
    public boolean SelectMaxPower(Block blk){
        return true;
    }
    public List<Block> SelectAvailableBlock(List<Block> blocks) throws SQLException {
        List<Block> targetblocks = new ArrayList<Block>();
        for (int i = 0; i < blocks.size(); i++) {
            if (true == CheckBlockReferEx(blocks.get(i)) &&
                    SelectMaxPower(blocks.get(i))) {
                targetblocks.add(blocks.get(i));
            }
        }
        return targetblocks;
    }
    public boolean cmpDiff(Block blk1, Block blk2) {
        if (MineHelper.cmpDiff(blk1.diff, blk2.diff)) {
            return true;
        }
        return false;
    }
    public void CoordinateMCs2(List<Block> tagblocks) throws SatException {
        if (tagblocks.size() > 0) {
            RuleThread.G_MaxDiffBlock = tagblocks.get(0);
        } else{
            throw new SatException(ErrCode.SAT_RULEBACKERFER_ERROR, "can't find the block of backrefer mc");
        }
        for (int i = 0; i < tagblocks.size(); i++) {
            Block blk = tagblocks.get(i);
            if (cmpDiff(blk, RuleThread.G_MaxDiffBlock)) {
                RuleThread.G_MaxDiffBlock = blk;
            }
        }
    }
    public List<Block> SelLinkBlock(List<Block> lists) {
        List<Block> blocks = new ArrayList<Block>();
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).header.btype == Block.BLKType.SMARTX_MAIN ||
                    lists.get(i).header.btype == Block.BLKType.SMARTX_MAINREF) {
                blocks.add(lists.get(i));
            }
        }
        return blocks;
    }
    //get double quoted transactions
    public List<Block> GetBlockRef(Block mcBlock){
        List<Block> hashs = new ArrayList<Block>();
        for (int i = 0; i < mcBlock.Flds.size(); i++) {
            Block fldBlk = txdb.GetBlock(mcBlock.Flds.get(i).hash, DataBase.SMARTX_BLOCK_HISTORY);
            if (fldBlk == null) continue;
            hashs.add(fldBlk);
            for (int j = 0; j < fldBlk.Flds.size(); j++) {
                Block tmp = txdb.GetBlock(fldBlk.Flds.get(j).hash,  DataBase.SMARTX_BLOCK_HISTORY);
                if (tmp != null) {
                    hashs.add(tmp);
                }
            }
        }
        hashs.add(mcBlock);
        return hashs;
    }
    public long GetAllBlocks(Block mcBlock) {
        long total = 0;
        for (int i = 0; i < mcBlock.Flds.size(); i++) {
            Block fldBlk = txdb.GetBlock(mcBlock.Flds.get(i).hash, DataBase.SMARTX_BLOCK_HISTORY);
            if (fldBlk == null || fldBlk.header.btype == Block.BLKType.SMARTX_TXS) continue;
            total++;
            for (int j = 0; j < fldBlk.Flds.size(); j++) {
                Block tmp = txdb.GetBlock(fldBlk.Flds.get(j).hash, DataBase.SMARTX_BLOCK_HISTORY);
                if (tmp != null && tmp.header.btype != Block.BLKType.SMARTX_MAIN) {
                    total++;
                }
            }
        }
        return total;
    }
    // Check if the reference block exists
    // exist true
    // no exist false
    public boolean CheckBlockReferEx(Block mblock) throws SQLException {
        try {
            CheckBlockRefer(mblock);
        } catch (SatException e) {
            if (e.errcode == ErrCode.SAT_CHECKREFER_ERROR) {
                return false;
            }
        }
        return true;
    }
    public void CheckBlockRefer(Block blk) throws SatException, SQLException {
        if (blk.header.btype == Block.BLKType.SMARTX_MAIN || blk.header.btype == Block.BLKType.SMARTX_MAINREF) {
            for (int i = 0; i < blk.Flds.size(); i++) {
                if (txdb.GetBlock(blk.Flds.get(i).hash, DataBase.SMARTX_BLOCK_HISTORY) == null) {
                    logger.warn("  mhash: " + blk.header.hash);
                    logger.warn("    need refer hash:" + blk.Flds.get(i).hash);
                    throw new SatException(ErrCode.SAT_CHECKREFER_ERROR, "depend block isnot exist!");
                }
            }
        }
    }
    public Block GetMCBlock(int height) throws SatException, SQLException, SignatureException {
        List<Block> blks = Collections.synchronizedList(new ArrayList<Block>());
        txdb.GetAllMainBlockByHeight(blks, height, DataBase.SMARTX_BLOCK_HISTORY);
        if (blks.size() > 0) {
            Block MC = null;
            if ((MC = SelectMCBlock(blks)) == null) {
                return null;
            }
            return MC;
        }
        return null;
    }
    public Block SelectMCBlock(List<Block> blocks) throws SignatureException {
        Block MC = null;
        for (int i = 0; i < blocks.size(); i++) {
            MC = blocks.get(i);
            if (MC.ruleSigns != null) {
                if (executor.verifyRuleSignBlock(MC)) {
                    return MC;
                }
            }
        }
        return null;
    }
    public Block TrimBlock(Block blk) {
        for (int i = 0; i < blk.Flds.size(); i++) {
            blk.Flds.get(i).time = "0000";
        }
        return blk;
    }
}
