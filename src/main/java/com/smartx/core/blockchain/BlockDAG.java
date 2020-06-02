package com.smartx.core.blockchain;

import java.math.BigInteger;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.*;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.smartx.block.Account;
import com.smartx.block.Block;
import com.smartx.block.BlockRelation;
import com.smartx.block.Field;
import com.smartx.config.SystemProperties;
import com.smartx.core.SmartxCore;
import com.smartx.core.consensus.*;
import com.smartx.core.coordinate.RuleExecutor;
import com.smartx.core.ledger.GetHeightEvent;
import com.smartx.crypto.HashUtil;
import com.smartx.crypto.Key;
import com.smartx.crypto.Sha256;
import com.smartx.db.AccountDB;
import com.smartx.db.BlockStats;
import com.smartx.db.QueryDB;
import com.smartx.db.TransDB;
import com.smartx.event.PubSub;
import com.smartx.event.PubSubEvent;
import com.smartx.event.PubSubFactory;
import com.smartx.event.PubSubSubscriber;
import com.smartx.message.Message;
import com.smartx.mine.MineHelper;
import com.smartx.net.msg.MessageHandle;
import com.smartx.util.ByteUtil;
import com.smartx.util.Tools;
import com.smartx.util.TxBlock;
import com.smartx.wallet.Key25519;
import com.smartx.wallet.SmartXWallet;

import io.github.novacrypto.base58.Base58;
import net.sf.json.JSONObject;

public class BlockDAG implements IBlockDAG, PubSubSubscriber {
    private static Logger log = Logger.getLogger(BlockDAG.class);
    public SystemProperties config = SystemProperties.getDefault();
    private static final PubSub pubSub = PubSubFactory.getDefault();
    private long height = 1;
    private Block LastestMC = null;
    private long proed_blocks = 0;
    TransDB txdb = SATObjFactory.GetTxDB();
    TraverBlock tvblock = SATObjFactory.GetTravBlock();
    RuleExecutor executor = SATObjFactory.GetExecutor();
    public BlockDAG() {
        pubSub.subscribe(this, GetHeightEvent.class);
    }
    public int CheckBlockRepeat(Block blk) throws SQLException, SatException {
        assert (blk != null);
        if (null == blk) {
            throw new SatException(-1, "error block");
        }
        BlockRelation repeat = new BlockRelation(blk);
        return repeat.IsRepeat();
    }
    public BigInteger GetBalance(String address) {
        AccountDB accdb = SATObjFactory.GetAccDB();
        try {
            Account account = accdb.GetAccount(address);
            if (account != null) return account.balance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BigInteger("0");
    }
    public void onPubSubEvent(PubSubEvent event) {
        String json = event.GetMessage();
        Message message = Message.FromJson(json);
        if (message.args.get("command").equals(Message.MESSAGE_GET_HEIGHT)) {
            height = Long.parseLong(message.args.get("height"));
        } else if (message.args.get("command").equals(Message.MESSAGE_LASTEST_BLOCK)) {
            LastestMC = message.collection.mblock;
        } else if (message.args.get("command").equals(Message.MESSAGE_PROCED_BLOCKS)) {
            proed_blocks = Long.parseLong(message.args.get("blocks"));
        }
    }
    public Block GetMCBlock(long height) {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        try {
            return tvblock.GetMCBlock((int) height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public long GetLatestHeight() {
        return height;
    }
    public Block GetLastestMC() {
        return LastestMC;
    }
    // TODO testnet
    public List<TxBlock> GetBlocks(String address) {
        List<TxBlock> blocks = new ArrayList<TxBlock>();
        QueryDB querydb = SATObjFactory.GetQueryDB();
        try {
            querydb.GetTxBlocks(address, blocks, 0);
            return blocks;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public Block MakeTransfer(String rawjson){
        JSONObject keyStoreJson = JSONObject.fromObject(rawjson);
        long tm = keyStoreJson.getLong("timestamp");
        String amount = keyStoreJson.getString("amount");
        String nonce = keyStoreJson.getString("nonce");
        String from = keyStoreJson.getString("from");
        String to = keyStoreJson.getString("to");
        String sign = keyStoreJson.getString("sign");
        Block blk = new Block();
        blk.header.headtype = 1;
        blk.header.btype = Block.BLKType.SMARTX_TXS;
        blk.header.timestamp = tm;
        blk.time = Tools.TimeStamp2DateEx(tm);
        blk.epoch = SmartxEpochTime.EpochTime(SmartxEpochTime.StrToStamp(blk.time));
        blk.header.address = from;
        blk.timenum = SmartxEpochTime.CalTimeEpochNum(tm);
        blk.header.nonce = nonce;
        blk.header.amount = new BigInteger("0");
        blk.nodename = DataBase.G_NAME;
        blk.diff = "1";
        blk.header.random = "1";
        Field fieldfrom = new Field();
        fieldfrom.amount = new BigInteger(amount);
        fieldfrom.type = Field.FldType.SAT_FIELD_IN;
        fieldfrom.hash = blk.header.address;
        Field fieldto = new Field();
        fieldto.amount = new BigInteger(amount);
        fieldto.type = Field.FldType.SAT_FIELD_OUT;
        fieldto.hash = to;
        blk.Flds.add(fieldfrom);
        blk.Flds.add(fieldto);
        blk.header.hash = Sha256.getH256(blk);
        byte[] hexEncodedBytes = ByteUtil.hexStringToBytes(sign);
        Key.Signature signature = Key.Signature.fromBytes(hexEncodedBytes);
        if (signature != null)
            blk.sign = Base58.base58Encode(signature.toBytes());
        log.info("------------------------------------------------------------------------------");
        log.info("TXS_create:[" + blk.time + "] " + blk.header.hash);
        return blk;
    }
    public Block MakeTransfer(String to, long amount){
        if (null == to || to.equals("")) return null;
        long tm = SmartxEpochTime.get_timestamp();
        Block blk = new Block();
        blk.header.headtype = 1;
        blk.header.btype = Block.BLKType.SMARTX_TXS;
        blk.header.timestamp = tm;
        blk.time = Tools.TimeStamp2DateEx(tm);
        blk.epoch = SmartxEpochTime.EpochTime(SmartxEpochTime.StrToStamp(blk.time));
        assert SmartxCore.G_Wallet != null;
        if (SmartXWallet.wallettype == SmartXWallet.FAST_WALLETTYPE)
            blk.header.address = SmartxCore.G_Wallet.getFastAddress();
        else blk.header.address = SmartxCore.G_Wallet.getAddress();
        blk.timenum = SmartxEpochTime.CalTimeEpochNum(tm);
        blk.header.nonce = Tools.getUUID();
        blk.header.amount = new BigInteger("0");
        blk.nodename = DataBase.G_NAME;
        blk.diff = "1";
        blk.header.random = "1";
        blk.status = DataBase.BROADCAST;
        Field fieldfrom = new Field();
        fieldfrom.amount = BigInteger.valueOf(amount);
        fieldfrom.type = Field.FldType.SAT_FIELD_IN;
        fieldfrom.hash = SmartxCore.G_Wallet.getRealAddress();
        Field fieldto = new Field();
        fieldto.amount = BigInteger.valueOf(amount);
        fieldto.type = Field.FldType.SAT_FIELD_OUT;
        fieldto.hash = to;
        blk.Flds.add(fieldfrom);
        blk.Flds.add(fieldto);
        blk.header.hash = Sha256.getH256(blk);
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        blk.sign = blockdag.SignBlock(blk.header.hash, SmartxCore.G_Wallet);
        //boolean result = Key25519.verify2(blk.ToSignStringBase58(), blk.sign, blk.header.address);
        log.info("------------------------------------------------------------------------------");
        log.info("TXS_create:[" + blk.time + "] " + blk.header.hash);
        return blk;
    }
    public String Transfer(String to, String amount) {
        try {
            long xferamount = Long.parseLong(amount);
            Block blk = MakeTransfer(to, xferamount);
            BlockDAG blkdag = SATObjFactory.GetBlockDAG();
            blkdag.AddBlock(blk);
            return blk.header.hash;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public boolean Transfer(String rawjson) {
        Block blk = MakeTransfer(rawjson);
        AddBlock(blk);
        return blk != null;
    }
    public Block ShowBlock(String hash) {
        return txdb.GetBlock(hash, DataBase.SMARTX_BLOCK_HISTORY);
    }
    public List<Block> GetBlocks(long height, String address) {
        try {
            List<Block> blocks = txdb.GetAllHeight(height, DataBase.SMARTX_BLOCK_HISTORY);
            List<Block> adrsblocks = new ArrayList<Block>();
            if (blocks != null) {
                for (int i = 0; i < blocks.size(); i++) {
                    if (blocks.get(i).header.address.equals(address)) {
                        adrsblocks.add(blocks.get(i));
                    }
                }
                return adrsblocks;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public BigInteger GetRewards(long height) {
        return new BigInteger("10240000");
    }
    public void CheckBlock(Block blk) throws SatException {
        if (blk.header.btype != Block.BLKType.SMARTX_TXS && blk.header.btype != Block.BLKType.SMARTX_MAIN && blk.header.btype != Block.BLKType.SMARTX_MAINREF) {
            throw new SatException(ErrCode.SAT_UNKNOWN_BLOCKTYPE, "unknow block type");
        }
    }
    public void CheckRandom(Block blk) throws SatException {
        if (blk.header.hash.equals(config.getGenesisHash())) {
            return;
        }
        String hash = Sha256.getH256(blk);
        if (!hash.equals(blk.header.hash)) {
            throw new SatException(ErrCode.SAT_CHECKRANDOM_ERROR, "check block hash error");
        }
        String sdiff = MineHelper.DiffLong2String(MineHelper.getHashDiff(hash));
        blk.diff = sdiff;
    }
    public Block InitRefField(Block mblk, Block refedblk) {
        Field field = new Field();
        field.amount = new BigInteger("0");
        field.type = Field.FldType.SAT_FIELD_OUT;
        field.hash = refedblk.header.hash;
        field.time = refedblk.time;
        mblk.Flds.add(field);    // main blk --> refedblock
        return mblk;
    }
    @Test
    public void testGetMCBlock() throws SatException, SQLException, SignatureException {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        Block blk = tvblock.GetMCBlock(1584);
        System.out.println("hash:" + blk.header.hash);
    }
    public void RefBlockLists(Block curblk, List<Block> lists) throws SatException, SQLException {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        int i = 1;
        Iterator<Block> it = lists.iterator();
        while (it.hasNext()) {
            Block tmpBlock = it.next();
            tvblock.CheckBlockRefer(tmpBlock);
            tmpBlock.height = SATObjFactory.GetMainTop().GetMCBlock().height;
            log.info(" height:" + tmpBlock.height + " front: " + i + " [" + tmpBlock.header.hash + "]" + " type:" + tmpBlock.header.btype + " " + tmpBlock.nodename);
            curblk = InitRefField(curblk, tmpBlock);
            //tmpBlock.status = DataBase.REFERED;             // TODO
            it.remove();
            BlockStats.TNum--;
            i++;
        }
        lists.clear();
    }
    public Block RuleSignQuery(long height) throws SatException, SQLException, SignatureException {
        try {
            ArrayList<Block> blocks = txdb.GetAllMainHeight(height);
            Block MC = null;
            for (int i = 0; i < blocks.size(); i++) {
                if (executor.verifyRuleSignBlock(blocks.get(i))) {
                    MC = blocks.get(i);
                    break;
                }
            }
            return MC;
        } catch (Exception e) {
            throw e;
        }
    }
    public void AddBlock(Block blk) {
        try {
            if (null == blk) return;
            log.debug("addblock:" + Tools.ToJson(blk));
            CheckBlock(blk);
            CheckRandom(blk);
            int ret = CheckBlockRepeat(blk);
            if (0 == ret) {
                VerifySign(blk);
                log.warn("  Good+ block hash:" + blk.header.hash);
            } else if (1073 == ret) {
                if (true == executor.verifyRuleSignBlock(blk)) {
                    log.debug("  Found MC block hash:" + blk.header.hash + " exist, but update");
                    txdb.SaveRuleSign(blk, DataBase.SMARTX_BLOCK_HISTORY);
                } else {
                    log.debug("  block hash:" + blk.header.hash);
                }
            }
            if (tvblock.CheckBlockReferEx(blk) || SystemProperties.SMARTX_ROLE_RULE != SmartxCore.role) {
                txdb.SaveBlock(blk, DataBase.SMARTX_BLOCK_HISTORY);
            }
            if (blk.header.btype == Block.BLKType.SMARTX_TXS && !txdb.GetBackRefer(blk, DataBase.SMARTX_BLOCK_HISTORY)
                    && blk.blackrefer.equals("")) {
                AddTxBlock(blk);
            }
        } catch (SatException | SignatureException | SQLException e) {
            log.error(e);
        }
    }
    public void AddTxBlock(Block blk) throws SatException, SQLException, SignatureException {
        if (null == blk) return;
        log.info("addtxblock:" + Tools.ToJson(blk));
        VerifySign(blk);
        if (DataBase.G_Status.GetStatus() == SmartxStatus.STATUS.SMARTX_STATUS_MINING) {
            TransDB txdb = SATObjFactory.GetTxDB();
            DataBase.G_TransactionList.add(blk);
            txdb.SaveBlock(blk, DataBase.SMARTX_BLOCK_HISTORY);
        } else if (SystemProperties.SMARTX_ROLE_RULE != SmartxCore.role && blk.status == DataBase.BROADCAST){
            MessageHandle handle = SATObjFactory.GetMessageHandle();
            try {
                txdb.SaveBlock(blk, DataBase.SMARTX_BLOCK_HISTORY);
                blk.status = DataBase.NOBROADCAST;
                handle.BroadCastMBlock(blk);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void VerifySign(Block blk) throws SatException, SignatureException {
        if (SmartXWallet.wallettype == SmartXWallet.FAST_WALLETTYPE) {
            if (!Key25519.verify2(blk.header.hash, blk.sign, blk.header.address)) {
                log.error("err:" + Tools.ToJson(blk));
                throw new SatException(ErrCode.SAT_CHECK_SIGN_ERROR, "check block sign error");
            }
        } else {
            if (!SmartXWallet.verify(blk.ToSignStringBase58(), blk.sign, blk.header.address)) {
                log.error("err:" + Tools.ToJson(blk));
                throw new SatException(ErrCode.SAT_CHECK_SIGN_ERROR, "check block sign error");
            }
        }
    }
    public String SignBlock(String message, SmartXWallet wallet) {
        if (SmartXWallet.wallettype == SmartXWallet.FAST_WALLETTYPE) {
            String base58RawSig = wallet.signfast(message);
            return base58RawSig;
        } else {
            byte[] rawhash = HashUtil.sha3(message.getBytes());
            String base58RawSig = wallet.sign(rawhash);
            return base58RawSig;
        }
    }
}

