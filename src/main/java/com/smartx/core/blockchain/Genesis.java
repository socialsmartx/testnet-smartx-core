package com.smartx.core.blockchain;

import java.math.BigInteger;
import java.security.SignatureException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.smartx.SmartXCli;
import com.smartx.block.Block;
import com.smartx.block.Field;
import com.smartx.config.SystemProperties;
import com.smartx.core.SmartxCore;
import com.smartx.core.consensus.*;
import com.smartx.core.coordinate.RuleExecutor;
import com.smartx.core.syncmanager.MerkleTree;
import com.smartx.crypto.ECKey;
import com.smartx.crypto.HashUtil;
import com.smartx.crypto.Sha256;
import com.smartx.db.DBConnection;
import com.smartx.db.DataDB;
import com.smartx.db.QueryDB;
import com.smartx.db.TransDB;
import com.smartx.message.Dictionary;
import com.smartx.util.Tools;
import com.smartx.wallet.Key25519;
import com.smartx.wallet.SmartXWallet;

import io.github.novacrypto.base58.Base58;

public class Genesis {
    private static Logger log = Logger.getLogger(Genesis.class);
    public SystemProperties config = SystemProperties.getDefault();
    private long height = 1;
    public Block CreateGenesis() throws SignatureException {
        final Block blk = new Block();
        blk.header.headtype = 1;
        blk.header.btype = Block.BLKType.SMARTX_MAIN;
        blk.header.timestamp = SmartxEpochTime.get_timestamp();
        blk.header.address = SmartxCore.G_Wallet.getAddress();
        blk.timenum = SmartxEpochTime.CalTimeEpochNum(blk.header.timestamp);
        blk.header.nonce = SmartxEpochTime.getUUID();
        blk.epoch = SmartxEpochTime.EpochTime(blk.header.timestamp);
        blk.time = SmartxEpochTime.GetSystime();
        System.out.println("genesis address:" + SmartxCore.G_Wallet.getAddress());
        final String message = blk.ToSignString();
        final byte[] rawhash = HashUtil.sha3(message.getBytes());
        final String base58RawHash = Base58.base58Encode(rawhash);
        final String base58RawSig = SmartxCore.G_Wallet.sign(rawhash);
        blk.sign = base58RawSig;
        System.out.println("base58RawHash: " + Base58.base58Encode(rawhash));
        System.out.println("base58RawSig: " + base58RawSig + " sigsize:" + base58RawSig.length());
        final ECKey ecKey = SmartxCore.G_Wallet.getECKey();
        //use public key to verify is the private key sign the message
        System.out.println("pubkey:" + ecKey.getAddress());
        final boolean result = SmartXWallet.verify(base58RawHash, base58RawSig, blk.header.address);
        System.out.println("verify result is " + result);
        blk.header.hash = Sha256.getH256(blk.ToSignString());
        return blk;
    }
    public Block CreateFastGenesis() throws SignatureException {
        final GeneralMine mine = new GeneralMine();
        final BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        final Block blk = new Block();
        blk.header.headtype = 1;
        blk.header.btype = Block.BLKType.SMARTX_MAIN;
        blk.header.timestamp = SmartxEpochTime.get_timestamp();
        blk.header.address = SmartxCore.G_Wallet.baseKey.toAddressString();
        blk.timenum = SmartxEpochTime.CalTimeEpochNum(blk.header.timestamp);
        blk.header.nonce = SmartxEpochTime.getUUID();
        blk.epoch = SmartxEpochTime.EpochTime(blk.header.timestamp);
        blk.time = SmartxEpochTime.GetSystime();
        blk.header.random = "1";
        blk.diff = "1";
        blk.mkl_hash = MerkleTree.ComputeRoot(blk);
        System.out.println("genesis address:" + SmartxCore.G_Wallet.baseKey.toAddressString());
        mine.SignBlock(blk);
        blk.header.hash = Sha256.getH256(blk);
        blk.sign = blockdag.SignBlock(blk.header.hash, SmartxCore.G_Wallet);
        return blk;
    }
    public static Block InitGensisTxBlock(final BigInteger amount, final BigInteger fee) {
        // set txs header
        final long tm = System.currentTimeMillis();
        final Block blk = new Block();
        blk.header.btype = Block.BLKType.SMARTX_TXS;
        blk.timenum = SmartxEpochTime.CalTimeEpochNum(tm);
        blk.header.headtype = 1;
        blk.header.timestamp = SmartxEpochTime.get_timestamp();
        blk.time = Tools.TimeStamp2DateEx(tm);
        blk.epoch = SmartxEpochTime.EpochTime(blk.header.timestamp);
        System.out.println("tm:" + blk.header.timestamp + " epoch:" + blk.epoch);
        blk.header.nonce = Tools.getUUID();
        blk.header.hash = "";
        blk.header.address = SmartxCore.G_Wallet.getRealAddress();
        // set txs content IN
        final Field in = new Field();
        in.amount = amount;
        in.fee = fee;
        in.type = Field.FldType.SAT_FIELD_IN;
        in.hash = "0000000000";
        blk.Flds.add(in);
        // OUT
        final Field out = new Field();
        out.amount = amount;
        out.fee = fee;
        out.type = Field.FldType.SAT_FIELD_OUT;
        out.hash = "5e13651fcfa007e8e6c67fa4106ba09ab6010956";
        blk.Flds.add(out);
        final String tostr = blk.ToSignString();
        log.debug(tostr);
        final String str = Sha256.getH256(tostr);
        blk.header.hash = str;
        return blk;
    }
    public Block GetFastRuleGenesis(Block blk) throws Exception {
        RuleExecutor executor = SATObjFactory.GetExecutor();
        blk = executor.ruleSignBlock(blk);
        blk.height = ++this.height;
        if (executor.verifyRuleSignBlock(blk)) log.info(" rulesign is ok");
        return blk;
    }
    public void ReadGenesisBlock() throws Exception {
        DataDB.m_DBConnet = new DBConnection();
        final BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        final SmartxCore core = new SmartxCore();
        core.InitStorage();
        core.ReadConfig();
        core.InitGenesisEpoch();
        core.ReadAccounts();
        final int dbtype = SATObjFactory.GetDbSource().GetDBType();
        if (1 == dbtype) {
            if (!DataDB.m_DBConnet.CreateSqlite("smartx.db")) {
                log.error("connection to database error!");
                System.exit(0);
            }
            DataBase.InitDBTable();
        }
        final TransDB txdb = SATObjFactory.GetTxDB();
        final Block mblk = txdb.GetBlock("299e20c050b3f9c0d572c852978b2a241ce182725a1b38a05bde2e7e7df09ebe", DataBase.SMARTX_BLOCK_HISTORY);
        final Block txblk = txdb.GetBlock("a36b410bf95d00fccf7eb88dd2cc372c8de3e457d32fb2e8b1227f7552cbaa8e", DataBase.SMARTX_BLOCK_HISTORY);
        final String message = mblk.ToSignStringBase58();
        final String signstr = blockdag.SignBlock(txblk.ToSignStringBase58(), SmartxCore.G_Wallet);
        final boolean txresult = SmartXWallet.verify(txblk.ToSignStringBase58(), signstr, mblk.header.address);
        boolean result = SmartXWallet.verify(message, mblk.sign, mblk.header.address);
        System.out.println("verify result is " + result);
        result = SmartXWallet.verify(txblk.ToSignStringBase58(), txblk.sign, txblk.header.address);
        System.out.println("verify result is " + result);
    }
    @Test
    public void GenSignForJson() throws SatException, SQLException, SignatureException {
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        core.ReadConfig();
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        TransDB txdb = SATObjFactory.GetTxDB();
        Block MC = txdb.GetBlock("299e20c050b3f9c0d572c852978b2a241ce182725a1b38a05bde2e7e7df09ebe", DataBase.SMARTX_BLOCK_HISTORY);
        Block block = txdb.GetBlock("a36b410bf95d00fccf7eb88dd2cc372c8de3e457d32fb2e8b1227f7552cbaa8e", DataBase.SMARTX_BLOCK_HISTORY);
        blockdag.VerifySign(MC);
        blockdag.VerifySign(block);
    }
    @Test
    public void ReadToJson() throws Exception {
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        core.ReadConfig();
        TransDB txdb = SATObjFactory.GetTxDB();
        Block MC = txdb.GetBlock("299e20c050b3f9c0d572c852978b2a241ce182725a1b38a05bde2e7e7df09ebe", DataBase.SMARTX_BLOCK_HISTORY);
        Block block = txdb.GetBlock("a36b410bf95d00fccf7eb88dd2cc372c8de3e457d32fb2e8b1227f7552cbaa8e", DataBase.SMARTX_BLOCK_HISTORY);
        String mcjson = Tools.ToJson(MC);
        String blockjson = Tools.ToJson(block);
        Tools.WriteFile("c:\\mc.json", mcjson);
        Tools.WriteFile("c:\\block.json", blockjson);
    }
    @Test
    public void WriteFromJson() throws Exception {
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        QueryDB querydb = SATObjFactory.GetQueryDB();
        querydb.ClearCache(DataBase.SMARTX_BLOCK_HISTORY);
        TransDB txdb = SATObjFactory.GetTxDB();
        String MCjson = Tools.ReadFile("c:\\mc.json");
        String blockjson = Tools.ReadFile("c:\\block.json");
        Block MC = Tools.FromJson(MCjson);
        Block block = Tools.FromJson(blockjson);
        txdb.SaveBlock(MC, DataBase.SMARTX_BLOCK_HISTORY);
        txdb.SaveBlock(block, DataBase.SMARTX_BLOCK_HISTORY);
        log.info("succed");
    }
    @Test
    public void TestFastCreateGenesis() throws Exception {
        SmartxCore core = new SmartxCore();
        SmartXCli cli = new SmartXCli();
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        DataBase.genesisEpoch = Long.parseLong(core.config.getGenesisEpoch());
        DataBase.genesisDate = core.config.getGenesisDate();
        System.out.println("epoch:" + DataBase.genesisEpoch);
        int flag = 1;
        DataDB.m_DBConnet = new DBConnection();
        int dbtype = 0;
        SATObjFactory.GetDbSource().SetDBType(0);
        if (1 == dbtype) {
            if (!DataDB.m_DBConnet.CreateSqlite("smartx.db")) {
                log.error("connection to database error!");
                System.exit(0);
            }
        } else if (0 == dbtype) {
            Dictionary dict = SystemProperties.getDefault().getMysqlConf();
            if (!DataDB.m_DBConnet.CreateMysql(dict.args.get("connection"), dict.args.get("username"), dict.args.get("password"))) {
                log.error("connection to database error!");
                System.exit(0);
            }
        }
        DataBase.InitDBTable();
        DataBase.password = "123";
        Key25519 key = cli.loadAndUnlockWallet();
        TransDB txdb = SATObjFactory.GetTxDB();
        SmartxCore.G_Wallet = new SmartXWallet();
        SmartxCore.G_Wallet.baseKey = key.getAccount(0);
        if (flag == 0) {
            Block blk = CreateFastGenesis();
            BigInteger amount = new BigInteger("100000000000000");
            BigInteger fee = new BigInteger("0");
            Block txblk = InitGensisTxBlock(amount, fee);
            txblk.sign = blockdag.SignBlock(txblk.ToSignString(), SmartxCore.G_Wallet);
            txdb.SaveBlock(txblk, DataBase.SMARTX_BLOCK_HISTORY);
            blk = BlockMainTop.InitRefField(blk, txblk);
            txdb.SaveBlock(blk, DataBase.SMARTX_BLOCK_HISTORY);
        } else if (flag == 1) {
            int type = txdb.GetDbtype("af6a89cbec692e476544ac8adb9b34e6b355442fac4cc93867805053ffc7560d");
            Block blk = txdb.GetBlock("af6a89cbec692e476544ac8adb9b34e6b355442fac4cc93867805053ffc7560d", type);
            GetFastRuleGenesis(blk);
            txdb.SaveRuleSign(blk, DataBase.SMARTX_BLOCK_HISTORY);
        }
    }
    @Test
    public void TestCreateGenesis() throws Exception {
        SmartxCore core = new SmartxCore();
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        //core.InitStorage();
        core.ReadConfig();
        DataBase.genesisEpoch = Long.parseLong(core.config.getGenesisEpoch());
        DataBase.genesisDate = core.config.getGenesisDate();
        System.out.println("epoch:" + DataBase.genesisEpoch);
        DataDB.m_DBConnet = new DBConnection();
        int dbtype = SATObjFactory.GetDbSource().GetDBType();
        if (0 == dbtype) {
            Dictionary dict = SystemProperties.getDefault().getMysqlConf();
            if (!DataDB.m_DBConnet.CreateMysql(dict.args.get("connection"), dict.args.get("username"), dict.args.get("password"))) {
                log.error("connection to database error!");
                System.exit(0);
            }
        } else if (1 == dbtype) {
            if (!DataDB.m_DBConnet.CreateSqlite("smartx.db")) {
                log.error("connection to database error!");
                System.exit(0);
            }
            DataBase.InitDBTable();
        }
        String path = Tools.GetWalletPath();
        try {
            SmartxCore.G_Wallet = SmartXWallet.fromKeyStore("123", path);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SatException(ErrCode.INIT_GENESIS_ERROR, "Init genesis block key error");
        }
        TransDB txdb = SATObjFactory.GetTxDB();
        Block blk = CreateGenesis();
        BigInteger amount = new BigInteger("100000000000000");
        BigInteger fee = new BigInteger("0");
        Block txblk = InitGensisTxBlock(amount, fee);
        txblk.sign = blockdag.SignBlock(txblk.ToSignString(), SmartxCore.G_Wallet);
        txdb.SaveBlock(txblk, DataBase.SMARTX_BLOCK_HISTORY);
        blk = BlockMainTop.InitRefField(blk, txblk);
        txdb.SaveBlock(blk, DataBase.SMARTX_BLOCK_HISTORY);
    }
}
