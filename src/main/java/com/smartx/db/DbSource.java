package com.smartx.db;
public class DbSource {
    public int dbtype = 1;  // 0 mysql 1 sqlite
    public static String SMARTX_DBNAME = "smartx_db.";
    public static final int SMARTX_SQLITE = 1;
    public static final int SMARTX_MYSQL = 0;
    public static final int SMARTX_LEVELDB = 2;
    public int GetDBType() {
        return dbtype;
    }
    public void SetDBType(int type) {
        dbtype = type;
    }
    public String GetDBName() {
        if (0 == dbtype) {
            return DbSource.SMARTX_DBNAME;
        } else if (1 == dbtype) {
            return "";
        }
        return "";
    }
}
