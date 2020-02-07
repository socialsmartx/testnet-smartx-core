package com.smartx.block;

import java.math.BigInteger;

import com.smartx.util.SimpleDecoder;
import com.smartx.util.SimpleEncoder;

public class BlockHeader {
    public String hash = "";
    public int headtype = 1;
    public Block.BLKType btype = Block.BLKType.UNKN;
    public BigInteger amount = new BigInteger("0");
    public long timestamp = 0L;
    public String address = "";
    public String nonce = "";
    public String random = "1";
    private byte[] encoded;
    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeString(hash);
        enc.writeBytes(encoded);
        return enc.toBytes();
    }
    public BlockHeader(String hash) {
    }
    public BlockHeader(String hash, byte[] enc) {
        this.hash = hash;
        SimpleDecoder dec = new SimpleDecoder(enc);
        this.headtype = dec.readInt();
        this.amount = new BigInteger(dec.readBytes());
        this.timestamp = dec.readLong();
        this.address = dec.readString();
        this.nonce = dec.readString();
        this.random = dec.readString();
        this.encoded = enc;
    }
    public static BlockHeader fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        String hash = dec.readString();
        byte[] encoded = dec.readBytes();
        return new BlockHeader(hash, encoded);
    }
}
