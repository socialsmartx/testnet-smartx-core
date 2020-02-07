package com.smartx.block;

import com.smartx.core.blockchain.DataBase;
import com.smartx.core.consensus.SmartxEpochTime;
import com.smartx.util.Tools;

public class Epoch {
    public String tmbegin = "";     //
    public String tmend = "";       //
    public long epochnum = 0;        //
    public long timenum = 0;         //
    public Block block = null;      //
    void Epoch() {
    }
    void Epoch(int num) {
    }
    void Epoch(Block blk) {
        this.epochnum = blk.epoch;
        this.timenum = blk.timenum;
    }
    void Epoch(long epoch) {
        this.epochnum = epochnum;
        this.timenum = epochnum - DataBase.genesisEpoch + 1;
    }
    public static Epoch GetNowEpoch() {
        Epoch pch = new Epoch();
        pch.tmbegin = SmartxEpochTime.GetSystime();
        pch.epochnum = SmartxEpochTime.StrToStamp(pch.tmbegin) / SmartxEpochTime.MAIN_CHAIN_PERIOD;
        return pch;
    }
    public long GetEpochNum() {
        return timenum;
    }
    public static long GetCurEpoch() {
        long tm = SmartxEpochTime.get_timestamp();
        String time = Tools.TimeStamp2DateEx(tm);
        Epoch pch = new Epoch();
        pch.tmbegin = SmartxEpochTime.TimeStamp2DateEx(tm);
        pch.epochnum = SmartxEpochTime.StrToStamp(pch.tmbegin) / SmartxEpochTime.MAIN_CHAIN_PERIOD;
        return SmartxEpochTime.EpochTime(SmartxEpochTime.StrToStamp(time));
    }
    public int CmpEpoch(long epoch) {
        return 0;
    }
}
