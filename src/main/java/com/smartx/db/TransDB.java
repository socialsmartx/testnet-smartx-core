package com.smartx.db;

import java.math.BigInteger;
import java.security.SignatureException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.block.Account;
import com.smartx.block.Block;
import com.smartx.block.Field;
import com.smartx.block.FieldItem;
import com.smartx.config.SystemProperties;
import com.smartx.core.blockchain.DataBase;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.TraverBlock;
import com.smartx.core.consensus.ErrCode;
import com.smartx.core.consensus.SatException;
import com.smartx.core.coordinate.RuleExecutor;
import com.smartx.util.Tools;

public class TransDB extends DataDB {
    private static final Logger log = Logger.getLogger("core");
    public Block GetBlock(String hash, int dbtype) {
        if (hash.equals("")) return null;
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        Block blk = null;
        DbSource dbsrc = SATObjFactory.GetDbSource();
        String sql = "";
        sql += "select Fversion, Fheadtype,Fbtype,Ftimestamp,Fhash,";
        sql += "Fnum,Fnonce,Faddress,Frefhash,Fnodename,Fepoch,Fdiff, Ftime, Frecv_time, Fmerkle_hash,FPremerkle_hash, Fecsign, Frandom, Frulesign, Fheight from " + dbsrc.GetDBName();
        if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_order where";
        else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitorder where";
        sql += " fhash ='";
        sql += hash;
        sql += "'";
        try {
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "query db error");
            while (!dt.IsEnd()) {
                blk = new Block();
                blk.header.headtype = Integer.parseInt(dt.rs.getString("Fheadtype"));
                blk.header.btype = Block.BLKType.values()[dt.rs.getInt("Fbtype")];
                blk.header.timestamp = Long.parseLong(dt.rs.getString("Ftimestamp"));
                blk.time = dt.rs.getString("Ftime");
                blk.header.hash = hash;
                blk.timenum = dt.rs.getInt("Fnum");
                blk.header.nonce = dt.rs.getString("Fnonce");
                blk.header.address = dt.rs.getString("Faddress");
                blk.blackrefer = dt.rs.getString("Frefhash");
                blk.nodename = dt.rs.getString("Fnodename");
                blk.epoch = dt.rs.getInt("Fepoch");
                blk.diff = dt.rs.getString("Fdiff");
                blk.mkl_hash = dt.rs.getString("Fmerkle_hash");
                blk.premkl_hash = dt.rs.getString("FPremerkle_hash");
                blk.sign = dt.rs.getString("Fecsign");
                blk.header.random = dt.rs.getString("Frandom");
                String rulesign = dt.rs.getString("Frulesign");
                blk.ruleSigns = Tools.GetRuleSignListByJson(rulesign);
                blk.height = dt.rs.getInt("Fheight");
                blk.blackrefer = dt.rs.getString("Frefhash");
                break;
            }
            dt.Close();
            if (null == blk) return blk;
            if (blk.header.btype == Block.BLKType.SMARTX_MAIN || blk.header.btype == Block.BLKType.SMARTX_MAINREF) {
                ArrayList<String> referhashs = GetReferHashs(blk, dbtype);
                for (int i = 0; i < referhashs.size(); i++) {
                    Field fd = new Field();
                    fd.type = Field.FldType.SAT_FIELD_OUT;
                    fd.amount = new BigInteger("0");
                    fd.hash = referhashs.get(i).split("\\|")[0];
                    fd.time = referhashs.get(i).split("\\|")[1];
                    blk.Flds.add(fd);
                }
            } else if (blk.header.btype == Block.BLKType.SMARTX_TXS) {
                FieldItem fdb = new FieldItem();
                GetFields(blk.header.nonce, fdb, dbtype, blk);
                Field in = new Field();
                in.amount = fdb.amount;
                in.fee = new BigInteger("0");
                in.type = Field.FldType.SAT_FIELD_IN;
                in.hash = fdb.inhash;
                blk.Flds.add(in);
                // OUT
                Field out = new Field();
                out.amount = fdb.amount;
                out.fee = new BigInteger("0");
                out.type = Field.FldType.SAT_FIELD_OUT;
                out.hash = fdb.outhash;
                blk.Flds.add(out);
            }
            return blk;
        } catch (SatException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            dt.Close();
        }
    }
    public String BlockHeaderSql(Block blk, int dbtype) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DbSource dbsrc = SATObjFactory.GetDbSource();
        String sql = "insert into " + dbsrc.GetDBName();
        if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_order(Fversion, Fheadtype, Fbtype,Ftimestamp,Fhash,";
        else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH)
            sql += "t_waitorder(Fversion, Fheadtype, Fbtype,Ftimestamp,Fhash,";
        sql += "Fnum,Fnonce,Faddress,Frefhash,Fnodename,Fepoch,Fdiff, Famount, " + "Ftime, fmodify_time, frecv_time, ";
        sql += "Fmerkle_hash, FPremerkle_hash, Fecsign, Frandom,";
        sql += "Frulesign, Fheight)values('1','";
        sql += blk.header.headtype;
        sql += "',";
        sql += blk.header.btype.ordinal() + "," + blk.header.timestamp + ",'" + blk.header.hash + "'," + blk.timenum;
        sql += ",'" + blk.header.nonce + "','" + blk.header.address + "','" + blk.blackrefer + "','";
        sql += blk.nodename + "'," + blk.epoch;
        sql += ",'" + blk.diff + "', " + blk.header.amount + ", '" + blk.time + "', '" + df.format(new Date());
        sql += "','" + blk.recvtime + "','" + blk.mkl_hash + "','" + blk.premkl_hash + "','" + blk.sign;
        sql += "','" + blk.header.random + "','";
        String rulesign = Tools.ToRuleSignListByList(blk.ruleSigns);
        sql += rulesign + "'," + blk.height + ")";
        return sql;
    }
    public String BackReferSql(String hash, String hashref, int dbtype) {
        DbSource dbsrc = SATObjFactory.GetDbSource();
        String sql = "update " + dbsrc.GetDBName();
        if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_order ";
        else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitorder ";
        sql += " set Frefhash = '";
        sql += hash;
        sql += "' where fhash='";
        sql += hashref;
        sql += "'";
        return sql;
    }
    public String ReferBlockSql(Block blk, String hashrefed, int dbtype) {
        DbSource dbsrc = SATObjFactory.GetDbSource();
        String sql = "insert into " + dbsrc.GetDBName();
        if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_reftx(Fmhash, ftxhash, ftime";
        else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitreftx(Fmhash, ftxhash, ftime";
        sql += ")values('";
        sql += blk.header.hash;
        sql += "', '";
        sql += hashrefed;
        sql += "', '";
        sql += Tools.TimeStamp2DateEx(System.currentTimeMillis());
        sql += "')";
        return sql;
    }
    public String FieldSql(Block blk, String inhash, String outhash, BigInteger amount, int dbtype) {
        DbSource dbsrc = SATObjFactory.GetDbSource();
        String sql = "insert into " + dbsrc.GetDBName();
        if (dbtype == DataBase.SMARTX_BLOCK_HISTORY)
            sql += "t_fields(finhash, fouthash, fnonce, famount, ftime)values('";
        else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH)
            sql += "t_waitfields(finhash, fouthash, fnonce, famount, ftime)values('";
        sql += inhash;
        sql += "', '";
        sql += outhash;
        sql += "', '";
        sql += blk.header.nonce;
        sql += "', '";
        sql += amount;
        sql += "', '";
        sql += blk.time;
        sql += "')";
        return sql;
    }
    public void SaveMainBlock(Block blk, int dbtype) throws SatException, SQLException {
        SaveBlockHeader(blk, dbtype);
        for (int i = 0; i < blk.Flds.size(); i++) {
            SaveBackRefer(blk.Flds.get(i).hash, blk.header.hash, DataBase.SMARTX_BLOCK_HISTORY);
        }
        for (int i = 0; i < blk.Flds.size(); i++) {
            if (blk.Flds.get(i).type != Field.FldType.SAT_FIELD_OUT)
                throw new SatException(ErrCode.SAT_BLOCKREF_ERROR, "mc refence type error");
            String referhash = blk.Flds.get(i).hash;
            SaveBlockReferTx(blk, referhash, blk.Flds.get(i).time, dbtype);
        }
    }
    public void SaveTxBlock(Block blk, int dbtype) throws SatException, SQLException {
        if (!Field.IsValid(blk)) return;
        if (1 == SaveBlockHeader(blk, dbtype)) {
            log.warn("the block header is exist!:" + blk.header.hash);
        }
        String inhash = "";
        String outhash = "";
        BigInteger amount = new BigInteger("0");
        String nonce = blk.header.nonce;
        for (int i = 0; i < blk.Flds.size(); i++) {
            if (blk.Flds.get(i).type == Field.FldType.SAT_FIELD_IN) {
                inhash = blk.Flds.get(i).hash;
                amount = blk.Flds.get(i).amount;
            } else if (blk.Flds.get(i).type == Field.FldType.SAT_FIELD_OUT) {
                outhash = blk.Flds.get(i).hash;
            }
        }
        SaveField(blk, inhash, outhash, amount, dbtype);
    }
    public boolean GetRefExist(DataSet dt, int dbtype, String mhash, String hashrefed) throws SatException {
        dt.Close();
        String sql = "select fmhash from ";
        if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_reftx where fmhash='";
        else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitreftx where fmhash='";
        sql += mhash;
        sql += "' and ftxhash='";
        sql += hashrefed;
        sql += "'";
        if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
        return dt.Query();
    }
    public synchronized boolean SetTransaction(String in, BigInteger inbal, String out, BigInteger outbal) {
        DbSource dbsrc = SATObjFactory.GetDbSource();
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        try {
            DataDB.m_DBConnet.Begin();
            String sql1 = "select Faddress from " + dbsrc.GetDBName() + "t_account where Faddress ='";
            sql1 += in;
            sql1 += "'";
            if (!dt.Init(sql1)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            if (!dt.Query()) {
                // not exist error
                throw new SatException(ErrCode.SAT_TRANSFER_IN_ERROR, "in account isn't exist");
            } else {
                Account acc = new Account();
                acc.balance = inbal;
                acc.address = in;
                UpdateAccount(acc);
            }
            dt.Close();
            String sql2 = "select Faddress from " + dbsrc.GetDBName() + "t_account where Faddress ='";
            sql2 += out;
            sql2 += "'";
            if (!dt.Init(sql2)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            if (!dt.Query()) {
                // no exist insert
                Account acc = new Account();
                acc.balance = outbal;
                acc.address = out;
                InsertAccount(acc);
            } else {
                Account acc = new Account();
                acc.balance = outbal;
                acc.address = out;
                UpdateAccount(acc);
            }
            DataDB.m_DBConnet.Commit();
        } catch (Exception e) {
            log.error(e);
            DataDB.m_DBConnet.RollBack();
            return false;
        } finally {
            dt.Close();
        }
        return true;
    }
    private void InsertAccount(Account acc) throws SatException {
        DbSource dbsrc = SATObjFactory.GetDbSource();
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        String sql = "insert into " + dbsrc.GetDBName() + "t_account(Faddress, Fbalance)values('";
        sql += acc.address;
        sql += "', ";
        sql += acc.balance;
        sql += ")";
        if (!dt.excAffect(sql, 1)) throw new SatException(ErrCode.DB_INSERT_ERROR, "insert account to db error");
        dt.Close();
    }
    private void UpdateAccount(Account acc) throws SatException {
        DbSource dbsrc = SATObjFactory.GetDbSource();
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        String sql = "update " + dbsrc.GetDBName() + "t_account set Fbalance=";
        sql += acc.balance;
        sql += " where Faddress = '";
        sql += acc.address;
        sql += "'";
        if (!dt.excAffect(sql, 1)) {
            log.error("error sql:" + sql);
            throw new SatException(ErrCode.DB_UPDATE_ERROR, "update amount to db error");
        }
        dt.Close();
    }
    public int SaveMainBlock_mysql(Block blk, int dbtype) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DbSource dbsrc = SATObjFactory.GetDbSource();
            DataDB.m_DBConnet.Begin();
            DataSet dt = new DataSet(DataDB.m_DBConnet);
            DataSet dttrnas = new DataSet(DataDB.m_DBConnet);
            String sql = "select Fhash from " + dbsrc.GetDBName();
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_order where fhash ='";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitorder where fhash ='";
            sql += blk.header.hash;
            sql += "' for update";
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            if (dt.Query()) {
               for (int i = 0; i < blk.Flds.size(); i++) {
                    if (blk.Flds.get(i).type != Field.FldType.SAT_FIELD_OUT)
                        throw new SatException(ErrCode.SAT_BLOCKREF_ERROR, "mc refence type error");
                    String referhashed = blk.Flds.get(i).hash;
                    if (!GetRefExist(dt, dbtype, blk.header.hash, referhashed)) {
                        sql = ReferBlockSql(blk, referhashed, dbtype);
                        dttrnas.addBatch(sql);
                    }
                }
                if (false == dttrnas.exeBatch()) throw new SQLException();
                DataDB.m_DBConnet.Commit();
                return 0;
                //throw new SatException(ErrCode.SAT_TXBLOCK_EXIST, "the txblock is exist");
            }
            sql = BlockHeaderSql(blk, dbtype);
            dttrnas.addBatch(sql);
            for (int i = 0; i < blk.Flds.size(); i++) {
                // save back references
                //int type = GetDbtype(blk.Flds.get(i).hash);
                sql = BackReferSql(blk.Flds.get(i).hash, blk.header.hash, DataBase.SMARTX_BLOCK_HISTORY);
                dttrnas.addBatch(sql);
            }
            dt.Close();
            for (int i = 0; i < blk.Flds.size(); i++) {
                if (blk.Flds.get(i).type != Field.FldType.SAT_FIELD_OUT)
                    throw new SatException(ErrCode.SAT_BLOCKREF_ERROR, "mc refence type error");
                String referhashed = blk.Flds.get(i).hash;
                if (!GetRefExist(dt, dbtype, blk.header.hash, referhashed)) {
                    sql = ReferBlockSql(blk, referhashed, dbtype);
                    dttrnas.addBatch(sql);
                }
            }
            if (false == dttrnas.exeBatch()) throw new SQLException();
            DataDB.m_DBConnet.Commit();
        } catch (Exception e) {
            //log.error("db rollback block: " + blk.header.hash);
            DataDB.m_DBConnet.RollBack();
        }
        return 0;
    }
    public void SaveTxBlock_mysql(Block blk, int dbtype) {
        if (!Field.IsValid(blk)) return;
        try {
            DataDB.m_DBConnet.Begin();
            DbSource dbsrc = SATObjFactory.GetDbSource();
            DataSet dt = new DataSet(DataDB.m_DBConnet);
            String sql = "select Fhash from " + dbsrc.GetDBName();
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_order where fhash ='";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitorder where fhash ='";
            sql += blk.header.hash;
            sql += "' for update";
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            if (dt.Query()) throw new SatException(ErrCode.SAT_TXBLOCK_EXIST, "the txblock is exist");
            sql = BlockHeaderSql(blk, dbtype);
            DataSet dttrans = new DataSet(DataDB.m_DBConnet);
            dttrans.addBatch(sql);
            String inhash = "";
            String outhash = "";
            BigInteger amount = new BigInteger("0");
            String nonce = blk.header.nonce;
            for (int i = 0; i < blk.Flds.size(); i++) {
                if (blk.Flds.get(i).type == Field.FldType.SAT_FIELD_IN) {
                    inhash = blk.Flds.get(i).hash;
                    amount = blk.Flds.get(i).amount;
                } else if (blk.Flds.get(i).type == Field.FldType.SAT_FIELD_OUT) {
                    outhash = blk.Flds.get(i).hash;
                }
            }
            // need to query if t_field exists
            dt.Close();
            sql = "select fnonce from ";
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_fields where fnonce='";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitfields where fnonce='";
            sql += blk.header.nonce;
            sql += "' for update";
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            if (!dt.Query()) {
                sql = FieldSql(blk, inhash, outhash, amount, dbtype);
                dttrans.addBatch(sql);
            }
            log.debug("sql2:" + sql);
            if (false == dttrans.exeBatch()) throw new SQLException();
            DataDB.m_DBConnet.Commit();
        } catch (Exception e) {
            //log.error("db rollback block: " + blk.header.hash);
            DataDB.m_DBConnet.RollBack();
        }
    }
    public synchronized void SaveAccount(Account acc) {
        DbSource dbsrc = SATObjFactory.GetDbSource();
        String sql = "select Faddress from " + dbsrc.GetDBName() + "t_account where Faddress ='";
        sql += acc.address;
        sql += "'";
        DataDB.m_DBConnet.Begin();
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        try {
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            if (dt.Query()) {
                dt.Close();
                sql = "update " + dbsrc.GetDBName() + "t_account set Fbalance=";
                sql += acc.balance;
                sql += " where Faddress = '";
                sql += acc.address;
                sql += "'";
                if (!dt.excAffect(sql, 1)) {
                    log.error("error sql:" + sql);
                    throw new SatException(ErrCode.DB_INSERT_ERROR, "update amount to db error");
                }
                DataDB.m_DBConnet.Commit();
                return;
            }
            dt.Close();
            sql = "insert into " + dbsrc.GetDBName() + "t_account(Faddress, Fbalance)values('";
            sql += acc.address;
            sql += "', ";
            sql += acc.balance;
            sql += ")";
            if (!dt.excAffect(sql, 1)) throw new SatException(ErrCode.DB_INSERT_ERROR, "insert account to db error");
            DataDB.m_DBConnet.Commit();
        } catch (Exception e) {
            log.error(e);
            DataDB.m_DBConnet.RollBack();
        } finally {
            //dt.Close();
        }
    }
    public synchronized void SaveBlock(Block blk, int dbtype) throws SatException, SQLException {
        int storagetype = SystemProperties.getDefault().getDbtype();
        if (storagetype == DataBase.SMARTX_STORAGETYPE_MYSQL) {
            if (blk.header.btype == Block.BLKType.SMARTX_MAIN || blk.header.btype == Block.BLKType.SMARTX_MAINREF) {
                SaveMainBlock_mysql(blk, dbtype);
            } else if (blk.header.btype == Block.BLKType.SMARTX_TXS) {
                SaveTxBlock_mysql(blk, dbtype);
            }
        } else if (storagetype == DataBase.SMARTX_STORAGETYPE_SQLITE) {
            if (blk.header.btype == Block.BLKType.SMARTX_MAIN || blk.header.btype == Block.BLKType.SMARTX_MAINREF) {
                SaveMainBlock(blk, dbtype);
            } else if (blk.header.btype == Block.BLKType.SMARTX_TXS) {
                SaveTxBlock(blk, dbtype);
            }
        }
    }
    public int SaveBlockHeader(Block blk, int dbtype) throws SatException {
        assert (!blk.header.hash.equals(""));
        //synchronized (this) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DbSource dbsrc = SATObjFactory.GetDbSource();
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        String sql = "select Fhash from " + dbsrc.GetDBName();
        if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_order where fhash ='";
        else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitorder where fhash ='";
        sql += blk.header.hash;
        sql += "'";
        try {
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            if (dt.Query()) return 1;
            dt.Close();
            blk.recvtime = Tools.TimeStamp2DateEx((new Date()).getTime());
            sql = BlockHeaderSql(blk, dbtype);
            if (!dt.excAffect(sql, 1)) {
                log.error(" error sql:" + sql);
                throw new SatException(ErrCode.DB_INSERT_ERROR, "insert block to t_order error");
            }
        } catch (SatException e) {
            e.printStackTrace();
            throw e;
        } finally {
            dt.Close();
        }
        //}
        return 0;
    }
    public int SaveBlockReferTx(Block blk, String refhash, String time, int dbtype) throws SatException {
        //synchronized (this) {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        DbSource dbsrc = SATObjFactory.GetDbSource();
        String sql = "select Fmhash from " + dbsrc.GetDBName();
        if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_reftx where Fmhash ='";
        else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitreftx where Fmhash ='";
        sql += blk.header.hash;
        sql += "' and ftxhash = '";
        sql += refhash;
        sql += "'";
        try {
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            if (dt.Query()) return 1;
            dt.Close();
            sql = "insert into " + dbsrc.GetDBName();
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_reftx(Fmhash, ftxhash, ftime";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitreftx(Fmhash, ftxhash, ftime";
            sql += ")values('";
            sql += blk.header.hash;
            sql += "', '";
            sql += refhash;
            sql += "', '";
            sql += Tools.TimeStamp2DateEx(System.currentTimeMillis());
            sql += "')";
            if (!dt.excAffect(sql, 1)) {
                log.error("error sql:" + sql);
                throw new SatException(ErrCode.DB_INSERT_ERROR, "insert block to t_reftx error");
            }
        } catch (SatException e) {
            throw e;
        } finally {
            dt.Close();
        }
        //}
        return 0;
    }
    public int SaveField(Block blk, String inhash, String outhash, BigInteger amount, int dbtype) throws SatException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        DbSource dbsrc = SATObjFactory.GetDbSource();
        String sql = "select fnonce from " + dbsrc.GetDBName();
        if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_fields where fnonce ='";
        else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitfields where fnonce ='";
        sql += blk.header.nonce;
        sql += "'";
        try {
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            if (dt.Query()) return 1;
            dt.Close();
            sql = "insert into " + dbsrc.GetDBName();
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY)
                sql += "t_fields(finhash, fouthash, fnonce, famount, ftime)values('";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH)
                sql += "t_waitfields(finhash, fouthash, fnonce, famount, ftime)values('";
            sql += inhash;
            sql += "', '";
            sql += outhash;
            sql += "', '";
            sql += blk.header.nonce;
            sql += "', ";
            sql += amount;
            sql += ", '";
            sql += blk.time;
            sql += "')";
            if (!dt.excAffect(sql, 1)) throw new SatException(ErrCode.DB_INSERT_ERROR, "insert block to t_field error");
        } catch (SatException e) {
            throw e;
        } finally {
            dt.Close();
        }
        return 0;
    }
    public void GetFields(String nonce, FieldItem fdb, int dbtype, Block blk) throws SatException, SQLException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        DbSource dbsrc = SATObjFactory.GetDbSource();
        String sql = "select Finhash, Fouthash, famount, ftime, fnonce from " + dbsrc.GetDBName();
        if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_fields where fnonce='";
        else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitfields where fnonce='";
        sql += nonce;
        sql += "'";
        try {
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "query db error");
            while (!dt.IsEnd()) {
                fdb.inhash = dt.rs.getString("Finhash");
                fdb.outhash = dt.rs.getString("Fouthash");
                fdb.amount = new BigInteger(dt.rs.getString("famount"));
                fdb.time = dt.rs.getString("ftime");
                fdb.nonce = dt.rs.getString("fnonce");
                break;
            }
        } catch (SatException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } finally {
            dt.Close();
        }
    }
    public ArrayList<String> GetReferHashs(Block blk, int dbtype) throws SatException, SQLException {
        assert (!blk.header.hash.equals(""));
        DbSource dbsrc = SATObjFactory.GetDbSource();
        ArrayList<String> arrs = new ArrayList<String>();
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        String sql = "select Ftxhash, ftime from " + dbsrc.GetDBName();
        if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_reftx where Fmhash ='";
        else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitreftx where Fmhash ='";
        sql += blk.header.hash;
        sql += "'";
        try {
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            while (!dt.IsEnd()) {
                String str = dt.rs.getString("ftxhash");
                String time = dt.rs.getString("ftime");
                str += "|";
                str += time;
                arrs.add(str);
            }
        } catch (SatException e) {
            throw e;
        } finally {
            dt.Close();
        }
        return arrs;
    }
    public synchronized ArrayList<Block> GetAllMainHeight(long height) throws SatException, SQLException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        ArrayList<String> hashs = new ArrayList<String>();
        ArrayList<Block> blks = new ArrayList<Block>();
        hashs.clear();
        blks.clear();
        try {
            DbSource dbsrc = SATObjFactory.GetDbSource();
            String sql = "";
            if (dbsrc.GetDBType() == 0) {
                sql += "select fhash from smartx_db.t_order";
            } else {
                sql += "select * from (select * from t_order union all select * from t_waitorder ) ";
            }
            sql += " where fheight=" + height;
            sql += " and fbtype in (1,2)";
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            while (!dt.IsEnd()) hashs.add(dt.rs.getString("Fhash"));
            dt.Close();
            for (int i = 0; i < hashs.size(); i++) {
                Block blk = GetBlock(hashs.get(i), DataBase.SMARTX_BLOCK_HISTORY);
                blks.add(blk);
            }
            return blks;
        } catch (SatException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } finally {
            dt.Close();
        }
    }
    public ArrayList<Block> GetAllHeight(long height, int dbtype) throws SatException, SQLException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        ArrayList<String> hashs = new ArrayList<String>();
        ArrayList<Block> blks = new ArrayList<Block>();
        hashs.clear();
        blks.clear();
        try {
            DbSource dbsrc = SATObjFactory.GetDbSource();
            String sql = "select Fhash from " + dbsrc.GetDBName();
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_order where";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitorder where";
            sql += " fheight =";
            sql += height;
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            while (!dt.IsEnd()) hashs.add(dt.rs.getString("Fhash"));
            dt.Close();
            for (int i = 0; i < hashs.size(); i++) {
                Block blk = GetBlock(hashs.get(i), dbtype);
                if (null != blk) blks.add(blk);
            }
            return blks;
        } catch (SatException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } finally {
            dt.Close();
        }
    }
    public int GetDbtype(Block blk) throws SatException, SQLException {
        return GetDbtype(blk.header.hash);
    }
    public int GetDbtype(String hash) throws SatException, SQLException {
        DbSource dbsrc = SATObjFactory.GetDbSource();
        String sql = "select count(*) a from " + dbsrc.GetDBName() + "t_order where fhash='";
        sql += hash;
        sql += "'";
        if (0 == Integer.parseInt(GetLine(sql))) return DataBase.SMARTX_BLOCK_HISTORY;
        return DataBase.SMARTX_BLOCK_HISTORY;
    }
    public synchronized void SaveRuleSign(Block blk, int dbtype) throws SatException {
        if (null == blk) return;
        DataDB.m_DBConnet.Begin();
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        try {
            Block tmpblk = GetBlock(blk.header.hash, DataBase.SMARTX_BLOCK_HISTORY);
            if (tmpblk != null && tmpblk.ruleSigns != null && tmpblk.ruleSigns.size() > 0) return;
            DbSource dbsrc = SATObjFactory.GetDbSource();
            TransDB txdb = SATObjFactory.GetTxDB();
            String rulesign = Tools.ToRuleSignListByList(blk.ruleSigns);
            String sql = "update " + dbsrc.GetDBName();
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += " t_order ";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += " t_waitorder ";
            sql += " set Frulesign = '" + rulesign;
            sql += "', Fheight =" + blk.height;
            sql += " where Fhash='";
            sql += blk.header.hash;
            sql += "'";
            if (!dt.excAffect(sql, 1)) throw new SatException(ErrCode.DB_INSERT_ERROR, "insert db error");
            DataDB.m_DBConnet.Commit();
        } catch (Exception e) {
            e.printStackTrace();
            DataDB.m_DBConnet.RollBack();
        } finally {
            dt.Close();
        }
    }
    public void SaveHeight(Block blk, int dbtype) throws SatException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        try {
            DataDB.m_DBConnet.Begin();
            DbSource dbsrc = SATObjFactory.GetDbSource();
            String sql = "update " + dbsrc.GetDBName();
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += " t_order set Fheight = ";
            else sql += " t_waitorder set Fheight = ";
            sql += blk.height;
            sql += " where fhash='";
            sql += blk.header.hash;
            sql += "'";
            if (!dt.excute(sql)) throw new SatException(ErrCode.DB_INSERT_ERROR, "update height error");
            DataDB.m_DBConnet.Commit();
        } catch (Exception e) {
            e.printStackTrace();
            DataDB.m_DBConnet.RollBack();
            throw e;
        } finally {
            dt.Close();
        }
    }
    public boolean GetBackRefer(Block refblock, int dbtype) throws SatException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        DbSource dbsrc = SATObjFactory.GetDbSource();
        String sql = "select Fmhash from " + dbsrc.GetDBName();
        if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_reftx where Ftxhash ='";
        else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitreftx where Ftxhash ='";
        sql += refblock.header.hash;
        sql += "'";
        try {
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            if (dt.Query()) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public void SaveBackRefer(String hashref, String hash, int dbtype) throws SatException, SQLException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        try {
            DbSource dbsrc = SATObjFactory.GetDbSource();
            String sql = "update " + dbsrc.GetDBName();
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_order ";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitorder ";
            sql += " set Frefhash = '";
            sql += hash;
            sql += "' where fhash='";
            sql += hashref;
            sql += "'";
            if (!dt.excute(sql)) throw new SatException(ErrCode.DB_INSERT_ERROR, "update height error");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            dt.Close();
        }
    }
    public synchronized List<Block> GetAllMainBlockByNum(int num, int dbtype) throws SatException, SQLException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        ArrayList<Block> blks = new ArrayList<Block>();
        try {
            DbSource dbsrc = SATObjFactory.GetDbSource();
            String sql = "select fhash, ftime, fnonce, fnum, fbtype, fdiff, fnodename, Frecv_time, Fmerkle_hash, Frulesign, Fheight from " + dbsrc.GetDBName();
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_order ";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitorder ";
            sql += "where fbtype in (1,2) and fnum=" + num + " order by ftime desc";
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            while (!dt.IsEnd()) {
                Block blk = new Block();
                blk.header.hash = dt.rs.getString("fhash");
                blk.time = dt.rs.getString("ftime");
                blk.timenum = dt.rs.getLong("fnum");
                blk.header.nonce = dt.rs.getString("fnonce");
                blk.diff = dt.rs.getString("fdiff");
                blk.nodename = dt.rs.getString("fnodename");
                blk.header.btype = Block.BLKType.values()[dt.rs.getInt("Fbtype")];
                blk.recvtime = dt.rs.getString("Frecv_time");
                blk.mkl_hash = dt.rs.getString("Fmerkle_hash");
                String rulesign = dt.rs.getString("Frulesign");
                blk.ruleSigns = Tools.GetRuleSignListByJson(rulesign);
                blk.height = dt.rs.getInt("Fheight");
                ArrayList<String> refhashs = GetReferHashs(blk, DataBase.SMARTX_BLOCK_HISTORY);
                for (int i = 0; i < refhashs.size(); i++) {
                    Field field = new Field();
                    field.amount = new BigInteger("0");
                    field.type = Field.FldType.SAT_FIELD_OUT;
                    field.hash = refhashs.get(i).split("\\|")[0];
                    //field.time = refedblk.time;
                    blk.Flds.add(field);
                }
                blks.add(blk);
            }
        } catch (SatException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } finally {
            dt.Close();
        }
        return blks;
    }
    public synchronized void GetAllMainBlockByHeight(List<Block> blks, int height, int dbtype) throws SatException, SQLException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        synchronized (this) {
            try {
                DbSource dbsrc = SATObjFactory.GetDbSource();
                String sql = "select fhash, ftime, fnonce, fnum, fbtype, fdiff, fnodename, Frecv_time, Fmerkle_hash, Frulesign, " + "Fheight, faddress, Ftimestamp, Frandom, Fecsign, Frefhash from " + dbsrc.GetDBName();
                if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_order ";
                else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitorder ";
                sql += "where fheight=" + height + " and fbtype in (1,2)";
                if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
                while (!dt.IsEnd()) {
                    Block blk = new Block();
                    blk.header.btype = Block.BLKType.values()[dt.rs.getInt("Fbtype")];
                    blk.header.timestamp = Long.parseLong(dt.rs.getString("Ftimestamp"));
                    blk.header.address = dt.rs.getString("Faddress");
                    blk.header.nonce = dt.rs.getString("fnonce");
                    blk.header.random = dt.rs.getString("Frandom");
                    blk.header.hash = dt.rs.getString("fhash");
                    blk.sign = dt.rs.getString("Fecsign");
                    blk.blackrefer = dt.rs.getString("Frefhash");
                    blk.time = dt.rs.getString("ftime");
                    blk.timenum = dt.rs.getLong("fnum");
                    blk.diff = dt.rs.getString("fdiff");
                    blk.nodename = dt.rs.getString("fnodename");
                    blk.recvtime = dt.rs.getString("Frecv_time");
                    blk.mkl_hash = dt.rs.getString("Fmerkle_hash");
                    String rulesign = dt.rs.getString("Frulesign");
                    blk.ruleSigns = Tools.GetRuleSignListByJson(rulesign);
                    blk.height = dt.rs.getInt("Fheight");
                    ArrayList<String> refhashs = GetReferHashs(blk, dbtype);
                    for (int i = 0; i < refhashs.size(); i++) {
                        Field field = new Field();
                        field.amount = new BigInteger("0");
                        field.type = Field.FldType.SAT_FIELD_OUT;
                        field.hash = refhashs.get(i).split("\\|")[0];
                        //field.time = refedblk.time;
                        blk.Flds.add(field);
                    }
                    blks.add(blk);
                }
            } catch (SatException e) {
                throw e;
            } catch (SQLException e) {
                throw e;
            } finally {
                dt.Close();
            }
        }
    }
    public List<Block> GetBlockHashBack(String hash, int dbtype) throws SatException, SQLException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        List<Block> mblocks = new ArrayList<Block>();
        try {
            String sql = "select fmhash from ";
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_reftx where ftxhash = '";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitreftx where ftxhash = '";
            sql += hash;
            sql += "'";
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open database init");
            while (!dt.IsEnd()) {
                String mhash = dt.rs.getString("fmhash");
                Block tmpBlk = GetBlock(mhash, dbtype);
                if (null != tmpBlk) mblocks.add(tmpBlk);
            }
            return mblocks;
        } catch (SatException e) {
            e.printStackTrace();
            throw e;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            dt.Close();
        }
    }
    public Block GetNextRealMC(Block MC, int dbtype) throws SatException, SQLException, SignatureException {
        RuleExecutor executor = SATObjFactory.GetExecutor();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        ArrayList<String> hashs = new ArrayList<String>();
        try {
            String sql = "select fmhash from ";
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_reftx where ftxhash = '";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitreftx where ftxhash = '";
            sql += MC.header.hash;
            sql += "'";
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open database init");
            while (!dt.IsEnd()) hashs.add(dt.rs.getString("fmhash"));
            for (int i = 0; i < hashs.size(); i++) {
                Block blk = GetBlock(hashs.get(i), dbtype);
                if (blk != null && executor.verifyRuleSignBlock(blk)) {
                    if (!tvblock.CheckBlockReferEx(blk)) continue;
                    return blk;
                }
            }
            return null;
        } catch (SatException e) {
            e.printStackTrace();
            throw e;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            dt.Close();
        }
    }
    public Block GetNextMC(Block MC, int dbtype) {
        RuleExecutor executor = SATObjFactory.GetExecutor();
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        ArrayList<String> hashs = new ArrayList<String>();
        try {
            String sql = "select fmhash from ";
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY) sql += "t_reftx where ftxhash = '";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH) sql += "t_waitreftx where ftxhash = '";
            sql += MC.header.hash;
            sql += "'";
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open database init");
            while (!dt.IsEnd()) hashs.add(dt.rs.getString("fmhash"));
            for (int i = 0; i < hashs.size(); i++) {
                Block blk = GetBlock(hashs.get(i), dbtype);
                if (blk != null && executor.verifyRuleSignBlock(blk)) {
                    return blk;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            dt.Close();
        }
    }
    public String GetGenesisBlockHash(String genesishash) throws SatException, SQLException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        String hash = "";
        try {
            String sql = "select fhash from t_order where fhash = '";
            sql += genesishash;
            sql += "'";
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open database error");
            if (dt.Query()) hash = dt.rs.getString("fhash");
        } catch (SatException e) {
            e.printStackTrace();
            throw e;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dt.Close();
        }
        return hash;
    }
    public int IsRepeat(Block blk){
        TransDB smartxdb = SATObjFactory.GetTxDB();
        Block tmpblk = null;
        tmpblk = smartxdb.GetBlock(blk.header.hash, DataBase.SMARTX_BLOCK_HISTORY);
        if (null != tmpblk) return 1073;
        else return 0;
    }
    public Block GetLatestMC() throws SignatureException {
        DbSource dbsrc = SATObjFactory.GetDbSource();
        RuleExecutor executor = SATObjFactory.GetExecutor();
        String sql = "select fhash as a from " + dbsrc.GetDBName();
        sql += "t_order where length(Frulesign)>10 order by Fheight desc limit 1";

        String hash = GetLine(sql);
        Block MC = GetBlock(hash, DataBase.SMARTX_BLOCK_HISTORY);
        if (MC == null || false == executor.verifyRuleSignBlock(MC)) return null;
        return MC;
    }
    public int GetLatestHeight(int dbtype) throws SatException, SQLException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        try {
            DbSource dbsrc = SATObjFactory.GetDbSource();
            String sql = "select Fheight, Frulesign from " + dbsrc.GetDBName();
            if (dbtype == DataBase.SMARTX_BLOCK_HISTORY)
                sql += "t_order where fbtype in (1,2) and length(Frulesign)>10 order by Fheight desc limit 1";
            else if (dbtype == DataBase.SMARTX_BLOCK_EPOCH)
                sql += "t_waitorder where fbtype in (1,2) and length(Frulesign)>10 order by Fheight desc limit 1";
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            int length = 0;
            while (!dt.IsEnd()) {
                length = dt.rs.getInt("Fheight");
            }
            return length;
        } catch (SatException e) {
            throw e;
        } finally {
            dt.Close();
        }
    }
    public String GetLine(String sql){
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        try {
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "");
            while (!dt.IsEnd()) return dt.rs.getString("a");
        } catch (Exception e) {
            log.error("error:" + e);
        } finally {
            dt.Close();
        }
        return "0";
    }
}
