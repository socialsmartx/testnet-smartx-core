package com.smartx.core.consensus;

import java.math.BigInteger;

import com.smartx.block.Block;
import com.smartx.block.Field;

public class BlockMainTop {
    public Block MCBLOCK = null;
    public synchronized void SetMCBlock(Block blk) {
        MCBLOCK = blk;
    }
    public synchronized Block GetMCBlock() {
        return MCBLOCK;
    }
    public static Block InitRefField(Block mblk, Block refedblk) {
        Field field = new Field();
        field.amount = new BigInteger("0");
        field.type = Field.FldType.SAT_FIELD_OUT;
        field.hash = refedblk.header.hash;
        field.time = refedblk.time;
        mblk.Flds.add(field);    // main blk --> refedblock
        return mblk;
    }
}
