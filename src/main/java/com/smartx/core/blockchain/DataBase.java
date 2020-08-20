package com.smartx.core.blockchain;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import com.smartx.block.Block;
import com.smartx.core.SmartxCore;
import com.smartx.core.consensus.ErrCode;
import com.smartx.core.consensus.SatException;
import com.smartx.core.consensus.SmartxStatus;
import com.smartx.db.*;
import com.smartx.wallet.SmartXWallet;

public class DataBase {
    public static final int SMARTX_BLOCK_HISTORY = 1;
    public static final int SMARTX_BLOCK_EPOCH = 2;
    public static final int SMARTX_STORAGETYPE_MYSQL = 0;
    public static final int SMARTX_STORAGETYPE_SQLITE = 1;
    public static SmartxStatus G_Status = new SmartxStatus();
    public static List<Block> G_TransactionList = new ArrayList<Block>();
    public static List<SmartXWallet> G_WALLETS = Collections.synchronizedList(new ArrayList<SmartXWallet>());
    public static String G_NAME = "smartx";
    public static long genesisEpoch = 0;
    public static String genesisDate = "";
    public static String password = null;
    public static String rpcclient = null;
    public static String rpcserver = null;
    public static final int BROADCAST = 1;
    public static final int NOBROADCAST = 0;
    public static void InitDBTable() throws SatException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        DbSource dbsrc = SATObjFactory.GetDbSource();
        try {
            String sql = "create table IF NOT EXISTS " + dbsrc.GetDBName();
            sql += "t_account(" + "Faddress varchar(45)," + "Fbalance varchar(20)," + "PRIMARY KEY (`Faddress`)" + ")";
            if (!dt.excute(sql)) throw new SatException(ErrCode.DB_INSERT_ERROR, "create db error:" + sql);
            sql = "create table if not exists " + dbsrc.GetDBName() + "t_order(" + "Fversion varchar(1)," + "Fheadtype varchar(4)," + "Fbtype int," + "ftime varchar(20)," + "Ftimestamp long," + "Fhash varchar(64)," + "Fnum BIGINT," + "Fnonce varchar(32)," + "Faddress varchar(45)," + "Frefhash varchar(64)," + "Fnodename varchar(20)," + "Fepoch BIGINT," + "Fdiff varchar(64)," + "Famount varchar(64)," + "fmodify_time varchar(20)," + "frecv_time varchar(20)," + "Fmerkle_hash varchar(65)," + "FPremerkle_hash varchar(65), Fheight BIGINT, Fecsign varchar(160), Frandom varchar(65), " + "Frulesign varchar(2048), " + "Primary key (`Fhash`)" + ")";
            if (!dt.excute(sql)) throw new SatException(ErrCode.DB_INSERT_ERROR, "create db error: " + sql);
            sql = "create table if not exists " + dbsrc.GetDBName() + "t_reftx(" + "Fmhash varchar(64)," + "Ftxhash varchar(64)," + "ftime datetime" + ")";
            if (!dt.excute(sql)) throw new SatException(ErrCode.DB_INSERT_ERROR, "create db error:" + sql);
            sql = "create table if not exists " + dbsrc.GetDBName() + "t_fields(Finhash varchar(64),Fouthash varchar(64),famount varchar(64), ftime datetime,fnonce " + " varchar(32),PRIMARY KEY(fnonce,Finhash))";
            if (!dt.excute(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "create db error:" + sql);
        } catch (SatException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        } finally {
            dt.Close();
        }
    }
    public static void ShowBlock(String hash) throws SatException, SQLException {
        QueryDB querydb = SATObjFactory.GetQueryDB();
        querydb.ShowBlock(hash);
    }
    @Test
    public void testsqliteThread() throws SatException, SQLException {
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        core.ReadAccounts();
        core.ReadConfig();
        core.InitGenesisEpoch();
        System.out.println("hello world!\n");
    }

    @Test
    public void testleveldb(){
        File file = new File("c:\\satdb");
        LeveldbDatabase db = new LeveldbDatabase(file);

        List<Pair<byte[], byte[]>> batchs = new ArrayList<Pair<byte[], byte[]>>();

        for(int i=1; i<100; i++){
            Pair<byte[], byte[]> item = Pair.of(String.valueOf(i).getBytes(), ("heyi"+i).getBytes());
            batchs.add(item);
        }
        db.updateBatch(batchs);

        System.out.println(new String(db.get("123".getBytes())));

        for(int i=1; i<100; i++){
            String str = new String(db.get(String.valueOf(i).getBytes()));
            System.out.println(str);
        }

        db.close();

        System.out.println("leveldb");
    }
    @Test
    public void testlua(){

        System.out.println("\033[30;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[31;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[32;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[33;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[34;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[35;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[36;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[37;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[40;31;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[41;32;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[42;33;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[43;34;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[44;35;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[45;36;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[46;37;4m" + "Hello,Akina!" + "\033[0m");
        System.out.println("\033[47;4m" + "Hello,Akina!" + "\033[0m");

        System.exit(0);
//        String luaStr = "print 'hello,world!'";
//        Globals globals = JsePlatform.standardGlobals();
//        LuaValue chunk = globals.load(luaStr);
//        chunk.call();
        int iterNum = 10000;

        // *) java 
        long beg = System.currentTimeMillis();
        for ( int j = 0; j < iterNum; j++ ) {
            int a = 0;
            for ( int i = 0; i < 10000; i++ ) {
                a = a + i;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(String.format("Java consume: %dms", end - beg));

        // *) Lua
        String luaStr = "a = 0; for i = 0, 10000, 1 do a = a + i; end";
        Globals globals = JsePlatform.standardGlobals();
        LuaValue chunk = globals.load(luaStr);
        beg = System.currentTimeMillis();
        for ( int i = 0; i < 100; i++ ) {
            chunk.call();
        }
        end = System.currentTimeMillis();
        System.out.println(String.format("Lua consume: %dms", end - beg));
    }

}
