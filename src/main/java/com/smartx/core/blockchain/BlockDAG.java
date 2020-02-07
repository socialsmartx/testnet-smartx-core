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
import com.smartx.core.coordinate.BlockCache;
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
    static {
        BlockCache cache = SATObjFactory.GetCache();
    }
    public BlockDAG() {
        pubSub.subscribe(this, GetHeightEvent.class);
    }
    public Block GetBlockJson(String queryString) {
        Map<String, String> queryStringInfo = Tools.formData2Dic(queryString);
        String data = Tools.getURLDecoderString(queryStringInfo.get("data"));
        Block blk = Tools.FromJson(data);
        blk.recvtime = Tools.TimeStamp2DateEx((new Date()).getTime());
        log.info("in:" + Tools.getURLDecoderString(queryString));
        return blk;
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
        ArrayList<TxBlock> blocks = new ArrayList<TxBlock>();
        QueryDB querydb = SATObjFactory.GetQueryDB();
        try {
            querydb.GetTxBlocks(address, blocks);
            return blocks;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public Block MakeTransfer(String rawjson) throws SatException, SignatureException {
        BlockMainTop top = SATObjFactory.GetMainTop();
        Block topblk = top.GetTopBlock();
        JSONObject keyStoreJson = JSONObject.fromObject(rawjson);
        long tm = keyStoreJson.getLong("timestamp");
        String amount = keyStoreJson.getString("amount");
        String nonce = keyStoreJson.getString("nonce");
        String from = keyStoreJson.getString("from");
        String to = keyStoreJson.getString("to");
        String sign = keyStoreJson.getString("sign");
        String hash = keyStoreJson.getString("hash");
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
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        byte[] hexEncodedBytes = ByteUtil.hexStringToBytes(sign);
        Key.Signature signature = Key.Signature.fromBytes(hexEncodedBytes);
        blk.sign = Base58.base58Encode(signature.toBytes());
        log.info("------------------------------------------------------------------------------");
        log.info("TXS_create:[" + blk.time + "] " + blk.header.hash);
        return blk;
    }
    public String Transfer(String in, String out, BigInteger amount, SmartXWallet wallet) {
        return null;
    }
    public boolean Transfer(String rawjson) {
        try {
            Block blk = MakeTransfer(rawjson);
            BlockDAG blkdag = SATObjFactory.GetBlockDAG();
            blkdag.AddBlock(blk);
            return blk != null;
        } catch (SatException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }
    public Block ShowBlock(String hash) {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        try {
            return smartxdb.GetBlock(hash, DataBase.SMARTX_BLOCK_HISTORY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Block> GetBlocks(long height, String address) {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        try {
            List<Block> blocks = smartxdb.GetAllHeight(height, DataBase.SMARTX_BLOCK_HISTORY);
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
    void VerifyBlock(Block blk) throws SatException, SignatureException, SQLException {
        assert blk != null;
        try {
            TraverBlock tvblock = SATObjFactory.GetTravBlock();
            long TimeNum = SmartxEpochTime.GetCurTimeNum();
            log.info("local timenum:" + TimeNum + " Block timenum:" + blk.timenum + " hash:" + blk.header.hash);
            log.info("hash:" + blk.header.hash + " sign info:" + blk.ToSignStringBase58() + " sign:" + blk.sign + " address:" + blk.header.address);
            if (!SmartXWallet.verify(blk.ToSignStringBase58(), blk.sign, blk.header.address)) {
                log.error("err:" + Tools.ToJson(blk));
                Tools.show3(blk);
                throw new SatException(ErrCode.SAT_CHECK_SIGN_ERROR, "check block sign error");
            }
            log.info("check ecdsa sign pass");
            if (null == tvblock.GetPrevMCBlock(blk)) {
                log.warn("err:" + Tools.ToJson(blk));
                Tools.show3(blk);
                throw new SatException(ErrCode.SAT_CHECK_PREVMCBLOCK, "check refer the MC block, hadnot rule the block wait, warning!");
            }
            log.info("check MC block pass");
        } catch (SatException e) {
            e.printStackTrace();
            throw e;
        }
    }
    public BigInteger GetRewards(long height) {
        return new BigInteger("10240000");
    }
    public void CheckBlock(Block blk) throws SatException {
        // VerifyBlock(blk);
        if (blk.header.btype != Block.BLKType.SMARTX_TXS && blk.header.btype != Block.BLKType.SMARTX_MAIN && blk.header.btype != Block.BLKType.SMARTX_MAINREF) {
            throw new SatException(ErrCode.SAT_UNKNOWN_BLOCKTYPE, "unknow block type");
        }
    }
    public void CheckAmount(Block blk) throws SatException, SQLException {
        AccountDB accdb = SATObjFactory.GetAccDB();
        Account acc = accdb.GetAccount(blk.header.hash);
        if (acc.balance.compareTo(blk.header.amount) == -1) {
            throw new SatException(ErrCode.SAT_TRANSFER_AMOUNT_ERROR, "the transfer amount more than balance amount");
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
        Block blk = tvblock.GetMCBlock(1584, DataBase.SMARTX_BLOCK_HISTORY);
        System.out.println("hash:" + blk.header.hash);
    }
    public List<Block> DAGChainMerge(List<Block> T_2_allblocks, List<Block> T_1, Block MC) throws SatException, SQLException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        BlockRelation br = SATObjFactory.GetBlockRl();
        ArrayList<Block> t_1_reflist = new ArrayList<Block>();
        ArrayList<Block> t_1_noreflist = new ArrayList<Block>();
        for (int i = 0; i < MC.Flds.size(); i++) {
            String hash = MC.Flds.get(i).hash;
            Block blk = smartxdb.GetBlock(hash, DataBase.SMARTX_BLOCK_EPOCH);
            if (blk.header.btype == Block.BLKType.SMARTX_MAINREF || blk.header.btype == Block.BLKType.SMARTX_MAIN) {
                t_1_reflist.add(blk);
            }
        }
        for (int i = 0; i < T_2_allblocks.size(); i++) {
            if (false == br.IsInArray(T_2_allblocks.get(i).header.hash, t_1_reflist))
                t_1_noreflist.add(T_2_allblocks.get(i));
        }
        for (int i = 0; i < T_1.size(); i++) {
            if (true == br.IsInArray(T_1.get(i).header.hash, t_1_noreflist)) {
                T_1.remove(i);
            }
        }
        return T_1;
    }
    public List<Block> GetWantReferBlocks(Block topblk, Block curblk) throws SatException, SQLException {
        TransDB txdb = SATObjFactory.GetTxDB();
        BlockMainTop top = SATObjFactory.GetMainTop();
        List<Block> blocks = txdb.GetAllMainBlockByNum((int) topblk.timenum, DataBase.SMARTX_BLOCK_EPOCH);
        BlockRelation.AddList(DataBase.G_FRONTREF, blocks);
        BlockRelation.AddList(DataBase.G_FRONTREF, DataBase.G_WAITLIST);
        DataBase.G_WAITLIST.clear();
        long nowheight = top.GetTopBlock().height + 1;
        log.info("do refer nonce:" + curblk.header.nonce + " num:" + curblk.timenum + " time:" + curblk.time + " type:" + curblk.header.btype);
        log.info("front:" + topblk.timenum + " now:" + curblk.timenum + " now height:" + nowheight);
        return DataBase.G_FRONTREF;
    }
    void RefBlockLists(List<Block> lists, Block curblk) throws SatException, SQLException {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        TransDB smartxdb = SATObjFactory.GetTxDB();
        int i = 1;
        Iterator<Block> it = lists.iterator();
        while (it.hasNext()) {
            Block tmpBlock = it.next();
            tvblock.CheckBlockRefer(tmpBlock);
            tmpBlock.height = SATObjFactory.GetMainTop().GetTopBlock().height;
            log.info(" height:" + tmpBlock.height + " front: " + i + " [" + tmpBlock.header.hash + "]" + " type:" + tmpBlock.header.btype);
            curblk = InitRefField(curblk, tmpBlock);
            tmpBlock.blackrefer = curblk.header.hash;
            it.remove();
            BlockStats.TNum--;
            i++;
        }
        lists.clear();
    }
    public void RefEpochBlock(Block topblk, Block MC, Block curblk) throws SatException, SQLException {
        assert (curblk.timenum > topblk.timenum);
        List<Block> lists = GetWantReferBlocks(topblk, curblk);
        BlockRelation.AddBlock(lists, topblk);
        RefBlockLists(lists, curblk);
    }
    public Block RuleSignQuery(long height) throws SatException, SQLException, SignatureException {
        try {
            TraverBlock tvblock = SATObjFactory.GetTravBlock();
            TransDB smartxdb = SATObjFactory.GetTxDB();
            RuleExecutor executor = SATObjFactory.GetExecutor();
            ArrayList<Block> blocks = smartxdb.GetAllMainHeight(height);
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
    public synchronized ArrayList<Block> ReMoveMineTop() throws SatException, SQLException, SignatureException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        BlockMainTop top = SATObjFactory.GetMainTop();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        Block MC = smartxdb.GetLatestMC();
        top.SetMCTopBlock(MC);
        top.SetTopBlock(MC);
        int dbtype = smartxdb.GetDbtype(MC);
        ArrayList<Block> blks_2 = tvblock.SelLinkBlock(smartxdb.GetAllHeight(MC.height, dbtype));
        ArrayList<Block> blks_1 = tvblock.GetBackBlocks(MC, DataBase.SMARTX_BLOCK_EPOCH);
        if (null == blks_1 || blks_1.size() == 0) return blks_2;
        if (blks_1.size() > 0) {
            top.SetTopBlock(blks_1.get(0));
        }
        return blks_1;
    }
    public void CheckHash(Block blk) throws SatException {
        BlockHash bhash = new BlockHash();
        String hash = bhash.getH256(blk);
        if (!hash.equals(blk.header.hash) && !blk.header.hash.equals(config.getGenesisHash())) {
            throw new SatException(ErrCode.SAT_CHECKHASH_ERROR, "check the block's hash error");
        }
    }
    public void AddBlock(Block blk) {
        try {
            if (null == blk) return;
            log.info("addblock:" + Tools.ToJson(blk));
            TransDB txdb = SATObjFactory.GetTxDB();
            TraverBlock tvblock = SATObjFactory.GetTravBlock();
            RuleExecutor executor = SATObjFactory.GetExecutor();
            CheckBlock(blk);
            CheckRandom(blk);
            int ret = CheckBlockRepeat(blk);
            if (0 == ret) {
                VerifySign(blk);
                log.warn("  Good+ block hash:" + blk.header.hash);
            } else if (1073 == ret) {
                if (true == executor.verifyRuleSignBlock(blk)) {
                    log.warn("  Found MC block hash:" + blk.header.hash + " exist, but update");
                    txdb.SaveRuleSign(blk, DataBase.SMARTX_BLOCK_EPOCH);
                } else {
                    log.warn("  block hash:" + blk.header.hash);
                }
            }
            if (blk.header.btype == Block.BLKType.SMARTX_TXS && !txdb.GetBackRefer(blk, txdb.GetDbtype(blk.header.hash)) && blk.blackrefer.equals("")) {
                AddTxBlock(blk);
            }
            if (tvblock.CheckBlockReferEx(blk) || SystemProperties.SMARTX_ROLE_RULE != SmartxCore.role) {
                txdb.SaveBlock(blk, DataBase.SMARTX_BLOCK_EPOCH);
            }
        } catch (SatException | SignatureException | SQLException e) {
            log.error(e);
        }
    }
    public void AddTxBlock(Block blk) throws SatException, SQLException, SignatureException {
        if (null == blk) return;
        log.info("addtxblock:" + Tools.ToJson(blk));
        System.out.println(blk.header.hash);
        VerifySign(blk);
        if (DataBase.G_Status.GetStatus() == SmartxStatus.STATUS.SMARTX_STATUS_MINING) {
            TransDB txdb = SATObjFactory.GetTxDB();
            DataBase.G_WAITLIST.add(blk);
            txdb.SaveBlock(blk, DataBase.SMARTX_BLOCK_EPOCH);
        } else {
            MessageHandle handle = SATObjFactory.GetMessageHandle();
            //handle.BroadCastMBlock();
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

