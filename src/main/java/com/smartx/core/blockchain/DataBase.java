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
