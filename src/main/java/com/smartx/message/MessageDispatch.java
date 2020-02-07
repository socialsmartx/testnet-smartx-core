package com.smartx.message;

import java.io.IOException;
import java.io.OutputStream;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.smartx.block.Block;
import com.smartx.cli.SmartxCommands;
import com.smartx.core.SmartxCore;
import com.smartx.core.blockchain.BlockDAG;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.TraverBlock;
import com.smartx.core.consensus.SatException;
import com.smartx.db.TransDB;
import com.smartx.mine.MineThread;
import com.smartx.mine.PoolThread;
import com.smartx.util.Tools;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class MessageDispatch implements HttpHandler {
    public String messagejson = "";
    private static final Logger log = Logger.getLogger("core");
    public void packReturn(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    public MessageDispatch() {
    }
    public MessageDispatch(String json) {
        messagejson = json;
    }
    @Test
    public void testRuleSignQuery() throws SatException, SQLException, SignatureException {
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        BlockDAG blkdag = SATObjFactory.GetBlockDAG();
        Block block = blkdag.RuleSignQuery(24822);
        System.out.println(Tools.ToJson(block));
    }
    public String QueryRuleSign(Message message) throws IOException, SatException, SQLException, SignatureException {
        String response = "";
        BlockDAG blkdag = SATObjFactory.GetBlockDAG();
        long height = Long.parseLong(message.args.get("height"));
        Block block = blkdag.RuleSignQuery(height);
        if (block != null) {
            Message resp = new Message();
            resp.txs = Collections.synchronizedList(new ArrayList<Block>());
            resp.args = new HashMap<String, String>();
            resp.args.put("ret", "0");
            resp.args.put("res_info", "OK");
            resp.collection = new MCollection();
            resp.collection.mblock = block;
            response = Message.ToJson(resp);
            return Tools.getURLEncoderString(response);
        }
        return "";
    }
    public String AddBlocks(Message message) throws IOException, SatException, SQLException, SignatureException {
        String response = "0";
        BlockDAG blkdag = SATObjFactory.GetBlockDAG();
        if (message.collection != null && message.collection.mblock != null) {
            if (message.collection.blocks != null && message.collection.blocks.size() > 0) {
                for (int i = 0; i < message.collection.blocks.size(); i++) {
                    blkdag.AddBlock(message.collection.blocks.get(i));
                }
            }
            blkdag.AddBlock(message.collection.mblock);
            Message resp = new Message();
            resp.txs = Collections.synchronizedList(new ArrayList<Block>());
            resp.args = new HashMap<String, String>();
            resp.args.put("ret", "0");
            resp.args.put("res_info", "OK");
            response = Message.ToJson(resp);
            return Tools.getURLEncoderString(response);
        }
        return "";
    }
    public String QueryBlock(Message message) throws IOException, SatException, SQLException, SignatureException {
        String hash = message.args.get("hash");
        TransDB txdb = SATObjFactory.GetTxDB();
        Block blk = txdb.GetBlock(hash, txdb.GetDbtype(hash));
        Message resp = new Message();
        resp.txs = Collections.synchronizedList(new ArrayList<Block>());
        resp.txs.add(blk);
        resp.args = new HashMap<String, String>();
        resp.args.put("ret", "0");
        resp.args.put("res_info", "OK");
        String response = "do setconfig";
        response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String QueryBlockAll(Message message) throws IOException, SatException, SQLException, SignatureException {
        int height = Integer.parseInt(message.args.get("height"));
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        TransDB txdb = SATObjFactory.GetTxDB();
        List<Block> lists = tvblock.GetAllBolck(height);
        Message resp = new Message();
        resp.txs = lists;
        resp.args = new HashMap<String, String>();
        resp.args.put("ret", "0");
        resp.args.put("res_info", "OK");
        String response = "do setconfig";
        response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String QueryBlockMC(Message message) throws IOException, SatException, SQLException, SignatureException {
        int height = Integer.parseInt(message.args.get("height"));
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        Block blk = tvblock.GetMCBlock(height);
        Message resp = new Message();
        resp.txs = Collections.synchronizedList(new ArrayList<Block>());
        resp.txs.add(blk);
        resp.args = new HashMap<String, String>();
        resp.args.put("ret", "0");
        resp.args.put("res_info", "OK");
        String response = "do setconfig";
        response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String QueryMineHeight(Message message) throws IOException, SatException, SQLException, SignatureException {
        String response = "ok";
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        String height = String.valueOf(tvblock.GetMineBlockHeight());
        Message resp = new Message();
        resp.args = new HashMap<String, String>();
        resp.args.put("height", height);
        resp.args.put("ret", "0");
        resp.args.put("res_info", "OK");
        response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String NewMineTask(Message message) throws IOException, SatException, SQLException, SignatureException {
        long height = Long.valueOf(message.args.get("height"));
        String miningblk = message.args.get("miningblk");
        long start = Long.valueOf(message.args.get("start"));
        long end = Long.valueOf(message.args.get("end"));
        long taskid = Long.valueOf(message.args.get("taskid"));
        MineThread mineThread = SATObjFactory.GetMineThread();
        mineThread.OnNewMineTask(miningblk, taskid, height, start, end);
        String response = "ok";
        return Tools.getURLEncoderString(response);
    }
    // Mine to Pool
    public String GetMineTask(Message message) throws IOException, SatException, SQLException, SignatureException {
        PoolThread poolThread = SATObjFactory.GetPoolThread();
        Message resp = poolThread.OnGetMineTask(message);
        String response = "do setconfig";
        response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String GetSort(Message message) {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        int count = 0;
        if (message.args != null && !message.args.get("value").equals("")) {
            count = Integer.parseInt(message.args.get("value"));
        }
        try {
            tvblock.SortBlockFront(count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public String GetState(Message message) throws IOException, SatException, SQLException, SignatureException {
        String value = message.args.get("value");
        Message resp = new Message();
        resp.args = new HashMap<String, String>();
        resp.args.put("ret", "0");
        resp.args.put("res_info", SmartxCommands.Status());
        String response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String GetStats(Message message) throws IOException, SatException, SQLException, SignatureException {
        String value = message.args.get("value");
        Message resp = new Message();
        resp.args = new HashMap<String, String>();
        resp.args.put("ret", "0");
        resp.args.put("res_info", SmartxCommands.ShowStats());
        String response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String GetMining(Message message) throws IOException, SatException, SQLException, SignatureException {
        String value = message.args.get("value");
        Message resp = new Message();
        resp.args = new HashMap<String, String>();
        resp.args.put("ret", "0");
        SmartxCommands.Mining();
        resp.args.put("res_info", "");
        String response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String GetNet(Message message) {
        String value = message.args.get("value");
        Message resp = new Message();
        resp.args = new HashMap<String, String>();
        resp.args.put("ret", "0");
        resp.args.put("res_info", SmartxCommands.shownet());
        String response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String GetAccount(Message message) {
        String value = message.args.get("value");
        Message resp = new Message();
        resp.args = new HashMap<String, String>();
        resp.args.put("ret", "0");
        resp.args.put("res_info", SmartxCommands.list_accounts());
        String response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String GetBalance(Message message) {
        String value = message.args.get("value");
        Message resp = new Message();
        resp.args = new HashMap<String, String>();
        resp.args.put("ret", "0");
        resp.args.put("res_info", SmartxCommands.ShowBalance());
        String response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String GetXfer(Message message) {
        String to = message.args.get("to");
        String amount = message.args.get("amount");
        Message resp = new Message();
        resp.args = new HashMap<String, String>();
        resp.args.put("ret", "0");
        resp.args.put("res_info", SmartxCommands.Transfer(to, amount));
        String response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String GetCoinBase(Message message) {
        String to = message.args.get("to");
        String amount = message.args.get("amount");
        Message resp = new Message();
        resp.args = new HashMap<String, String>();
        resp.args.put("ret", "0");
        resp.args.put("res_info", SmartxCommands.GetCoinBase());
        String response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String SetCoinBase(Message message) {
        String value = message.args.get("value");
        SmartxCommands.SetCoinBase(value);
        Message resp = new Message();
        resp.args = new HashMap<String, String>();
        resp.args.put("ret", "0");
        resp.args.put("res_info", value);
        String response = Message.ToJson(resp);
        return Tools.getURLEncoderString(response);
    }
    public String MessageProcTerminal() throws SQLException, SignatureException, SatException, IOException {
        Message message = Message.FromJson(messagejson);
        if (message == null || message.args == null) return "";
        String command = message.args.get("command");
        switch (command) {
            case Message.MESSAGE_QUERY_RULE:
                return QueryRuleSign(message);
            case Message.STATE:
                return GetState(message);
            case Message.SORT:
                return GetSort(message);
            case Message.STATS:
                return GetStats(message);
            case Message.MINING:
                return GetMining(message);
            case Message.NET:
                return GetNet(message);
            case Message.ACCOUNT:
                return GetAccount(message);
            case Message.BALANCE:
                return GetBalance(message);
            case Message.XFER:
                return GetXfer(message);
            case Message.GETCOINBASE:
                return GetCoinBase(message);
            case Message.SETCOINBASE:
                return SetCoinBase(message);
            default:
        }
        return "";
    }
    public String MessageProc() throws SQLException, SignatureException, SatException, IOException {
        Message message = Message.FromJson(messagejson);
        if (message == null || message.args == null) return "";
        String command = message.args.get("command");
        switch (command) {
            case Message.MESSAGE_QUERY_RULE:
                return QueryRuleSign(message);
            //case Message.MESSAGE_ADD_BLOCK:
            //   return AddBlocks(message);
            case Message.MESSAGE_QUERY_BLOCK:
                return QueryBlock(message);
            case Message.MESSAGE_QUERY_BLOCK_ALL:
                return QueryBlockAll(message);
            case Message.MESSAGE_QUERY_BLOCK_MC:
                return QueryBlockMC(message);
            case Message.MESSAGE_QUERY_MINE_HEIGHT:
                return QueryMineHeight(message);
            case Message.MESSAGE_NEW_MINE_TASK:
                return NewMineTask(message);
            case Message.MESSAGE_GET_MINE_TASK:
                return GetMineTask(message);
            default:
        }
        return "";
    }
    @Test
    public void testMessDispatch() {
        System.out.println("hello");
    }
    public void Query(HttpExchange exchange) throws IOException, SatException, SQLException, SignatureException {
        String response = "ret=0&res_info=ok";
        String queryString = exchange.getRequestURI().getQuery();
        messagejson = Tools.getURLDecoderString(queryString);
        if (messagejson.equals("")) return;
        packReturn(exchange, MessageProcTerminal());
    }
    @Override
    public void handle(HttpExchange exchange) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Query(exchange);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
