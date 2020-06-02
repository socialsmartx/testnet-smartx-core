package com.smartx.block;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.smartx.core.blockchain.BlockHash;
import com.smartx.core.blockchain.DataBase;
import com.smartx.core.coordinate.RuleSign;
import com.smartx.crypto.Hash;
import com.smartx.crypto.HashUtil;
import com.smartx.util.SimpleEncoder;
import com.smartx.wallet.SmartXWallet;

import io.github.novacrypto.base58.Base58;

public class Block {
    public enum BLKType {
        UNKN, SMARTX_MAIN,                //	1
        SMARTX_MAINREF,                   //	2
        SMARTX_TXS,                       // 	3
    }
    public BlockHeader header = new BlockHeader("");
    public BigInteger rewards = new BigInteger("0");
    public String time = "";
    public long epoch = 0L;
    public long timenum = 0;
    public long height = 0;
    public int status = DataBase.NOBROADCAST;
    public String diff = "";
    public String blackrefer = "";
    public String nodename = "node1";
    public String recvtime = "";
    public String mkl_hash = "";
    public String premkl_hash = "";
    public List<Field> Flds = new ArrayList<Field>();
    public String sign = "";
    public List<RuleSign> ruleSigns = null;
    public Block() {
    }
    public String ToSignString() {
        BlockHash blkhash = new BlockHash();
        return blkhash.GetBlockSignString(this);
    }
    public String ToSignStringBase58() {
        BlockHash blkhash = new BlockHash();
        String rawstr = blkhash.GetBlockSignString(this);
        byte[] rawhash = null;
        String base58RawHash = "";
        if (SmartXWallet.wallettype == SmartXWallet.FAST_WALLETTYPE) {
            rawhash = Hash.h256(rawstr.getBytes());
            base58RawHash = Base58.base58Encode(rawhash);
        } else {
            rawhash = HashUtil.sha3(rawstr.getBytes());
        }
        return base58RawHash;
    }
    public Field GetInField() {
        for (int i = 0; i < Flds.size(); i++) {
            if (Field.FldType.SAT_FIELD_IN == Flds.get(i).type) {
                return Flds.get(i);
            }
        }
        return null;
    }
    public Field GetOutField() {
        for (int i = 0; i < Flds.size(); i++) {
            if (Field.FldType.SAT_FIELD_OUT == Flds.get(i).type) {
                return Flds.get(i);
            }
        }
        return null;
    }
    public String GetInHash() {
        for (int i = 0; i < Flds.size(); i++) {
            if (Field.FldType.SAT_FIELD_IN == Flds.get(i).type) {
                return Flds.get(i).hash;
            }
        }
        return "";
    }
    public String GetOutHash() {
        for (int i = 0; i < Flds.size(); i++) {
            if (Field.FldType.SAT_FIELD_OUT == Flds.get(i).type) {
                return Flds.get(i).hash;
            }
        }
        return "";
    }
    public synchronized byte[] toBytes(boolean withHash) {
        SimpleEncoder enc = new SimpleEncoder();
        return enc.toBytes();
    }
}
