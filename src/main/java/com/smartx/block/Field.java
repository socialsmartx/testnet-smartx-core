package com.smartx.block;

import java.math.BigInteger;

public class Field {
    public BigInteger amount = new BigInteger("0");
    public FldType type;
    public BigInteger fee = new BigInteger("0");
    public String hash = "";
    public String time = "";
    public enum FldType {
        SAT_FIELD_IN, SAT_FIELD_OUT
    }
    public static boolean IsValid(Block blk) {
        String inhash = "";
        String outhash = "";
        for (int i = 0; i < blk.Flds.size(); i++) {
            if (blk.Flds.get(i).type == Field.FldType.SAT_FIELD_IN) {
                inhash = blk.Flds.get(i).hash;
            } else if (blk.Flds.get(i).type == Field.FldType.SAT_FIELD_OUT) {
                outhash = blk.Flds.get(i).hash;
            }
        }
        if (inhash.equals(outhash)) {
            return false;
        }
        return true;
    }
}
