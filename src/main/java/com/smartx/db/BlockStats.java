package com.smartx.db;

import com.smartx.block.Block;
import com.smartx.core.blockchain.SATObjFactory;

public class BlockStats {
    public static int TNum = 0;
    public static int Ntotal = 0;
    public static int MCs = 0;
    public static Block blk = null;
    public static Block top = null;
    public static Block mctop = null;
    public static void GetStats() {
        QueryDB querydb = SATObjFactory.GetQueryDB();
        querydb.ShowStats();
    }
    public static int GetNtotal(){ return Ntotal; }
    public static String GetTopHash() {
        return BlockStats.top != null ? BlockStats.top.header.hash : "";
    }
    public static String GetMctopHash() {
        return BlockStats.mctop != null ? BlockStats.mctop.header.hash : "";
    }
    public static void GetPointer() {
        top = SATObjFactory.GetMainTop().GetMCBlock();
        mctop = SATObjFactory.GetMainTop().GetMCBlock();
    }
}
