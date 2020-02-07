package com.smartx.core.blockchain;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

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
    public static List<Block> G_FRONTREF = Collections.synchronizedList(new ArrayList<Block>());
    public static List<Block> G_WAITLIST = new ArrayList<Block>();
    public static List<SmartXWallet> G_WALLETS = Collections.synchronizedList(new ArrayList<SmartXWallet>());
    public static String G_NAME = "smartx";
    public static long genesisEpoch = 0;
    public static String genesisDate = "";
    public static String password = null;
    public static String rpcclient = null;
    public static String rpcserver = null;
    public static void InitDBTable() throws SatException, SQLException {
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
            sql = "create table if not exists " + dbsrc.GetDBName() + "t_waitorder(" + "Fversion varchar(1)," + "Fheadtype varchar(4)," + "Fbtype int," + "ftime varchar(20)," + "Ftimestamp long," + "Fhash varchar(64)," + "Fnum BIGINT," + "Fnonce varchar(32)," + "Faddress varchar(45)," + "Frefhash varchar(64)," + "Fnodename varchar(20)," + "Fepoch BIGINT," + "Fdiff varchar(64)," + "Famount varchar(64)," + "fmodify_time varchar(20)," + "frecv_time varchar(20)," + "Fmerkle_hash varchar(65)," + "FPremerkle_hash varchar(65), Fheight BIGINT, Fecsign varchar(160), Frandom varchar(65), " + "Frulesign varchar(2048), Primary key (`Fhash`)" + ")";
            if (!dt.excute(sql)) throw new SatException(ErrCode.DB_INSERT_ERROR, "create db error: " + sql);
            sql = "create table if not exists " + dbsrc.GetDBName() + " t_waitreftx(" + "Fmhash varchar(64)," + "Ftxhash varchar(64)," + "ftime datetime, FEpochtype varchar(3)" + ")";
            if (!dt.excute(sql)) throw new SatException(ErrCode.DB_INSERT_ERROR, "create db error:" + sql);
            sql = "create table if not exists " + dbsrc.GetDBName() + "t_waitfields(Finhash varchar(64),Fouthash varchar(64),famount varchar(64), ftime datetime,fnonce " + " varchar(32), FEpochtype varchar(3) , PRIMARY KEY(fnonce,Finhash))";
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
}
