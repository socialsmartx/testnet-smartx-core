package com.smartx.core.ledger;

import java.math.BigInteger;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.*;

import org.apache.log4j.Logger;

import com.smartx.Start;
import com.smartx.block.Account;
import com.smartx.block.Block;
import com.smartx.block.Field;
import com.smartx.cli.SmartxCommands;
import com.smartx.config.SystemProperties;
import com.smartx.core.blockchain.BlockDAG;
import com.smartx.core.blockchain.DataBase;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.TraverBlock;
import com.smartx.core.consensus.ErrCode;
import com.smartx.core.consensus.GeneralMine;
import com.smartx.core.consensus.SatException;
import com.smartx.core.consensus.SmartxEpochTime;
import com.smartx.core.syncmanager.MerkleTree;
import com.smartx.db.AccountDB;
import com.smartx.db.TransDB;
import com.smartx.event.PubSub;
import com.smartx.event.PubSubEvent;
import com.smartx.event.PubSubFactory;
import com.smartx.event.PubSubSubscriber;
import com.smartx.message.Message;
import com.smartx.net.Channel;
import com.smartx.net.msg.MessageHandle;
import com.smartx.wallet.Key25519;
import com.smartx.wallet.SmartXWallet;

public class ConsThread implements Runnable, PubSubSubscriber {
    private static Logger log = Logger.getLogger(Start.class);
    public SystemProperties config = SystemProperties.getDefault();
    Map<String, Long> transfers = new HashMap<String, Long>();
    PubSub pubsub = PubSubFactory.getDefault();
    public Long GetHegiht() {
        return curblk.height;
    }
    ;
    Block curblk = null;
    private static long proced_blocks = 0;
    public ConsThread() {
        pubsub.subscribe(this, PubSubEvent2.class);
    }
    public void onPubSubEvent(PubSubEvent event) {
        System.out.println("onPubSubEvent");
    }
    public void PublishLatestHeight() {
        TransDB txdb = SATObjFactory.GetTxDB();
        try {
            long height = txdb.GetLatestHeight(DataBase.SMARTX_BLOCK_HISTORY);
            GetHeightEvent event = new GetHeightEvent(Message.MESSAGE_STORAGE_LATESTHEIGHT);
            event.message.args.put("height", String.valueOf(height));
            pubsub.publish(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void PubSmartxEvent(Block MC) {
        PubSub.PubMessage(Message.MESSAGE_GET_HEIGHT, "height", String.valueOf(MC.height));
        pubsub.PubMessage(Message.MESSAGE_LASTEST_BLOCK, MC);
        PubSub.PubMessage(Message.MESSAGE_TOTAlBLOCKS, "totalblocks", String.valueOf(SmartxCommands.totalblocks));
        PubSub.PubMessage(Message.MESSAGE_PROCED_BLOCKS, "blocks", String.valueOf(proced_blocks));
    }
    public void run() {
        SmartxEpochTime.Sleep(3000);
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        TransDB txdb = SATObjFactory.GetTxDB();
        AccountDB accdb = SATObjFactory.GetAccDB();
        PublishLatestHeight();
        while (true) {
            try {
                if (curblk == null) {
                    accdb.SetZero();
                    String genesishash = config.getGenesisHash();
                    Block blk = txdb.GetBlock(genesishash, DataBase.SMARTX_BLOCK_HISTORY);
                    if (blk == null) {
                        SmartxEpochTime.Sleep(1000);
                        continue;
                    }
                    blk.height = 1;
                    ApplyTransferGenesis(blk);
                    curblk = blk;
                }
                Block mcBlock = null;
                mcBlock = tvblock.GetNextMC(curblk, DataBase.SMARTX_BLOCK_HISTORY);
                List<String> hashs = CheckBlockRef(mcBlock);
                if (hashs.size() > 0) {
                    BlockDAG blkdag = SATObjFactory.GetBlockDAG();
                    MessageHandle msghandle = SATObjFactory.GetMessageHandle();
                    Channel channel = SATObjFactory.GetMessageHandle().GetNodeBest();
                    if (channel != null) for (int i = 0; i < hashs.size(); i++) {
                        Block blk = msghandle.QueryBolck(channel, hashs.get(i), 5000);
                        blkdag.AddBlock(blk);
                    }
                }
                if (mcBlock != null && hashs.size() == 0) {
                    mcBlock.height = curblk.height + 1;
                    if (ApplyTransfer(mcBlock)) {
                        curblk = mcBlock;
                    }
                    SetPreBlockHeight(mcBlock, (int) mcBlock.height - 1);
                    SetRewards(mcBlock);
                    PubSmartxEvent(mcBlock);
                    GeneralMine.minePowerTotal.computingPower(mcBlock);
                } else {
                    PubSub.PubMessage(Message.MESSAGE_GET_LATESTHEIGHT, "height", String.valueOf(curblk.height));
                    SmartxEpochTime.Sleep(1000);
                }
                OnOutTimeTransfers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void SetRewards(Block mcblock) throws SatException, SQLException {
        AccountDB accdb = SATObjFactory.GetAccDB();
        mcblock.rewards = new BigInteger("1024");
        Account acc = accdb.GetAccount(mcblock.header.address);
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        if (acc == null) acc = accdb.CreateAccount(mcblock.header.address);
        acc.balance = acc.balance.add(blockdag.GetRewards(mcblock.height));
        accdb.SaveAccount(acc);
    }
    static public List<Block> GetAllPreBlock(Block mcBlock) throws SatException, SQLException, SignatureException {
        List<Block> hashs = new ArrayList<Block>();
        TransDB txdb = SATObjFactory.GetTxDB();
        for (int xx = 0; xx < mcBlock.Flds.size(); xx++) {
            Block fldBlk = txdb.GetBlock(mcBlock.Flds.get(xx).hash, txdb.GetDbtype(mcBlock.Flds.get(xx).hash));
            if (fldBlk == null || fldBlk.header.btype == Block.BLKType.SMARTX_TXS) {
                continue;
            }
            hashs.add(fldBlk);
            //
            for (int yy = 0; yy < fldBlk.Flds.size(); yy++) {
                Block blk = txdb.GetBlock(fldBlk.Flds.get(yy).hash, txdb.GetDbtype(fldBlk.Flds.get(yy).hash));
                if (fldBlk == null || fldBlk.header.btype != Block.BLKType.SMARTX_TXS) {
                    continue;
                }
                hashs.add(fldBlk);
            }
        }
        return hashs;
    }
    static public void SetPreBlockHeight(Block mcBlock, int height) throws SatException, SQLException, SignatureException {
        List<Block> hashs = new ArrayList<Block>();
        TransDB txdb = SATObjFactory.GetTxDB();
        for (int xx = 0; xx < mcBlock.Flds.size(); xx++) {
            Block fldBlk = txdb.GetBlock(mcBlock.Flds.get(xx).hash, txdb.GetDbtype(mcBlock.Flds.get(xx).hash));
            if (fldBlk == null || fldBlk.header.btype == Block.BLKType.SMARTX_TXS) {
                continue;
            }
            fldBlk.height = height;
            int dbtype = txdb.GetDbtype(fldBlk);
            txdb.SaveHeight(fldBlk, dbtype);
            for (int yy = 0; yy < fldBlk.Flds.size(); yy++) {
                Block blk = txdb.GetBlock(fldBlk.Flds.get(yy).hash, txdb.GetDbtype(fldBlk.Flds.get(yy).hash));
                if (blk == null || blk.header.btype != Block.BLKType.SMARTX_TXS) {
                    continue;
                }
                blk.height = height;
                dbtype = txdb.GetDbtype(blk);
                txdb.SaveHeight(blk, dbtype);
            }
        }
    }
    static public List<String> CheckBlockRef(Block mcBlock) throws SatException, SQLException {
        List<String> hashs = new ArrayList<String>();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        TransDB txdb = SATObjFactory.GetTxDB();
        if (mcBlock != null) {
            for (int i = 0; i < mcBlock.Flds.size(); i++) {
                Block fldBlk = txdb.GetBlock(mcBlock.Flds.get(i).hash, txdb.GetDbtype(mcBlock.Flds.get(i).hash));
                if (fldBlk == null) {
                    hashs.add(mcBlock.Flds.get(i).hash);
                    continue;
                }
                if (fldBlk.header.btype == Block.BLKType.SMARTX_MAIN || fldBlk.header.btype == Block.BLKType.SMARTX_MAINREF) {
                    for (int j = 0; j < fldBlk.Flds.size(); j++) {
                        Block tmp = txdb.GetBlock(fldBlk.Flds.get(j).hash, txdb.GetDbtype(fldBlk.Flds.get(j).hash));
                        if (tmp == null) {
                            hashs.add(fldBlk.Flds.get(j).hash);
                            continue;
                        }
                    }
                }
            }
        }
        return hashs;
    }
    public boolean ApplyTransferGenesis(Block mcBlock) throws SatException, SQLException, SignatureException {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        List<Block> lists = tvblock.GetBlockRef(mcBlock);
        MerkleTree.sortNumberBlock(lists);
        TransDB txdb = SATObjFactory.GetTxDB();
        AccountDB accdb = SATObjFactory.GetAccDB();
        for (int i = 0; i < lists.size(); i++) {
            Block blk = lists.get(i);
            if (blk.header.btype == Block.BLKType.SMARTX_TXS) {
                if (SmartXWallet.wallettype == SmartXWallet.FAST_WALLETTYPE) {
                    if (!Key25519.verify2(blk.header.hash, blk.sign, blk.header.address)) {
                        log.info(blk.header.hash + "  Transfer sgin verify error!");
                        continue;
                    }
                } else {
                    if (!SmartXWallet.verify(blk.ToSignStringBase58(), blk.sign, blk.header.address)) {
                        log.info(blk.header.hash + "  Transfer sgin verify error!");
                        continue;
                    }
                }
                Field satfield = GetBlockField(blk, Field.FldType.SAT_FIELD_OUT);
                Account account = accdb.GetAccount(satfield.hash);
                if (account == null) {
                    account = accdb.CreateAccount(satfield.hash);
                }
                if (account == null) {
                    log.info(blk.header.hash + "  Transfer account error!");
                    break;
                }
                account.balance = account.balance.add(satfield.amount);
                accdb.SaveAccount(account);
                Account account2 = accdb.GetAccount(satfield.hash);
                if (!account2.balance.equals(account.balance)) {
                    throw new SatException(ErrCode.DB_INSERT_ERROR, "updata account to db error");
                }
            }
        }
        return true;
    }
    public boolean ApplyTransfer(Block mcBlock) throws SatException, SQLException, SignatureException {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        List<Block> lists = tvblock.GetBlockRef(mcBlock);
        proced_blocks = tvblock.GetAllBlocks(mcBlock);
        SmartxCommands.totalblocks += proced_blocks;
        log.info("  apply height:" + mcBlock.height);
        MerkleTree.sortNumberBlock(lists);
        TransDB txdb = SATObjFactory.GetTxDB();
        AccountDB accdb = SATObjFactory.GetAccDB();
        for (int i = 0; i < lists.size(); i++) {
            Block blk = lists.get(i);
            if (blk.header.btype == Block.BLKType.SMARTX_TXS) {
                BigInteger satIn = GetBlockFieldAmount(blk, Field.FldType.SAT_FIELD_IN);
                BigInteger satOut = GetBlockFieldAmount(blk, Field.FldType.SAT_FIELD_OUT);
                if (satIn.compareTo(satOut) != 0) {
                    log.info(blk.header.hash + "Transfer amount not equals!");
                    continue;
                }
                Field fieldIn = GetBlockField(blk, Field.FldType.SAT_FIELD_IN);
                if (!fieldIn.hash.equals(blk.header.address)) {
                    log.info(blk.header.hash + "Transfer hash not equals!");
                    continue;
                }
                if (SmartXWallet.wallettype == SmartXWallet.FAST_WALLETTYPE) {
                    if (!Key25519.verify2(blk.header.hash, blk.sign, blk.header.address)) {
                        log.info(blk.header.hash + "Transfer hash not equals!");
                        continue;
                    }
                } else {
                    if (!SmartXWallet.verify(blk.ToSignStringBase58(), blk.sign, blk.header.address)) {
                        log.error(blk.header.hash + "Transfer sgin verify error!");
                        continue;
                    }
                }
                for (int j = 0; j < blk.Flds.size(); j++) {
                    Field satfield = blk.Flds.get(j);
                    Account account = accdb.GetAccount(satfield.hash);
                    if (account == null) {
                        account = accdb.CreateAccount(satfield.hash);
                    }
                    if (account == null) {
                        log.info(blk.header.hash + "Transfer account error!");
                        break;
                    }
                    if (BigInteger.ZERO.compareTo(satfield.amount) != -1) break;
                    if (satfield.type == Field.FldType.SAT_FIELD_IN && !(account.balance.compareTo(satfield.amount) != -1))
                        break;
                    if (satfield.type == Field.FldType.SAT_FIELD_IN) {
                        account.balance = account.balance.subtract(satfield.amount);
                    }
                    if (satfield.type == Field.FldType.SAT_FIELD_OUT) {
                        account.balance = account.balance.add(satfield.amount);
                    }
                    String mapstring = blk.header.hash + j + satfield.type.toString();
                    Long transferHeight = transfers.get(mapstring);
                    if (transferHeight != null && transferHeight > 0) continue;
                    accdb.SaveAccount(account);
                    Account account2 = accdb.GetAccount(satfield.hash);
                    if (!account2.balance.equals(account.balance)) {
                        throw new SatException(ErrCode.DB_INSERT_ERROR, "updata account to db error");
                    }
                    transfers.put(mapstring, (long) mcBlock.height);
                }
            }
        }
        return true;
    }
    public Field GetBlockField(Block blk, Field.FldType fldType) {
        for (int i = 0; i < blk.Flds.size(); i++) {
            Field f = blk.Flds.get(i);
            if (f.type == fldType) {
                return f;
            }
        }
        return null;
    }
    public BigInteger GetBlockFieldAmount(Block blk, Field.FldType fldType) {
        BigInteger amount = new BigInteger("0");
        for (int i = 0; i < blk.Flds.size(); i++) {
            Field f = blk.Flds.get(i);
            if (f.type == fldType) {
                amount = amount.add(f.amount);
            }
        }
        return amount;
    }
    public void OnOutTimeTransfers() {
        if (curblk == null || transfers.size() < 1000) return;
        Iterator<Map.Entry<String, Long>> it = transfers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> entry = it.next();
            if (entry.getValue() + 100 < curblk.height) {
                it.remove();//使用迭代器的remove()方法删除元素
            }
        }
    }
}
