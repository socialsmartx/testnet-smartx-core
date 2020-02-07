package com.smartx.core.blockchain;

import java.util.List;

import com.smartx.block.Block;
import com.smartx.block.Field;
import com.smartx.crypto.Hash;
import com.smartx.crypto.Sha256;

public class BlockHash implements IBlockHash {
    public String GetBlockSignString(Block blk) {
        String signstr = String.valueOf(blk.header.headtype);
        signstr += "-";
        signstr += blk.header.btype;
        signstr += "-";
        signstr += blk.header.timestamp;
        signstr += "-";
        signstr += blk.header.address;
        signstr += "-";
        signstr += blk.header.nonce;
        signstr += "-";
        if (Block.BLKType.SMARTX_TXS == blk.header.btype) {
            Field in = blk.GetInField();
            signstr += in.hash;
            signstr += "-";
            Field out = blk.GetOutField();
            signstr += out.hash;
            signstr += "-";
            signstr += in.amount;
        } else if (Block.BLKType.SMARTX_MAIN == blk.header.btype || Block.BLKType.SMARTX_MAINREF == blk.header.btype) {
        }
        return signstr;
    }
    public String getH256(Block blk) {
        return Hash.h256(Hash.h256(blk.ToSignString()) + blk.header.random);
    }
    public String getSha256(Block blk) {
        return Sha256.getSHA256(Sha256.getSHA256(blk.ToSignString()) + blk.header.random);
    }
    public String CalTMCBlockHash(Block blk) {
        return "";
    }
    public String CalTBlockReferHash(Block blk, List<Block> blks) {
        return "";
    }
}
