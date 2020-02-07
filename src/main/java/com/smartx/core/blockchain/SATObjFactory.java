package com.smartx.core.blockchain;

import com.smartx.Kernel;
import com.smartx.block.BlockRelation;
import com.smartx.cli.SmartxCommands;
import com.smartx.core.consensus.BlockMainTop;
import com.smartx.core.consensus.Consensus;
import com.smartx.core.coordinate.BlockCache;
import com.smartx.core.coordinate.RuleExecutor;
import com.smartx.db.AccountDB;
import com.smartx.db.DbSource;
import com.smartx.db.QueryDB;
import com.smartx.db.TransDB;
import com.smartx.mine.MineThread;
import com.smartx.mine.PoolThread;
import com.smartx.net.ChannelManager;
import com.smartx.net.msg.MessageHandle;

public class SATObjFactory {
    private static SatPeerManager client = null;
    private static TransDB txdb = null;
    private static AccountDB accdb = null;
    private static BlockRelation br = null;
    private static DbSource dbsrc = null;
    private static TraverBlock travblock = null;
    private static Consensus conss = null;
    private static BlockDAG blockdag = null;
    private static TraverBlock tvblock = null;
    private static RuleExecutor executor = null;
    private static QueryDB querydb = null;
    private static BlockMainTop blocktop = null;
    private static BlockCache cache = null;
    private static SmartxCommands commandexcute = null;
    public static SmartxCommands GetCommand() {
        if (commandexcute == null) {
            commandexcute = new SmartxCommands();
        }
        return commandexcute;
    }
    public static BlockCache GetCache() {
        if (cache == null) {
            cache = new BlockCache();
        }
        return cache;
    }
    public static BlockMainTop GetMainTop() {
        if (blocktop == null) {
            blocktop = new BlockMainTop();
        }
        return blocktop;
    }
    public static RuleExecutor GetExecutor() {
        if (executor == null) {
            executor = new RuleExecutor();
        }
        return executor;
    }
    public static TraverBlock GetTraveBlock() {
        if (tvblock == null) {
            tvblock = new TraverBlock();
        }
        return tvblock;
    }
    public static BlockDAG GetBlockDAG() {
        if (blockdag == null) {
            blockdag = new BlockDAG();
        }
        return blockdag;
    }
    public static Consensus GetConsensus() {
        if (conss == null) {
            conss = new Consensus();
        }
        return conss;
    }
    public static DbSource GetDbSource() {
        if (dbsrc == null) {
            dbsrc = new DbSource();
        }
        return dbsrc;
    }
    public static TraverBlock GetTravBlock() {
        if (travblock == null) {
            travblock = new TraverBlock();
        }
        return travblock;
    }
    public static SatPeerManager GetPeerMgr() {
        if (client == null) {
            client = new SatPeerManager();
        }
        return client;
    }
    public static BlockRelation GetBlockRl() {
        if (br == null) br = new BlockRelation(null);
        return br;
    }
    public static AccountDB GetAccDB() {
        if (null == accdb) {
            accdb = new AccountDB();
        }
        return accdb;
    }
    public static TransDB GetTxDB() {
        if (txdb == null) {
            txdb = new TransDB();
        }
        return txdb;
    }
    public static QueryDB GetQueryDB() {
        if (querydb == null) {
            querydb = new QueryDB();
        }
        return querydb;
    }
    static MineThread mineThread = null;
    public static MineThread GetMineThread() {
        if (mineThread == null) {
            mineThread = new MineThread();
        }
        return mineThread;
    }
    static PoolThread poolThread = null;
    public static PoolThread GetPoolThread() {
        if (poolThread == null) {
            poolThread = new PoolThread();
        }
        return poolThread;
    }
    public static ChannelManager channelManager = null;
    public static ChannelManager GetChannelMrg() {
        return channelManager;
    }
    public static MessageHandle messageHandle = null;
    public static MessageHandle GetMessageHandle() {
        if (messageHandle == null) {
            messageHandle = new MessageHandle();
        }
        return messageHandle;
    }
    public static Kernel kernel = null;
}
