/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx.net.msg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smartx.block.Block;
import com.smartx.message.MCollection;
import com.smartx.util.SimpleDecoder;
import com.smartx.util.SimpleEncoder;

public class SmartXMessage extends Message {
    public static final String MESSAGE_QUERY_RULE = "100";
    public static final String MESSAGE_ADD_BLOCK = "101";
    public static final String MESSAGE_QUERY_BLOCK = "102";
    public static final String MESSAGE_QUERY_BLOCK_ALL = "103";
    public static final String MESSAGE_QUERY_MINE_HEIGHT = "105";
    public static final String MESSAGE_QUERY_BLOCK_MC = "104";
    public static final String MESSAGE_NEW_MINE_TASK = "106";
    public static final String MESSAGE_GET_MINE_TASK = "107";
    public class Message {
        public Map<String, String> args = null;
        public Map<String, String> data = null;
        public MCollection collection = null;
        public List<Block> txs = null;  //仅用于返回
    }
    public Message msg = new Message();
    public SmartXMessage(String cmd) {
        super(MessageCode.CORE, null);
        if (msg.args == null) msg.args = new HashMap<String, String>();
        if (msg.collection == null) msg.collection = new MCollection();
        msg.args.put("command", cmd);
    }
    public SmartXMessage() {
        super(MessageCode.CORE, null);
        msg.args = new HashMap<String, String>();
    }
    public SmartXMessage(byte[] uuid, byte[] body) {
        super(MessageCode.CORE, uuid, null);
        this.body = body;
        this.Decoder();
    }
    public void Decoder() {
        Gson gson = new GsonBuilder().create();
        SimpleDecoder dec = new SimpleDecoder(body);
        msg = gson.fromJson(dec.readString(), new TypeToken<Message>() {
        }.getType());
        //String str =  dec.readString();
        //System.out.println("SmartXMessagebody Decoder:"+str);
    }
    public void Encoder() {
        Gson gson = new GsonBuilder().create();
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeString(gson.toJson(msg));
        this.body = enc.toBytes();
        //SimpleDecoder dec = new SimpleDecoder(body);
        //String str =  dec.readString();
        //System.out.println("SmartXMessagebody Encoder:"+str);
    }
}





