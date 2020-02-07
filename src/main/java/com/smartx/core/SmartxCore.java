package com.smartx.core;

import java.io.File;
import java.io.FileInputStream;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.smartx.block.Block;
import com.smartx.config.OSInfo;
import com.smartx.config.SystemProperties;
import com.smartx.core.blockchain.BlockDAG;
import com.smartx.core.blockchain.DataBase;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.TraverBlock;
import com.smartx.core.consensus.*;
import com.smartx.core.coordinate.RuleExecutor;
import com.smartx.core.coordinate.RuleThread;
import com.smartx.core.ledger.ConsThread;
import com.smartx.core.syncmanager.SyncThread;
import com.smartx.crypto.HashUtil;
import com.smartx.crypto.Sha256;
import com.smartx.db.AccountDB;
import com.smartx.db.DBConnection;
import com.smartx.db.DataDB;
import com.smartx.db.TransDB;
import com.smartx.message.Dictionary;
import com.smartx.mine.PoolThread;
import com.smartx.util.Tools;
import com.smartx.wallet.SmartXWallet;

import io.github.novacrypto.base58.Base58;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

public class SmartxCore {
    private static final Logger log = Logger.getLogger("Smartx");
    public SystemProperties config = SystemProperties.getDefault();
    private GeneralMine generalMine = new GeneralMine();
    public static boolean G_INSTANCE = false;
    public static SmartXWallet G_Wallet = null;
    public static int role = SystemProperties.SMARTX_ROLE_ORDINARY;
    private static final String myWalletDir;
    static {
        String path = System.getProperty("user.dir");
        if (OSInfo.isWindows()) {
            myWalletDir = path + "\\MyWallet\\";
        } else {
            myWalletDir = path + "/MyWallet/";
        }
    }
    public SmartxCore() {
    }
    public void CheckGensBlock() throws SatException, SQLException {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        String gensis = smartxdb.GetGenesisBlockHash(config.getGenesisHash());
        if (gensis.equals("") && SystemProperties.SMARTX_ROLE_ORDINARY != SmartxCore.role) {
            System.out.println("the genesis block isn't exist");
            throw new SatException(ErrCode.SAT_GENESIS_BLOCK_EXPECT, "the genesis block isn't exist");
        }
    }
    public void ReadConfig() throws SatException, SQLException {
        SmartxCore.role = config.role();
        if (config.getMining()) {
            GeneralMine.g_stop_general_mining = !GeneralMine.g_stop_general_mining;
        }
        // 检查创世块
        CheckGensBlock();
    }
    public static SmartXWallet GetWallet(String address) {
        for (int i = 0; i < DataBase.G_WALLETS.size(); i++) {
            String walletaddress = DataBase.G_WALLETS.get(i).getAddress();
            if (address.equals(walletaddress)) {
                return DataBase.G_WALLETS.get(i);
            }
        }
        return null;
    }
    public static void SetWallet(SmartXWallet wallet) {
        SmartxCore.G_Wallet = wallet;
    }
    public void ReadAccounts() {
        try {
            DataBase.G_WALLETS.clear();
            File file = new File(myWalletDir);
            File[] fs = file.listFiles();
            for (File f : fs) {
                if (f.getName().equals("UTC--2019-09-18T08.json")) continue;
                if (!f.isDirectory() && f.getName().matches(".+\\.json")) {
                    FileInputStream fileInputStream = new FileInputStream(f);
                    byte[] keystoreByte = new byte[512];
                    fileInputStream.read(keystoreByte);
                    String keyStoreString = new String(keystoreByte);
                    JSON keyStoreJson = JSONObject.fromObject(keyStoreString);
                    String address = ((JSONObject) keyStoreJson).getString("address");
                    System.out.println(address);
                    String path = myWalletDir;
                    path += f.getName();
                    SmartXWallet Wallet = SmartXWallet.fromKeyStore("123", path);
                    DataBase.G_WALLETS.add(Wallet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void InitGenesisEpoch() throws SatException, SQLException {
        DataBase.genesisEpoch = Long.parseLong(config.getGenesisEpoch());
        DataBase.genesisDate = config.getGenesisDate();
        AccountDB accdb = SATObjFactory.GetAccDB();
        String path = Tools.GetWalletPath();
        try {
            G_Wallet = SmartXWallet.fromKeyStore("123", path);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SatException(ErrCode.INIT_GENESIS_ERROR, "Init genesis block key error");
        }
    }
    public void InitStorage() throws SatException, SQLException {
        DataBase.G_NAME = config.getNodeName();
        SATObjFactory.GetDbSource().dbtype = config.getDbtype();
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
        }
        DataBase.InitDBTable();
    }
    public void InitSmartxPrc() {
        DataBase.G_Status.SetStatus(SmartxStatus.STATUS.SMARTX_STATUS_INIT);
        if (SystemProperties.SMARTX_ROLE_RULE != SmartxCore.role) {
            SATCoreThread coreThread = new SATCoreThread();
            coreThread.setPriority(Thread.MAX_PRIORITY);
            coreThread.setSatService(generalMine);
            coreThread.setName("Poolmine");
            coreThread.start();
            SyncThread syncThread = new SyncThread();
            Thread thread = new Thread(syncThread, "SyncThread");
            //thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
            PoolThread poolThread = SATObjFactory.GetPoolThread();
            Thread thread2 = new Thread(poolThread, "PoolThread");
            thread2.setPriority(Thread.MAX_PRIORITY);
            thread2.start();
        }
        if (SystemProperties.SMARTX_ROLE_RULE == SmartxCore.role) {
            RuleThread rule = new RuleThread();
            Thread ruleServer = new Thread(rule, "Rulethread");
            ruleServer.setPriority(Thread.MAX_PRIORITY);
            ruleServer.start();
        }
        if (SystemProperties.SMARTX_ROLE_LIGHT != config.getRole()) {
            ConsThread consThread = new ConsThread();
            Thread thread = new Thread(consThread, "ConsThread");
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
        CacheBlock cacheblock = new CacheBlock();
        Thread cacheServer = new Thread(cacheblock, "CacheBlock");
        cacheServer.start();
    }
    public void start() {
        try {
            InitStorage();
            ReadConfig();
            // InitGenesisEpoch();
            InitSmartxPrc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testVerify() throws SatException, SQLException, SignatureException {
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        core.InitGenesisEpoch();
        core.ReadConfig();
        core.ReadAccounts();
        TransDB txdb = SATObjFactory.GetTxDB();
        Block blk = GeneralMine.CreateMainBlock();
        blk.header.random = "aaa";
        blk.header.hash = Sha256.getH256(blk.ToSignString());
        log.info("gen hash:" + blk.header.hash);
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        blk.sign = blockdag.SignBlock(blk.ToSignString(), SmartxCore.G_Wallet);
        System.out.println(blk.ToSignString() + " " + blk.ToSignStringBase58() + " " + blk.sign + " " + blk.header.address);
        boolean result = SmartXWallet.verify(blk.ToSignStringBase58(), blk.sign, blk.header.address);
        System.out.println(result);
        txdb.SaveBlockHeader(blk, DataBase.SMARTX_BLOCK_EPOCH);
        Block tmpblk = txdb.GetBlock(blk.header.hash, DataBase.SMARTX_BLOCK_EPOCH);
        System.out.println(tmpblk.ToSignString() + " " + tmpblk.ToSignStringBase58() + " " + tmpblk.sign + " " + tmpblk.header.address);
        result = SmartXWallet.verify(tmpblk.ToSignStringBase58(), tmpblk.sign, tmpblk.header.address);
        System.out.println(result);
    }
    @Test
    public void testsingle() throws SignatureException, SatException, SQLException {
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        core.InitGenesisEpoch();
        core.ReadConfig();
        core.ReadAccounts();
        RuleExecutor executor = SATObjFactory.GetExecutor();
        TransDB txdb = SATObjFactory.GetTxDB();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        int dbtype = txdb.GetDbtype("a36b410bf95d00fccf7eb88dd2cc372c8de3e457d32fb2e8b1227f7552cbaa8e");
        Block blk = txdb.GetBlock("a36b410bf95d00fccf7eb88dd2cc372c8de3e457d32fb2e8b1227f7552cbaa8e", dbtype);
        System.out.println(Tools.ToJson(blk));
        blk = executor.ruleSignBlock(blk);
        executor.ruleSignBlock(blk);
        txdb.SaveRuleSign(blk, DataBase.SMARTX_BLOCK_EPOCH);
        boolean result = SmartXWallet.verify(blk.ToSignStringBase58(), blk.sign, blk.header.address);
        System.out.println(result);
    }
    @Test
    public void testSign() throws SatException, SQLException, SignatureException {
        String sha3str = Base58.base58Encode(HashUtil.sha3("www".getBytes()));
        System.out.println(sha3str);
        System.out.println(Sha256.getH256("hello world"));
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        core.InitGenesisEpoch();
        core.ReadConfig();
        core.ReadAccounts();
        TransDB txdb = SATObjFactory.GetTxDB();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        Block blk = txdb.GetBlock("299e20c050b3f9c0d572c852978b2a241ce182725a1b38a05bde2e7e7df09ebe", DataBase.SMARTX_BLOCK_HISTORY);
        boolean result = SmartXWallet.verify(blk.ToSignStringBase58(), blk.sign, blk.header.address);
        System.out.println(result);
        do {
            int dbtype = txdb.GetDbtype(blk);
            ArrayList<Block> blks = tvblock.GetBackBlocks(blk, dbtype);
            if (blks == null) break;
            for (int i = 0; i < blks.size(); i++) {
                result = SmartXWallet.verify(blk.ToSignStringBase58(), blk.sign, blk.header.address);
                System.out.println(result);
            }
            blk = blks.get(0);
        } while (true);
    }
}
