package com.smartx.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {
    protected List<Connection> connections = new ArrayList<Connection>();
    protected int size = 1;
    protected List<Connection> cs = new ArrayList<Connection>();
    public ConnectionPool(String connectString, String username, String password, int size) {
        this.size = size;
        init(connectString, username, password);
    }
    public void init(String connectString, String username, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            for (int i = 0; i < size; i++) {
                Connection c = DriverManager.getConnection(connectString, username, password);
                cs.add(c);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public synchronized Connection getConnection() {
        while (cs.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Connection c = cs.remove(0);
        return c;
    }
    public synchronized void ReleaseConnection(Connection c) {
        cs.add(c);
        this.notifyAll();
    }
}
