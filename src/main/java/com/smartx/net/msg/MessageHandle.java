package com.smartx.net.msg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.block.Block;
import com.smartx.block.Field;
import com.smartx.config.SystemProperties;
import com.smartx.core.blockchain.*;
import com.smartx.core.consensus.ErrCode;
import com.smartx.core.consensus.SatException;
import com.smartx.core.consensus.SmartxEpochTime;
import com.smartx.db.TransDB;
import com.smartx.message.MCollection;
import com.smartx.net.Channel;
import com.smartx.util.Tools;

public class MessageHandle {
    private static final Logger log = Logger.getLogger("core");
    public SystemProperties config = SystemProperties.getDefault();
    public MessageHandle() {
        SATObjFactory.GetChannelMrg().RegMessage(SmartXMessage.MESSAGE_QUERY_RULE, this, "OnQueryRuleSign");
        SATObjFactory.GetChannelMrg().RegMessage(SmartXMessage.MESSAGE_ADD_BLOCK, this, "OnAddBlocks");
        SATObjFactory.GetChannelMrg().RegMessage(SmartXMessage.MESSAGE_QUERY_BLOCK, this, "OnQueryBlock");
        SATObjFactory.GetChannelMrg().RegMessage(SmartXMessage.MESSAGE_QUERY_BLOCK_ALL, this, "OnQueryBlockAll");
        SATObjFactory.GetChannelMrg().RegMessage(SmartXMessage.MESSAGE_QUERY_BLOCK_MC, this, "OnQueryBlockMC");
        SATObjFactory.GetChannelMrg().RegMessage(SmartXMessage.MESSAGE_QUERY_MINE_HEIGHT, this, "OnQueryMineHeight");
    }
    public Channel GetNodeBest() throws Exception {
        String Max = "0";
        Channel chantemp = null;
        List<Channel> list = SATObjFactory.GetChannelMrg().getActiveChannels();
        for (Channel c : list) {
            String stmp = getLatestHeightRemote(c);
            if (stmp.compareTo(Max) > 0) {
                Max = stmp;
                chantemp = c;
            }
        }
        // 断线重连
        if (chantemp == null) {
            SATObjFactory.kernel.getNodeManager().stop();
            SATObjFactory.kernel.getNodeManager().start();
            SmartxEpochTime.Sleep(2000);
        }
        return chantemp;
    }
    public void CheckChannel(){
        List<Channel> list = SATObjFactory.GetChannelMrg().getActiveChannels();
        if (list.size() == 0) {
            SATObjFactory.kernel.getNodeManager().stop();
            SATObjFactory.kernel.getNodeManager().start();
            SmartxEpochTime.Sleep(500);
        }
    }
    public long GetHeightBest(){
        try {
            Channel c = GetNodeBest();
            if (c == null) return 0;
            String maxheight = getLatestHeightRemote(c);
            if (maxheight !=null && !maxheight.equals("") && !maxheight.equals("0")){
                return Long.parseLong(maxheight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    // 获得最新的高度
    public String getLatestHeightRemote(Channel channel) throws Exception {
        SmartXMessage message = new SmartXMessage();
        message.msg.args = new HashMap<String, String>();
        message.msg.args.put("command", SmartXMessage.MESSAGE_QUERY_MINE_HEIGHT);
        SmartXMessage resp = channel.queryMessage(message, 5000);
        if (resp != null) {
            return resp.msg.args.get("height");
        }
        return "";
    }
    // 广播MC引用的所有tx和MC引用的所有MC
    public void BroadCastMBlock(Block blk) throws Exception {
        TransDB smartxdb = SATObjFactory.GetTxDB();
        SmartXMessage message = new SmartXMessage(SmartXMessage.MESSAGE_ADD_BLOCK);
        message.msg.collection.mblock = blk;
        if (message.msg.args != null)
            message.msg.args.put("id", SATObjFactory.kernel.getClient().getPeerId());
        try {
            if (blk.header.btype == Block.BLKType.SMARTX_TXS){
                Broadcast(message);
                return;
            }
            for (int i = 0; i < blk.Flds.size(); i++) {
                if (Field.FldType.SAT_FIELD_OUT == blk.Flds.get(i).type) {
                    String hash = blk.Flds.get(i).hash;
                    Block tmpblk = smartxdb.GetBlock(hash, DataBase.SMARTX_BLOCK_HISTORY);
                    if (tmpblk == null) {
                        throw new SatException(ErrCode.SAT_BLOCKREF_ERROR, "[" + hash + "] isn't exist in db");
                    }
                    if (tmpblk.header.btype == Block.BLKType.SMARTX_MAIN || tmpblk.header.btype == Block.BLKType.SMARTX_MAINREF){
//                        for (int j=0; j<tmpblk.Flds.size(); j++){
//                            Block txblock = smartxdb.GetBlock(tmpblk.Flds.get(j).hash, DataBase.SMARTX_BLOCK_HISTORY);
//                            if (txblock == null) continue;
//                            message.msg.collection.blocks.add(txblock);
//                        }
                    }
                    message.msg.collection.blocks.add(tmpblk);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Broadcast(message);
    }
    public void Broadcast(SmartXMessage message) throws Exception {
        List<Channel> list = SATObjFactory.GetChannelMrg().getActiveChannels();
        for (Channel c : list) {
            c.sendMessage(message);
        }
    }
    public Block RuleQuery(Block blk, long timeout) throws Exception {
        SatPeerManager peer = SATObjFactory.GetPeerMgr();
        String rulesign = config.getRuleSignInfo2();
        Channel channel = SATObjFactory.GetChannelMrg().getChannels(rulesign);
        if (channel == null) return null;
        SmartXMessage message = new SmartXMessage();
        message.msg.args = new HashMap<String, String>();
        message.msg.args.put("height", String.valueOf(blk.height));
        message.msg.args.put("command", com.smartx.message.Message.MESSAGE_QUERY_RULE);
        SmartXMessage resp = channel.queryMessage(message, timeout);
        if (resp != null && resp.msg.args.get("ret").equals("0")) {
            // 得到已经签名的block
            Block block = resp.msg.collection.mblock;
            log.info("MC is :" + block.header.hash + " rulelen:" + Tools.ToRuleSignListByList(block.ruleSigns).length());
            return block;
        }
        return null;
    }
    public void OnQueryRuleSign(Channel channel, Message msg) throws Exception {
        SmartXMessage message = (SmartXMessage) msg;
        BlockDAG blkdag = SATObjFactory.GetBlockDAG();
        long height = Long.parseLong(message.msg.args.get("height"));
        // 查询裁决签名
        Block block = blkdag.RuleSignQuery(height);
        if (block != null) {
            SmartXMessage resp = new SmartXMessage();
            resp.msg.txs = Collections.synchronizedList(new ArrayList<Block>());
            resp.msg.args = new HashMap<String, String>();
            resp.msg.args.put("ret", "0");
            resp.msg.args.put("res_info", "OK");
            resp.msg.collection = new MCollection();
            resp.msg.collection.mblock = block;
            channel.replyMessage((SmartXMessage) message, resp);
        }
    }
    public void OnAddBlocks(Channel channel, Message msg) throws Exception {
        BlockDAG blkdag = SATObjFactory.GetBlockDAG();
        SmartXMessage message = (SmartXMessage) msg;
        if (message.msg.args != null && message.msg.args.get("id") != null) {
            Tools.broadcastID(message.msg.args.get("id"),
                    SATObjFactory.kernel.getClient().getPeerId());

        }
        if (message.msg.collection != null && message.msg.collection.mblock != null) {
            if (message.msg.collection.blocks != null && message.msg.collection.blocks.size() > 0) {
                for (int i = 0; i < message.msg.collection.blocks.size(); i++) {
                    blkdag.AddBlock(message.msg.collection.blocks.get(i));
                }
            }
            blkdag.AddBlock(message.msg.collection.mblock);
            SmartXMessage resp = new SmartXMessage();
            resp.msg.txs = Collections.synchronizedList(new ArrayList<Block>());
            resp.msg.args = new HashMap<String, String>();
            resp.msg.args.put("ret", "0");
            resp.msg.args.put("res_info", "OK");
            channel.replyMessage((SmartXMessage) message, resp);
        }
    }
    public Block QueryBolck(Channel channel, String hash, long timeout) throws Exception {
        SmartXMessage message = new SmartXMessage(SmartXMessage.MESSAGE_QUERY_BLOCK);
        message.msg.args.put("hash", hash);
        SmartXMessage resp = channel.queryMessage(message, timeout);
        if (resp != null) {
            return resp.msg.txs.get(0);
        }
        return null;
    }
    public void OnQueryBlock(Channel channel, Message message) throws Exception {
        String hash = ((SmartXMessage) message).msg.args.get("hash");
        TransDB txdb = SATObjFactory.GetTxDB();
        Block blk = txdb.GetBlock(hash, DataBase.SMARTX_BLOCK_HISTORY);
        SmartXMessage resp = new SmartXMessage();
        resp.msg.txs = Collections.synchronizedList(new ArrayList<Block>());
        resp.msg.txs.add(blk);
        resp.msg.args = new HashMap<String, String>();
        resp.msg.args.put("ret", "0");
        resp.msg.args.put("res_info", "OK");
        channel.replyMessage((SmartXMessage) message, resp);
    }
    public List<Block> QueryAllBolck(Channel channel, int height, long timeout) throws Exception {
        SmartXMessage message = new SmartXMessage();
        message.msg.args = new HashMap<String, String>();
        message.msg.args.put("command", SmartXMessage.MESSAGE_QUERY_BLOCK_ALL);
        message.msg.args.put("height", String.valueOf(height));
        SmartXMessage resp = channel.queryMessage(message, timeout);
        if (resp != null) {
            return resp.msg.txs;
        }
        return null;
    }
    public void OnQueryBlockAll(Channel channel, Message message) throws Exception {
        int height = Integer.parseInt(((SmartXMessage) message).msg.args.get("height"));
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        TransDB txdb = SATObjFactory.GetTxDB();
        List<Block> lists = tvblock.GetAllBolck(height);
        SmartXMessage resp = new SmartXMessage();
        resp.msg.txs = lists;
        resp.msg.args = new HashMap<String, String>();
        resp.msg.args.put("ret", "0");
        resp.msg.args.put("res_info", "OK");
        channel.replyMessage((SmartXMessage) message, resp);
    }
    public Block QueryBlockMC(Channel channel, int height, long timeout) throws Exception {
        SmartXMessage message = new SmartXMessage();
        message.msg.args = new HashMap<String, String>();
        message.msg.args.put("command", SmartXMessage.MESSAGE_QUERY_BLOCK_MC);
        message.msg.args.put("height", String.valueOf(height));
        SmartXMessage resp = channel.queryMessage(message, timeout);
        if (resp != null) {
            return resp.msg.txs.get(0);
        }
        return null;
    }
    public void OnQueryBlockMC(Channel channel, Message message) throws Exception {
        int height = Integer.parseInt(((SmartXMessage) message).msg.args.get("height"));
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        Block blk = tvblock.GetMCBlock(height);
        SmartXMessage resp = new SmartXMessage();
        resp.msg.txs = Collections.synchronizedList(new ArrayList<Block>());
        resp.msg.txs.add(blk);
        resp.msg.args = new HashMap<String, String>();
        resp.msg.args.put("ret", "0");
        resp.msg.args.put("res_info", "OK");
        channel.replyMessage((SmartXMessage) message, resp);
    }
    public long QueryMineHeight(Channel channel, long timeout) throws Exception {
        SmartXMessage message = new SmartXMessage();
        message.msg.args = new HashMap<String, String>();
        message.msg.args.put("command", SmartXMessage.MESSAGE_QUERY_MINE_HEIGHT);
        SmartXMessage resp = channel.queryMessage((SmartXMessage) message, timeout);
        if (resp != null) {
            return Long.valueOf(resp.msg.args.get("height"));
        }
        return 0;
    }
    public void OnQueryMineHeight(Channel channel, Message message) throws Exception {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        String height = String.valueOf(tvblock.GetMineBlockHeight());
        SmartXMessage resp = new SmartXMessage(SmartXMessage.MESSAGE_QUERY_MINE_HEIGHT);
        resp.msg.args = new HashMap<String, String>();
        resp.msg.args.put("height", height);
        resp.msg.args.put("ret", "0");
        resp.msg.args.put("res_info", "OK");
        channel.replyMessage((SmartXMessage) message, resp);
    }
}
