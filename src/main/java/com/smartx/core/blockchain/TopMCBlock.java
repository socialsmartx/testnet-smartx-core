package com.smartx.core.blockchain;

import com.smartx.block.Block;

public class TopMCBlock {
    public Block topblock = null;
    public long height = 0;
    public void AddCount() {
        height++;
    }
    public void SetTopMCBlock(Block blk) {
        topblock = blk;
    }
    public Block GetTopMCBlock(Block blk) {
        return topblock;
    }
    public long GetTopEpochNum() {
        return topblock.epoch;
    }
}
