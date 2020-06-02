package com.smartx.db;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.smartx.block.Account;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.consensus.ErrCode;
import com.smartx.core.consensus.SatException;

public class AccountDB extends DataDB {
    private static final Logger logger = Logger.getLogger("AccountDB");
    public synchronized Account CreateAccount(String address) throws SatException, SQLException {
        Account acc = new Account();
        acc.balance = new BigInteger("0");
        acc.address = address;
        SaveAccount(acc);
        return acc;
    }
    public synchronized void SaveAccount(Account acc) throws SatException, SQLException {
        DbSource dbsrc = SATObjFactory.GetDbSource();
        String sql = "select Faddress from " + dbsrc.GetDBName() + "t_account where Faddress ='";
        sql += acc.address;
        sql += "'";
        Connection c = DataDB.DBPools.getConnection();
        DataSet dt = new DataSet(new DBConnection(c));
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
                    logger.error("error sql:" + sql);
                    throw new SatException(ErrCode.DB_INSERT_ERROR, "update amount to db error");
                }
                return;
            }
            dt.Close();
            sql = "insert into " + dbsrc.GetDBName() + "t_account(Faddress, Fbalance)values('";
            sql += acc.address;
            sql += "', ";
            sql += acc.balance;
            sql += ")";
            if (!dt.excAffect(sql, 1)) throw new SatException(ErrCode.DB_INSERT_ERROR, "insert account to db error");
        } catch (Exception e) {
            logger.error(e);
        } finally {
            dt.Close();
            DataDB.DBPools.ReleaseConnection(c);
        }
    }
    public synchronized ArrayList<Account> GetAllAccount() throws SatException, SQLException {
        final DataSet dt = new DataSet(DataDB.m_DBConnet);
        try {
            final DbSource dbsrc = SATObjFactory.GetDbSource();
            final String sql = "select Faddress, Fbalance " + " from " + dbsrc.GetDBName() + "t_account";
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            final ArrayList<Account> accs = new ArrayList<Account>();
            while (!dt.IsEnd()) {
                Account acc = new Account();
                acc.balance = new BigInteger(dt.rs.getString("Fbalance"));
                acc.address = dt.rs.getString("Faddress");
                accs.add(acc);
            }
            return accs;
        } catch (final SatException e) {
            throw e;
        } catch (final SQLException e) {
            throw e;
        }
    }
    public synchronized Account GetAccount(String address) throws SatException, SQLException {
        final DataSet dt = new DataSet(DataDB.m_DBConnet);
        try {
            final DbSource dbsrc = SATObjFactory.GetDbSource();
            String sql = "select Faddress, Fbalance " + " from " + dbsrc.GetDBName() + "t_account where Faddress = '";
            sql += address;
            sql += "'";
            if (!dt.Init(sql)) throw new SatException(ErrCode.DB_OPEN_ERROR, "open db error");
            while (!dt.IsEnd()) {
                Account acc = new Account();
                acc.balance = new BigInteger(dt.rs.getString("Fbalance"));
                acc.address = dt.rs.getString("Faddress");
                return acc;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            dt.Close();
        }
        return null;
    }
    public synchronized void SetZero() throws SatException, SQLException {
        DataSet dt = new DataSet(DataDB.m_DBConnet);
        try {
            DataDB.m_DBConnet.Begin();
            DbSource dbsrc = SATObjFactory.GetDbSource();
            String sql = "update " + dbsrc.GetDBName() + "t_account set Fbalance = 0";
            if (!dt.excute(sql)) throw new SatException(ErrCode.DB_UPDATE_ERROR, "update db error");
            dt.Close();
            DataDB.m_DBConnet.Commit();
        } catch (SatException e) {
            DataDB.m_DBConnet.RollBack();
            throw e;
        } finally {
            dt.Close();
        }
    }
}
