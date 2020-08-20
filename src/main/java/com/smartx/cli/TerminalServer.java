package com.smartx.cli;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.smartx.config.SystemProperties;
import com.smartx.message.MessageDispatch;
import com.sun.net.httpserver.HttpServer;

public class TerminalServer {
    private static final Logger log = Logger.getLogger("TerminalServer");
    public static SystemProperties config = SystemProperties.getDefault();
    public static void start() {
        try {
            int port = config.getTerminalport();
            HttpServer server = HttpServer.create(new InetSocketAddress( port ), 0);
            server.createContext("/terminal", new MessageDispatch());
            server.start();
        } catch (IOException e) {
            log.error(e.toString());
        }
    }
}
