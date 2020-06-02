package com.smartx.db;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.smartx.block.Account;
import com.smartx.block.Block;
import com.smartx.block.Field;
import com.smartx.cli.SmartxCommands;
import com.smartx.core.SmartxCore;
import com.smartx.core.blockchain.DataBase;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.TraverBlock;
import com.smartx.core.consensus.ErrCode;
import com.smartx.core.consensus.SatException;
import com.smartx.core.consensus.SmartxEpochTime;
import com.smartx.util.SortByChar;
import com.smartx.util.SortTimeForUnit;
import com.smartx.util.Tools;
import com.smartx.util.TxBlock;

public class QueryDB extends DataDB {
    private static final Logger logger = Logger.getLogger("core");
    public String GetHashByNonce(String nonce) throws SatException, SQLException {
        final TransDB txdb = SATObjFactory.GetTxDB();
        String sql = "select fhash as a from t_order where Fnonce = '";
        sql += nonce;
        sql += "'";
        return txdb.GetLine(sql);
    }
    public int GetTxBlocks(String address, List<TxBlock> blocks, long offset) throws SatException, SQLException {
        final DataSet dt = new DataSet(DataDB.m_DBConnet);
        try {
            String sql = "select Finhash, Fouthash, famount, ftime, fnonce from t_fields where finhash = '";
            sql += address;
            sql += "' order by ftime desc limit 10 offset ";
            sql += offset;
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "db open error");
            while (!dt.IsEnd()) {
                final TxBlock unit = new TxBlock();
                unit.amount = new BigInteger(dt.rs.getString("famount"));
                unit.nonce = dt.rs.getString("fnonce");
                unit.hash = GetHashByNonce(unit.nonce);
                unit.timestamp = SmartxEpochTime.StrToStamp(dt.rs.getString("ftime"));
                unit.in = dt.rs.getString("Finhash");
                unit.out = dt.rs.getString("Fouthash");
                unit.type = TxBlock.TxType.TXIN;
                blocks.add(unit);
            }
            dt.Close();
            sql = "select Finhash, Fouthash, famount, ftime, fnonce from t_fields where fouthash = '";
            sql += address;
            sql += "' order by ftime desc limit 10 offset ";
            sql += offset;

            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "db open error");
            while (!dt.IsEnd()) {
                final TxBlock unit = new TxBlock();
                unit.amount = new BigInteger(dt.rs.getString("famount"));
                unit.nonce = dt.rs.getString("fnonce");
                unit.hash = GetHashByNonce(unit.nonce);
                unit.timestamp = SmartxEpochTime.StrToStamp(dt.rs.getString("ftime"));
                unit.in = dt.rs.getString("Finhash");
                unit.out = dt.rs.getString("Fouthash");
                unit.type = TxBlock.TxType.TXOUT;
                blocks.add(unit);
            }
            Collections.sort(blocks, new SortTimeForUnit());
            return 0;
        } catch (final Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            dt.Close();
        }
    }
    public static String ShowBalance(String address) throws SatException, SQLException {
        AccountDB accdb = SATObjFactory.GetAccDB();
        Account acc = accdb.GetAccount(address);
        if (acc != null ) {
            System.out.println(acc.balance);
            return acc.balance.toString();
        }
        return "";
    }

    public static String ShowBalance() throws SatException, SQLException {
        AccountDB accdb = SATObjFactory.GetAccDB();
        ArrayList<Account> accs = accdb.GetAllAccount();
        Collections.sort(accs, new SortByChar());
        String strs = "";
        for (int i = 0; i < accs.size(); i++) {
            String stmp = accs.get(i).address + " " + Double.parseDouble(accs.get(i).balance.toString())/10000;
            System.out.println(stmp);
            strs += accs.get(i).address + " " + accs.get(i).balance + "\n";
        }
        return strs;
    }
    public void ClearCache(int dbtype) {
        synchronized (this) {
            try {
                DbSource dbsrc = SATObjFactory.GetDbSource();
                DataSet dt = new DataSet(DataDB.m_DBConnet);
                DataDB.m_DBConnet.Begin();
                String sql = "delete from " + dbsrc.GetDBName();
                if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_order";
                else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitorder";
                if (!dt.excute(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "delete order error");
                sql = "delete from " + dbsrc.GetDBName();
                if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_reftx";
                else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitreftx";
                if (!dt.excute(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "delete reftx error");
                sql = "delete from " + dbsrc.GetDBName();
                if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_fields";
                else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitfields";
                if (!dt.excute(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "delete fields error");
                sql = "delete from t_account";
                dt.excute(sql);
                DataDB.m_DBConnet.Commit();
            } catch (Exception e) {
                e.printStackTrace();
                DataDB.m_DBConnet.RollBack();
            }
        }
    }
    public void ShowStats() {
        try {
            final TransDB txdb = SATObjFactory.GetTxDB();
            final String sql = "";
            BlockStats.Ntotal = (int) SmartxCommands.totalblocks;
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testAccountCheck() throws SatException, SQLException {
        final SmartxCore core = new SmartxCore();
        core.InitStorage();
        final TransDB txdb = SATObjFactory.GetTxDB();
        String sql = "select fheight as a from (select * from t_order union all select * from t_waitorder) order by fheight desc limit 1";
        BlockStats.TNum = Integer.parseInt(txdb.GetLine(sql));
        System.out.println(BlockStats.TNum);
        sql = "select count(*) as a from (select * from t_order union all select * from t_waitorder)";
        BlockStats.Ntotal = Integer.parseInt(txdb.GetLine(sql));
        System.out.println(BlockStats.Ntotal);
        sql = "select count(*) as a from (select * from t_order union all select * from t_waitorder) where length(Frulesign) > 10";
        BlockStats.MCs = Integer.parseInt(txdb.GetLine(sql));
        System.out.println(BlockStats.MCs);
    }
    public static String ShowBlockEx(String hash) throws SatException, SQLException {
        final TransDB txdb = SATObjFactory.GetTxDB();
        final TraverBlock tvblock = SATObjFactory.GetTravBlock();
        final Block blk = txdb.GetBlock(hash, DataBase.SMARTX_BLOCK_HISTORY);
        if (null == blk) return "";
        final double amount = 0;
        String str = "height:" + blk.height + " hash:" + blk.header.hash + " " + blk.header.btype + " merkle_hash:" + blk.mkl_hash + " amount:" + amount + " time:" + blk.time + " " + blk.nodename + " " + blk.timenum;
        str += "\n";
        if (!blk.blackrefer.equals("")) {
            //str += "    blackrefer:" + blk.blackrefer;
            //str += "\n";
        }
        for (int j = 0; j < blk.Flds.size(); j++) {
            final Field fld = blk.Flds.get(j);
            Block btmp = txdb.GetBlock(fld.hash, DataBase.SMARTX_BLOCK_HISTORY);
            if (btmp != null) {
                btmp = tvblock.TrimBlock(btmp);
                // display the full data of the reference block
                str += "    " + Tools.ToJson(btmp);
                str += "\n";
                for (int i = 0; i < btmp.Flds.size(); i++) {
                    final Block fldblk = txdb.GetBlock(btmp.Flds.get(i).hash, DataBase.SMARTX_BLOCK_HISTORY);
                    if (null != fldblk) {
                        // show the quoted block hash only
                        str += ("        fldhash:" + fldblk.header.hash);
                        str += "\n";
                    }
                }
            } else {
                // If the reference block does not exist, only the hash of the reference block is displayed
                str += ("	missing hash:" + fld.hash + " type:" + fld.type + " " + fld.amount);
                str += "\n";
            }
        }
        return str;
    }
    public static void ShowBlock(String hash) throws SatException, SQLException {
        final TransDB txdb = SATObjFactory.GetTxDB();
        final TraverBlock tvblock = SATObjFactory.GetTravBlock();
        final Block blk = txdb.GetBlock(hash, DataBase.SMARTX_BLOCK_HISTORY);
        if (null == blk) return;
        final double amount = 0;
        System.out.println("height:" + blk.height + " hash:" + blk.header.hash + " " + blk.header.btype + " merkle_hash:" + blk.mkl_hash + " amount:" + amount + " time:" + blk.time + " " + blk.nodename + " " + blk.timenum);
        if (!blk.blackrefer.equals("")) {
            System.out.println("    blackrefer:" + blk.blackrefer);
        }
        for (int j = 0; j < blk.Flds.size(); j++) {
            final Field fld = blk.Flds.get(j);
            Block btmp = txdb.GetBlock(fld.hash, DataBase.SMARTX_BLOCK_HISTORY);
            if (btmp != null) {
                btmp = tvblock.TrimBlock(btmp);
                System.out.println("    " + Tools.ToJson(btmp));
                for (int i = 0; i < btmp.Flds.size(); i++) {
                    final Block fldblk = txdb.GetBlock(btmp.Flds.get(i).hash, DataBase.SMARTX_BLOCK_HISTORY);
                    if (null != fldblk) System.out.println("        fldhash:" + fldblk.header.hash);
                }
            } else {
                System.out.println("	missing hash:" + fld.hash + " type:" + fld.type + " " + fld.amount);
            }
        }
    }
}
