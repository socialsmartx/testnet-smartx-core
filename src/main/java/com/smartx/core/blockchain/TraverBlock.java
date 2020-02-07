package com.smartx.core.blockchain;

import static com.smartx.db.BlockStats.blk;

import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.block.Block;
import com.smartx.block.Field;
import com.smartx.config.SystemProperties;
import com.smartx.core.SmartxCore;
import com.smartx.core.consensus.ErrCode;
import com.smartx.core.consensus.SatException;
import com.smartx.core.consensus.SmartxEpochTime;
import com.smartx.core.coordinate.RuleExecutor;
import com.smartx.crypto.Key;
import com.smartx.db.QueryDB;
import com.smartx.db.TransDB;
import com.smartx.mine.MineHelper;
import com.smartx.util.SortByTime;
import com.smartx.util.Tools;
import com.smartx.wallet.SmartXWallet;

public class TraverBlock {
    public SystemProperties config = SystemProperties.getDefault();
    private static final Logger logger = Logger.getLogger("core");
    public long GetPrevBlock(Block blk) throws SatException, SQLException {
        TransDB txdb = SATObjFactory.GetTxDB();
        long height = 0;
        for (int i = 0; i < blk.Flds.size(); i++) {
            Block tmpblk = txdb.GetBlock(blk.Flds.get(i).hash, txdb.GetDbtype(blk.Flds.get(i).hash));
            if (blk.Flds.get(i).type == Field.FldType.SAT_FIELD_OUT) {
                String resp = ("hash:" + tmpblk.header.hash + " height:" + tmpblk.height + " address:" + tmpblk.header.address + " rulelen:" + Tools.ToRuleSignListByList(tmpblk.ruleSigns).length() + " diff: " + tmpblk.diff + " num:" + tmpblk.timenum + " timestamp:" + tmpblk.header.timestamp + " btype:" + tmpblk.header.btype + " time:" + tmpblk.time + " epoch:" + tmpblk.epoch + " nodename:" + tmpblk.nodename);
                System.out.println(resp);
                for (int j = 0; j < blk.Flds.size(); j++) {
                    System.out.println("	hash:" + blk.Flds.get(j).hash + " " + blk.Flds.get(j).time);
                }
                height = blk.height - 1;
            }
        }
        return height;
    }
    public List<Block> GetAllBolck(int height) throws SatException, SQLException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        int Latestheight = smartxdb.GetLatestHeight(DataBase.SMARTX_BLOCK_HISTORY);
        if (height > Latestheight) {
            return smartxdb.GetAllHeight(height, DataBase.SMARTX_BLOCK_EPOCH);
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
    public Block GetPrevMCBlock(Block blk) throws SatException, SQLException, SignatureException {
        TransDB txdb = SATObjFactory.GetTxDB();
        RuleExecutor executor = SATObjFactory.GetExecutor();
        for (int i = 0; i < blk.Flds.size(); i++) {
            if (blk.Flds.get(i).type == Field.FldType.SAT_FIELD_OUT) {
                Block tmpblk = txdb.GetBlock(blk.Flds.get(i).hash, DataBase.SMARTX_BLOCK_EPOCH);
                if (null == tmpblk) {
                    tmpblk = txdb.GetBlock(blk.Flds.get(i).hash, DataBase.SMARTX_BLOCK_HISTORY);
                }
                if (null == tmpblk) {
                    logger.error("  hash:" + blk.Flds.get(i).hash);
                    return null;
                }
                if (tmpblk.header.btype == Block.BLKType.SMARTX_MAIN || tmpblk.header.btype == Block.BLKType.SMARTX_MAINREF) {
                    logger.info("   tmpblk hash:" + tmpblk.header.hash + " rule len:" + Tools.ToRuleSignListByList(tmpblk.ruleSigns).length());
                    if (executor.verifyRuleSignBlock(tmpblk)) return tmpblk;
                }
            }
        }
        return null;
    }
    public void SortBlockBack(int count) {
        try {
            TransDB smartxdb = SATObjFactory.GetTxDB();
            long height = smartxdb.GetLatestHeight(DataBase.SMARTX_BLOCK_HISTORY);
            ArrayList<Block> blks = smartxdb.GetAllMainHeight(height);
            Collections.sort(blks, new SortByTime());
            Block blk = blks.get(0);
            height = GetPrevBlock(blk);
            Traver(height, --count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void Traver(long height, int count) throws SatException, SQLException {
        TransDB txdb = SATObjFactory.GetTxDB();
        ArrayList<Block> blks = txdb.GetAllMainHeight(height);
        if (blks == null || blks.size() == 0 || height == 0) return;
        if (count == 0) return;
        Collections.sort(blks, new SortByTime());
        Block blk = blks.get(0);
        height = GetPrevBlock(blk);
        Traver(height, --count);
    }
    public boolean CheckMoreCreate(ArrayList<Block> blks_1) {
        assert SmartxCore.G_Wallet != null;
        if (blks_1 == null || blks_1.size() == 0) return false;
        HashMap<String, String> tmMap = new HashMap<String, String>();
        String address = "";
        if (SmartXWallet.wallettype == SmartXWallet.SECP_WALLETTYPE) {
            address = SmartxCore.G_Wallet.getAddress();
        } else {
            List<Key> keys = SmartxCore.G_Wallet.fastkeys.getAccounts();
            address = keys.get(0).toAddressString();
        }
        long timenum = blks_1.get(0).timenum;
        for (int i = 0; i < blks_1.size(); i++) {
            if (blks_1.get(i).header.address.equals(address) && blks_1.get(i).timenum != timenum) {
                return true;
            }
        }
        return false;
    }
    public ArrayList<Block> GetBackBlocks(Block blk, int dbtype) throws SatException, SQLException {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        ArrayList<Block> linkblocks = tvblock.GetBackBlockHash(blk.header.hash, dbtype);
        return linkblocks;
    }
    public ArrayList<Block> GetBackBlockHash(String hash, int dbtype) throws SatException, SQLException {
        TransDB txdb = SATObjFactory.GetTxDB();
        return txdb.GetBlockHashBack(hash, dbtype);
    }
    public Block GetNextMC(Block MC, int dbtype) {
        TransDB txdb = SATObjFactory.GetTxDB();
        return txdb.GetNextMC(MC, dbtype);
    }
    public void SortBlockFront(int count) throws SatException, SQLException, SignatureException {
        String genesishash = config.getGenesisHash();
        TransDB txdb = SATObjFactory.GetTxDB();
        String hash = "";
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
    public List<Block> SelectAvailableBlock(List<Block> blocks) throws SQLException {
        ArrayList<Block> targetblocks = new ArrayList<Block>();
        for (int i = 0; i < blocks.size(); i++) {
            if (true == CheckBlockReferEx(blocks.get(i))) {
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
    public void CoordinateMCs(List<Block> tagblocks, HashMap<Integer, Block> hashmap) throws SatException {
        if (tagblocks.size() > 0) {
            hashmap.put((int) tagblocks.get(0).timenum, tagblocks.get(0));
        }
        for (int i = 0; i < tagblocks.size(); i++) {
            blk = tagblocks.get(i);
            Block tmpblk = hashmap.get((int) tagblocks.get(0).timenum);
            if (cmpDiff(blk, tmpblk)) {
                hashmap.put((int) tagblocks.get(0).timenum, blk);
            }
        }
        if (hashmap.size() == 0 || tagblocks.size() == 0 || (blk = hashmap.get((int) tagblocks.get(0).timenum)) == null) {
            throw new SatException(ErrCode.SAT_RULEBACKERFER_ERROR, "can't find the block of backrefer mc");
        }
    }
    public ArrayList<Block> SelLinkBlock(ArrayList<Block> lists) {
        ArrayList<Block> blocks = new ArrayList<Block>();
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).header.btype == Block.BLKType.SMARTX_MAIN || lists.get(i).header.btype == Block.BLKType.SMARTX_MAINREF) {
                blocks.add(lists.get(i));
            }
        }
        return blocks;
    }
    public void RemoveToHistory(Block MC) throws SatException, SQLException, SignatureException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        if (MC == null) return;
        List<Block> lists = tvblock.GetBlockRef(MC);
        if (lists.size() == 0) {
            return;
        }
        for (int i = 0; i < lists.size(); i++) {
            Block tmpblk = smartxdb.GetBlock(lists.get(i).header.hash, DataBase.SMARTX_BLOCK_EPOCH);
            if (tmpblk == null) continue;
            smartxdb.SaveBlock(tmpblk, DataBase.SMARTX_BLOCK_HISTORY);
            smartxdb.RemoveBlock(tmpblk, DataBase.SMARTX_BLOCK_EPOCH);
        }
    }
    public Block GetMCBlockByHeight(List<Block> blks) throws SatException, SignatureException {
        RuleExecutor executor = SATObjFactory.GetExecutor();
        boolean flag = false;
        Block blk = null;
        for (int i = 0; i < blks.size(); i++) {
            blk = blks.get(i);
            if (blk.ruleSigns != null && blk.ruleSigns.size() > 0) {
                if (executor.verifyRuleSignBlock(blk)) {
                    flag = true;
                    break;
                }
            }
        }
        if (!flag && blks.size() > 0)
            throw new SatException(ErrCode.SAT_VERIFY_BLOCK_ARRS, "block verify rulesign error");
        return blk;
    }
    public void UpdateGenensis() throws SatException, SQLException, SignatureException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        int height = smartxdb.GetLatestHeight(DataBase.SMARTX_BLOCK_HISTORY);
        if (height == 0) {
            String gensis = smartxdb.GetGenesisBlockHash(config.getGenesisHash());
            if (gensis.equals("")) {
                Block mcblock = smartxdb.GetBlock(config.getGenesisHash(), DataBase.SMARTX_BLOCK_EPOCH);
                RemoveToHistory(mcblock);
            }
        }
    }
    public void UpdateCache() throws SatException, SQLException, SignatureException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        int height = smartxdb.GetLatestHeight(DataBase.SMARTX_BLOCK_HISTORY);
        Block mcblock = tvblock.GetMCBlock(height, DataBase.SMARTX_BLOCK_HISTORY);
        Block premcblock = mcblock;
        for (; ; ) {
            if (null == mcblock) break;
            long curtimenum = SmartxEpochTime.GetCurTimeNum();
            if (mcblock.timenum < curtimenum - 1) {
                mcblock = smartxdb.GetNextRealMC(mcblock, DataBase.SMARTX_BLOCK_EPOCH);
                if (mcblock == null) {
                    RemoveToHistory(premcblock);
                    break;
                }
                RemoveToHistory(mcblock);
                premcblock = mcblock;
            } else {
                break;
            }
        }
    }
    public List<Block> GetBlockRef(Block mcBlock) throws SatException, SQLException, SignatureException {
        List<Block> hashs = new ArrayList<Block>();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        TransDB txdb = SATObjFactory.GetTxDB();
        for (int i = 0; i < mcBlock.Flds.size(); i++) {
            Block fldBlk = txdb.GetBlock(mcBlock.Flds.get(i).hash, txdb.GetDbtype(mcBlock.Flds.get(i).hash));
            if (fldBlk == null) continue;
            hashs.add(fldBlk);
            for (int j = 0; j < fldBlk.Flds.size(); j++) {
                Block tmp = txdb.GetBlock(fldBlk.Flds.get(j).hash, txdb.GetDbtype(fldBlk.Flds.get(j).hash));
                if (tmp != null) {
                    hashs.add(tmp);
                }
            }
        }
        hashs.add(mcBlock);
        return hashs;
    }
    public long GetAllBlocks(Block mcBlock) throws SatException, SQLException, SignatureException {
        TransDB txdb = SATObjFactory.GetTxDB();
        long total = 0;
        for (int i = 0; i < mcBlock.Flds.size(); i++) {
            Block fldBlk = txdb.GetBlock(mcBlock.Flds.get(i).hash, txdb.GetDbtype(mcBlock.Flds.get(i).hash));
            if (fldBlk == null || fldBlk.header.btype == Block.BLKType.SMARTX_TXS) continue;
            total++;
            for (int j = 0; j < fldBlk.Flds.size(); j++) {
                Block tmp = txdb.GetBlock(fldBlk.Flds.get(j).hash, txdb.GetDbtype(fldBlk.Flds.get(j).hash));
                if (tmp != null && tmp.header.btype != Block.BLKType.SMARTX_MAIN) {
                    total++;
                }
            }
        }
        return total;
    }
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
        TransDB smartxdb = SATObjFactory.GetTxDB();
        if (blk.header.btype == Block.BLKType.SMARTX_MAIN || blk.header.btype == Block.BLKType.SMARTX_MAINREF) {
            for (int i = 0; i < blk.Flds.size(); i++) {
                int dbtype = smartxdb.GetDbtype(blk.Flds.get(i).hash);
                if (smartxdb.GetBlock(blk.Flds.get(i).hash, dbtype) == null) {
                    logger.error("  mhash: " + blk.header.hash + " refer hash:" + blk.Flds.get(i).hash + " isn't exist");
                    throw new SatException(ErrCode.SAT_CHECKREFER_ERROR, "depend block isnot exist!");
                }
            }
        }
    }
    public boolean CheckBlockRefer2(Block blk) throws SatException, SQLException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        if (blk.header.btype == Block.BLKType.SMARTX_MAIN || blk.header.btype == Block.BLKType.SMARTX_MAINREF) {
            for (int i = 0; i < blk.Flds.size(); i++) {
                int dbtype = smartxdb.GetDbtype(blk.Flds.get(i).hash);
                if (smartxdb.GetBlock(blk.Flds.get(i).hash, dbtype) == null) {
                    return true;
                }
            }
        }
        return false;
    }
    public Block GetMCBlock(Block blk, int dbtype) throws SatException, SQLException, SignatureException {
        TransDB txdb = SATObjFactory.GetTxDB();
        RuleExecutor executor = SATObjFactory.GetExecutor();
        List<Block> blks = txdb.GetAllMainBlockByNum((int) blk.timenum, dbtype);
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        Block MC = null;
        for (int i = 0; i < blks.size(); i++) {
            MC = blks.get(i);
            if (MC.ruleSigns != null) {
                if (executor.verifyRuleSignBlock(MC)) {
                    CheckBlockRefer(MC);
                    return MC;
                }
            }
        }
        return null;
    }
    public Block GetMCBlock(int height) throws SatException, SQLException, SignatureException {
        TransDB txdb = new TransDB();
        List<Block> blks = Collections.synchronizedList(new ArrayList<Block>());
        RuleExecutor executor = SATObjFactory.GetExecutor();
        txdb.GetAllMainBlockByHeight(blks, height, DataBase.SMARTX_BLOCK_EPOCH);
        if (blks.size() > 0) {
            Block MC = null;
            if ((MC = GetMCBlock(blks)) == null) {
                txdb.GetAllMainBlockByHeight(blks, height, DataBase.SMARTX_BLOCK_HISTORY);
                return GetMCBlock(blks);
            }
            return MC;
        } else {
            txdb.GetAllMainBlockByHeight(blks, height, DataBase.SMARTX_BLOCK_HISTORY);
            return GetMCBlock(blks);
        }
    }
    public Block GetMCBlock(List<Block> blocks) throws SignatureException {
        RuleExecutor executor = SATObjFactory.GetExecutor();
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
    public Block GetMCBlock(int height, int dbtype) throws SatException, SQLException, SignatureException {
        TransDB txdb = new TransDB();
        List<Block> blks = Collections.synchronizedList(new ArrayList<Block>());
        RuleExecutor executor = SATObjFactory.GetExecutor();
        txdb.GetAllMainBlockByHeight(blks, height, dbtype);
        Block MC = null;
        for (int i = 0; i < blks.size(); i++) {
            MC = blks.get(i);
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
