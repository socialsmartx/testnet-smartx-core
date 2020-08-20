package com.smartx.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.message.Message;
import com.smartx.message.MessageDispatch;
import com.smartx.util.Tools;

public class TerminalReader extends Thread {
    protected static Logger logger = Logger.getLogger(TerminalReader.class);
    private static int BUFFER_SIZE = 1024 * 32;
    private static List<Socket> pool = Collections.synchronizedList(new LinkedList<Socket>());
    private MessageDispatch messagedispatch = new MessageDispatch();
    public TerminalReader(String name) {
        super(name);
    }
    public void run() {
        while (true) {
            try {
                Socket sc;
                synchronized (pool) {
                    while (pool.isEmpty()) {
                        pool.wait();
                    }
                    logger.info("begin to do do work process");
                    sc = (Socket) pool.remove(0);
                }
                process(sc);
            } catch (Exception ex) {
                continue;
            }
        }
    }
    private void process(Socket sc) throws IOException {
        try {
            if (sc == null) {
                logger.error("Inner error");
                return;
            }
            while (!sc.isClosed()) {
                String clientData = readRequest(sc);
                if (clientData == null) {
                    sc.close();
                    return;
                }
                Message message = new Message(clientData);
                message.args = new HashMap<String, String>();
                message.args.put("command", clientData);
                message.args.put("value", "");
                messagedispatch.messagejson = Message.ToJson(message);
                String outString = messagedispatch.MessageTelnet();
                outString = Tools.getURLDecoderString(outString);
                outString += "\r\n";
                OutputStream out = sc.getOutputStream();
                out.write(outString.getBytes());
                out.flush();
            }
        } catch (IOException ex) {
            logger.error("recv error" + ex.toString());
            sc.close();
            return;
        } catch (Exception e) {
            logger.error("error:", e);
        }
    }
    private String readRequest(Socket sc) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(sc.getInputStream(), "UTF-8"));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        if ((line = br.readLine()) != null) {
            buffer.append(line);
        }
        //System.out.println(buffer.toString());
        return new String(buffer);
    }
    public static int GetSockSize() {
        return pool.size();
    }
    public static void AddSocketPool(Socket sc) {
        synchronized (pool) {
            if (sc == null) {
                return;
            }
            pool.add(pool.size(), sc);
            logger.info("pool=" + Integer.toString((pool.size())));
            pool.notifyAll();
        }
    }
}
