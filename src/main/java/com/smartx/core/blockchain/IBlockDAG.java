package com.smartx.core.blockchain;

import java.math.BigInteger;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.List;

import com.smartx.block.Block;
import com.smartx.core.consensus.SatException;
import com.smartx.util.TxBlock;
import com.smartx.wallet.SmartXWallet;

public interface IBlockDAG {
    void AddBlock(Block blk) throws SatException, SQLException, SignatureException;
    BigInteger GetBalance(String address);
    Block ShowBlock(String hash);
    List<Block> GetBlocks(long height, String address);
    List<TxBlock> GetBlocks(String address);
    long GetLatestHeight();
    boolean Transfer(String rawjson);
    Block GetMCBlock(long height);
}
