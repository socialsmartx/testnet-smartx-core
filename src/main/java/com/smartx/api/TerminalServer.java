package com.smartx.api;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.smartx.core.blockchain.DataBase;
import com.smartx.message.MessageDispatch;
import com.sun.net.httpserver.HttpServer;

public class TerminalServer {
    private static final Logger log = Logger.getLogger("TerminalServer");
    public static void start() {
        try {
            if (DataBase.rpcserver != null) {
                String url[] = DataBase.rpcserver.split("\\:");
                HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(url[1])), 0);
                server.createContext("/terminal", new MessageDispatch());
                server.start();
            }
        } catch (IOException e) {
            log.error(e.toString());
        }
    }
}
