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
    public void AddBlock(Block blk) throws SatException, SQLException, SignatureException;
    BigInteger GetBalance(String address);
    String Transfer(String in, String out, BigInteger amount, SmartXWallet wallet);
    Block ShowBlock(String hash);
    List<Block> GetBlocks(long height, String address);
    List<TxBlock> GetBlocks(String address);
    long GetLatestHeight();
    Block GetMCBlock(long height);
}
