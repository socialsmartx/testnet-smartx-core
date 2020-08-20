package com.smartx.db;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MybatisJDBCUtil {
    private static final String resource = "src/mybatis-config.xml";// SRC 根目录下必须有这个文件
    private static InputStream inputStream;
    private static SqlSessionFactory sqlSessionFactory;
    static {
        try {
            inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("can't find " + resource);
        }
    }
    private static final ThreadLocal<SqlSession> THREAD_LOCAL = new ThreadLocal<SqlSession>();
    public static SqlSession currentSession() {
        SqlSession session = THREAD_LOCAL.get();
        if (session == null) {
            session = sqlSessionFactory.openSession();
            THREAD_LOCAL.set(session);
        }
        return session;
    }
    public static void closeSession() {
        SqlSession session = THREAD_LOCAL.get();
        THREAD_LOCAL.set(null);
        if (session != null) {
            session.close();
        }
    }
}
