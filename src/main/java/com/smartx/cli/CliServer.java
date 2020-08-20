package com.smartx.cli;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class CliServer implements Runnable {
    protected static Logger logger = Logger.getLogger(CliServer.class);
    private static ServerSocket svrSocket;
    public static long iconectMax = 0;
    public CliServer() throws IOException {
        String host = "127.0.0.1";
        int port = 8081;
        int maxReadThreads = 5;
        for (int i = 0; i < maxReadThreads; i++) {
            Thread recv = new TerminalReader("reader" + i);
            recv.start();
        }
        svrSocket = new ServerSocket();
        InetSocketAddress address = new InetSocketAddress(host, port);
        svrSocket.bind(address);
        logger.info("CliServer listening on " + host + ":" + String.valueOf(port));
    }
    public void run() {
        while (true) {
            try {
                logger.info("begin to accept..");
                Socket socket = svrSocket.accept();
                logger.info("accept a client: " + socket.toString());
                if (TerminalReader.GetSockSize() > CliServer.iconectMax) {
                    logger.error("clients more than max count");
                    continue;
                }
                handleKey(socket);
            } catch (IOException ex) {
                logger.error("error:" + ex.toString());
            }
        }
    }
    private void handleKey(Socket key) {
        logger.info("put client to a messq");
        TerminalReader.AddSocketPool(key);
        return;
    }
    public static void main(String[] args) {
        try {
            CliServer server = new CliServer();
            Thread gwServer = new Thread(server, "CliServer");
            gwServer.setPriority(Thread.MAX_PRIORITY);
            gwServer.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
