package com.smartx.core.consensus;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.smartx.block.Block;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.TraverBlock;
import com.smartx.db.TransDB;
import com.smartx.net.Channel;
import com.smartx.net.msg.MessageHandle;

public class Consensus {
    private static final Logger log = Logger.getLogger("core");
    public boolean ReSendBlock() throws Exception {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        TransDB smartxdb = SATObjFactory.GetTxDB();
        MessageHandle msghandle = SATObjFactory.GetMessageHandle();
        Channel channel = SATObjFactory.GetMessageHandle().GetNodeBest();
        if (channel == null) return true;
        long rmnum = msghandle.QueryMineHeight(channel, 5000);
        long lcnum = tvblock.GetMineBlockHeight();
        if (lcnum >= rmnum) {
            ArrayList<Block> blocks = smartxdb.GetAllMainHeight(lcnum);
            for (Block blk : blocks) {
                log.info("	resend hash:" + blk.header.hash);
                msghandle.BroadCastMBlock(blk);
            }
            return true;
        }
        return false;
    }
}
