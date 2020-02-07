package com.smartx.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.mysql.jdbc.CommunicationsException;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.consensus.ErrCode;
import com.smartx.core.consensus.SatException;

public class DataSet {
    private static final Logger logger = Logger.getLogger("DataSet");
    public DataSet(final DBConnection dbconnct) {
        this.dbconnct = dbconnct;
    }
    public ResultSet rs = null;
    public Statement statement = null;
    public DBConnection dbconnct = null;
    public void Close() {
        synchronized (this) {
            try {
                if (this.rs != null) {
                    this.rs.close();
                }
                if (this.statement != null) {
                    this.statement.close();
                }
                this.rs = null;
                this.statement = null;
            } catch (final SQLException e) {
                logger.error("error: " + e);
            }
        }
    }
    public int getRows(final String sql) throws SQLException, SatException {
        logger.debug("ds getrow: [" + sql + "]");
        int rowcount = 0;
        if (this.rs != null) {
            try {
                this.rs.close();
            } catch (final SQLException e) {
                e.printStackTrace();
            }
            this.rs = null;
        }
        try {
            if (this.statement == null) {
                this.statement = this.dbconnct.CreateStatement();
                if (this.statement == null) {
                    throw new SatException(ErrCode.DB_OPEN_ERROR, "create statement error");
                }
            }
            final ResultSet rs = this.statement.executeQuery(sql);
            if (rs.last()) {
                rowcount = rs.getRow();
                rs.beforeFirst();
            }
        } catch (final SQLException e) {
            logger.error("error: " + e);
            throw e;
        }
        return rowcount;
    }
    public boolean IsEnd() {
        synchronized (this) {
            try {
                return !this.rs.next();
            } catch (final SQLException e) {
                return true;
            }
        }
    }
    public boolean Query() {
        synchronized (this) {
            boolean flag = false;
            while (!IsEnd()) {
                flag = true;
                break;
            }
            return flag;
        }
    }
    public boolean exeBatch() {
        try {
            if (this.statement == null) {
                this.statement = this.dbconnct.CreateStatement();
                if (this.statement == null) {
                    return false;
                }
            }
            this.statement.executeBatch();
        } catch (final CommunicationsException e) {
            if (this.statement != null) {
                try {
                    this.statement.close();
                    if (this.rs != null) this.rs.close();
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            }
            this.statement = this.dbconnct.CreateStatement();
            try {
                this.statement.executeBatch();
            } catch (final SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        } catch (final SQLException e) {
            logger.error("error: " + e);
            return false;
        }
        return true;
    }
    public boolean addBatch(final String sql) {
        try {
            if (this.statement == null) {
                this.statement = this.dbconnct.CreateStatement();
                if (this.statement == null) {
                    return false;
                }
            }
            this.statement.addBatch(sql);
        } catch (final CommunicationsException e) {
            if (this.statement != null) {
                try {
                    this.statement.close();
                    if (this.rs != null) this.rs.close();
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                }
            }
            this.statement = this.dbconnct.CreateStatement();
            try {
                this.statement.addBatch(sql);
            } catch (final SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        } catch (final SQLException e) {
            logger.error("error: " + e);
            return false;
        }
        return true;
    }
    public boolean Init(final String sql) {
        logger.debug("ds open: [" + sql + "]");
        synchronized (this) {
            if (this.rs != null) {
                try {
                    this.rs.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
                this.rs = null;
            }
            try {
                if (this.statement == null) {
                    this.statement = this.dbconnct.CreateStatement();
                    if (this.statement == null) {
                        return false;
                    }
                }
                this.rs = this.statement.executeQuery(sql);
            } catch (final CommunicationsException e) {
                if (this.statement != null) {
                    try {
                        this.statement.close();
                        if (this.rs != null) this.rs.close();
                    } catch (final SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                this.statement = this.dbconnct.CreateStatement();
                try {
                    this.rs = this.statement.executeQuery(sql);
                } catch (final SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            } catch (final SQLException e) {
                logger.error("error: " + e);
                return false;
            }
            return true;
        }
    }
    public boolean excAffect(final String sql, final int line) {
        synchronized (this) {
            try {
                int ret = 0;
                logger.debug("ds excute: [" + sql + "]");
                if (this.statement == null) {
                    if (0 == SATObjFactory.GetDbSource().GetDBType()) {
                        this.statement = this.dbconnct.prepareStatement(sql); //mysql
                    } else if (1 == SATObjFactory.GetDbSource().dbtype) {
                        this.statement = this.dbconnct.CreateStatement(); // sqlite
                    }
                    ret = this.statement.executeUpdate(sql);
                } else {
                    ret = this.statement.executeUpdate(sql);
                }
                if (ret != line) {
                    logger.error("error: effect : " + ret);
                    return false;
                }
            } catch (final SQLException e) {
                logger.error("errcode:" + e.getErrorCode());
                e.printStackTrace();
                return false;
            } finally {
                try {
                    this.statement.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
                this.statement = null;
            }
            return true;
        }
    }
    public boolean excute(final String sql) {
        synchronized (this) {
            try {
                logger.debug("ds excute: [" + sql + "]");
                if (this.statement == null) {
                    if (0 == SATObjFactory.GetDbSource().GetDBType()) {
                        this.statement = this.dbconnct.prepareStatement(sql);
                    } else if (1 == SATObjFactory.GetDbSource().GetDBType()) {
                        this.statement = this.dbconnct.CreateStatement();
                    }
                    this.statement.executeUpdate(sql);
                } else {
                    this.statement.executeUpdate(sql);
                }
            } catch (final SQLException e) {
                logger.error("error: " + e);
                return false;
            } finally {
                try {
                    this.statement.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
                this.statement = null;
            }
            return true;
        }
    }
    public boolean Next() {
        synchronized (this) {
            boolean bRet = false;//= true; oracle
            try {
                bRet = this.rs.next();
            } catch (final SQLException e) {
                e.printStackTrace();
            }
            return bRet;
        }
    }
}
