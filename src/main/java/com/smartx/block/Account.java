package com.smartx.block;

import java.math.BigInteger;

import com.smartx.core.consensus.SmartxEpochTime;
import com.smartx.crypto.Sha256;
import com.smartx.crypto.UserKeyPair;
import com.smartx.util.Tools;

public class Account {
    public UserKeyPair kp = new UserKeyPair();
    public BigInteger balance = new BigInteger("0");
    public String address = "";
    public static void main(String[] args) {
    }
    public static Account CreateAccount() {
        long tm = System.currentTimeMillis();
        Account acc = new Account();
        Block blk = new Block();
        blk.header.btype = Block.BLKType.SMARTX_TXS;
        blk.header.amount = new BigInteger("500");
        blk.header.hash = "";
        blk.timenum = SmartxEpochTime.CalTimeEpochNum(tm);
        blk.header.headtype = 0;
        blk.header.timestamp = tm;
        blk.time = Tools.TimeStamp2DateEx(blk.header.timestamp);
        blk.header.nonce = Tools.getUUID();
        blk.header.address = Tools.getUUID();    // TODO for test
        blk.epoch = SmartxEpochTime.CalTimeEpochNum(blk.header.timestamp);
        String tostr = blk.toString();
        String str = Sha256.getH256(tostr);
        acc.balance = new BigInteger("0");
        return acc;
    }
}
