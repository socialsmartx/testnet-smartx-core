package com.smartx.db;
public class DataDB {
    public static DBConnection m_DBConnet = null;
    public static ConnectionPool DBPools = null;
    protected DBConnection GetDBConnection() {
        return m_DBConnet;
    }
}
