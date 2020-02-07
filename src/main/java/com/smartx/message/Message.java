package com.smartx.message;

import java.sql.SQLException;
import java.util.*;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smartx.block.Block;
import com.smartx.core.SmartxCore;
import com.smartx.core.consensus.GeneralMine;
import com.smartx.core.consensus.SatException;

public class Message {
    public static final String MESSAGE_QUERY_RULE = "100";
    public static final String MESSAGE_ADD_BLOCK = "101";
    public static final String MESSAGE_QUERY_BLOCK = "102";
    public static final String MESSAGE_QUERY_BLOCK_ALL = "103";
    public static final String MESSAGE_QUERY_BLOCK_MC = "104";
    public static final String MESSAGE_QUERY_MINE_HEIGHT = "105";
    public static final String MESSAGE_NEW_MINE_TASK = "106";
    public static final String MESSAGE_GET_MINE_TASK = "107";
    public static final String MESSAGE_GET_HEIGHT = "108";
    public static final String MESSAGE_GET_LATESTHEIGHT = "109";
    public static final String MESSAGE_STORAGE_LATESTHEIGHT = "110";
    public static final String MESSAGE_TOTAlBLOCKS = "111";
    public static final String MESSAGE_LASTEST_BLOCK = "112";
    public static final String MESSAGE_PROCED_BLOCKS = "113";
    public static final String NETCONN = "/querycmd";
    public static final String STATE = "state";
    public static final String STATS = "stats";
    public static final String MINING = "mining";
    public static final String NET = "net";
    public static final String ACCOUNT = "account";
    public static final String BALANCE = "balance";
    public static final String SORT = "sort";
    public static final String XFER = "xfer";
    public static final String SETCOINBASE = "setcoinbase";
    public static final String GETCOINBASE = "getcoinbase";
    public static final String MAIN = "main";
    public Map<String, String> args = null;
    public Map<String, String> data = null;
    public MCollection collection = null;
    public List<Block> txs = null;
    public static String ToJson(Message message) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(message);
    }
    public static Message FromJson(String messjson) {
        try {
            Gson gson = new GsonBuilder().create();
            Message mess = gson.fromJson(messjson, new TypeToken<Message>() {
            }.getType());
            return mess;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Message() {
    }
    public Message(String cmd) {
        if (args == null) args = new HashMap<String, String>();
        this.args.put("command", cmd);
    }
    @Test
    public void testMessage() throws SatException, SQLException {
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        core.InitGenesisEpoch();
        // 请求
        Message mess = new Message();
        mess.args = new HashMap<String, String>();
        mess.args.put("command", "200");
        mess.args.put("height", "0");
        mess.collection = new MCollection();
        mess.collection.mblock = GeneralMine.CreateMainBlock();
        mess.collection.blocks.add(GeneralMine.CreateMainBlock());
        //mess.txs = Collections.synchronizedList(new ArrayList<Block>());
        //mess.txs.add(GeneralMine.CreateMainBlock());
        Gson gson = new GsonBuilder().create();
        System.out.println(gson.toJson(mess));
        Message respone = new Message();
        respone.args = new HashMap<String, String>();
        respone.args.put("ret", "0");
        respone.args.put("res_info", "ok");
        respone.collection = new MCollection();
        respone.collection.mblock = GeneralMine.CreateMainBlock();
        respone.collection.blocks.add(GeneralMine.CreateMainBlock());
        respone.txs = Collections.synchronizedList(new ArrayList<Block>());
        respone.txs.add(GeneralMine.CreateMainBlock());
        System.out.println(gson.toJson(respone));
    }
}
