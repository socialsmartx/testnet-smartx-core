package com.smartx.core.blockchain;

import java.util.List;

import com.smartx.block.Block;

public interface IBlockHash {
    public String GetBlockSignString(Block blk);
    public String getH256(Block blk);
}
