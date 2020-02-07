package com.smartx.util;

import java.math.BigInteger;

public class TxBlock {
    public enum TxType {
        TXIN, TXOUT
    }
    public String in = "";
    public String out = "";
    public BigInteger amount = new BigInteger("0");
    public String nonce = "";
    public String hash = "";
    public long timestamp = 0;
    public TxType type;
}
