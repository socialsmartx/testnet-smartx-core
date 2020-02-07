package com.smartx.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.smartx.core.blockchain.SATObjFactory;

public class DBConnection {
    protected static final Logger log = Logger.getLogger(DBConnection.class);
    protected String url = null;
    protected String username = null;
    protected String password = null;
    public Connection conn = null;
    public synchronized Statement prepareStatement(final String sql) {
        Statement statement = null;
        try {
            statement = this.conn.prepareStatement(sql);
        } catch (final SQLException e) {
            if (this.conn != null) {
                try {
                    this.conn.close();
                } catch (final SQLException e1) {
                    e1.printStackTrace();
                }
            }
            this.conn = null;
            final DbSource dbsrc = SATObjFactory.GetDbSource();
            if (0 == dbsrc.dbtype) {
                CreateMysql(this.url, this.username, this.password);
            } else if (1 == dbsrc.dbtype) {
                CreateSqlite("satdb");
            }
            CreateOracle(this.url, this.username, this.password);
            try {
                statement = this.conn.prepareStatement(sql);
            } catch (final SQLException e1) {
                e1.printStackTrace();
            }
            return statement;
        }
        return statement;
    }
    public synchronized Statement CreateStatement() {
        Statement statement = null;
        try {
            statement = this.conn.createStatement();
        } catch (final SQLException e) {
            if (this.conn != null) {
                try {
                    this.conn.close();
                } catch (final SQLException e1) {
                    e1.printStackTrace();
                }
            }
            this.conn = null;
            CreateMysql(this.url, this.username, this.password);
            try {
                statement = this.conn.createStatement();
            } catch (final SQLException e1) {
                e1.printStackTrace();
            }
            return statement;
        }
        return statement;
    }
    public void Close() {
        try {
            if (this.conn != null) {
                this.conn.close();
            }
        } catch (final SQLException e) {
            log.error("error: " + e);
        }
    }
    public boolean CreateOracle(final String connectString, final String username, final String password) {
        final String url = connectString;
        this.url = url;
        this.username = username;
        this.password = password;
        try {
            if (null == this.conn) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                this.conn = DriverManager.getConnection(url, username, password);
                if (this.conn.isClosed()) {
                    System.out.println("fail to connecting to the Database!");
                    return false;
                }
            }
        } catch (final ClassNotFoundException cnfex) {
            System.err.println("JDBC/ODBC");
            cnfex.printStackTrace();
            return false;
        } catch (final SQLException e) {
            log.error("error: " + e);
            return false;
        }
        return true;
    }
    public boolean CreateSqlite(final String dbpath) {
        String url = "jdbc:sqlite:";
        url += dbpath;
        try {
            if (null == this.conn) {
                Class.forName("org.sqlite.JDBC");
                this.conn = DriverManager.getConnection(url);
                if (this.conn.isClosed()) {
                    System.out.println("fail to connecting to the Database!");
                    return false;
                }
            }
        } catch (final ClassNotFoundException e) {
            log.error("JDBC/ODBC");
            e.printStackTrace();
            return false;
        } catch (final SQLException e) {
            log.error("error: " + e);
            return false;
        }
        return true;
    }
    public boolean CreateMysql(final String connectString, final String username, final String password) {
        final String url = connectString;
        this.url = url;
        this.username = username;
        this.password = password;
        try {
            if (null == this.conn) {
                log.info("connection to db!");
                Class.forName("com.mysql.jdbc.Driver");
                this.conn = DriverManager.getConnection(url, username, password);
                if (this.conn.isClosed()) {
                    System.out.println("fail to connecting to the Database!");
                    return false;
                }
            }
        } catch (final ClassNotFoundException cnfex) {
            log.error("JDBC/ODBC");
            cnfex.printStackTrace();
            return false;
        } catch (final SQLException e) {
            log.error("error: " + e);
            return false;
        }
        return true;
    }
    public boolean RollBack() {
        try {
            this.conn.rollback();
        } catch (final SQLException e) {
            log.error("error: " + e);
            return false;
        }
        return true;
    }
    public boolean Commit() {
        try {
            this.conn.commit();
        } catch (final SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean Begin() {
        try {
            this.conn.setAutoCommit(false);
            //conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        } catch (final SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
