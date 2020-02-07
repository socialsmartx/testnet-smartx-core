package com.smartx.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.smartx.core.consensus.SmartxEpochTime;

// 排序类
public class SortTimeForUnit implements Comparator {
    public static void main(String[] args) throws InterruptedException {
        // TODO Auto-generated method stub
        ArrayList<TxBlock> blks = new ArrayList<TxBlock>();
        TxBlock blk1 = new TxBlock();
        blk1.timestamp = SmartxEpochTime.StrToStamp("2019-04-16 10:30:11");
        blks.add(blk1);
        Thread.sleep(100);
        TxBlock blk2 = new TxBlock();
        blk2.timestamp = SmartxEpochTime.StrToStamp("2019-04-16 10:31:11");
        blks.add(blk2);
        Thread.sleep(100);
        TxBlock blk3 = new TxBlock();
        blk3.timestamp = SmartxEpochTime.StrToStamp("2019-04-15 11:31:11");
        blks.add(blk3);
        Collections.sort(blks, new SortTimeForUnit());
        for (int i = 0; i < blks.size(); i++) {
            System.out.println("times: " + blks.get(i).timestamp);
        }
    }
    // 时间从小到大排序
    public int compare(Object o1, Object o2) {
        TxBlock s1 = (TxBlock) o1;
        TxBlock s2 = (TxBlock) o2;
        long a1 = s1.timestamp;
        long a2 = s2.timestamp;
        if (a1 > a2) {
            return -1;
        } else if (a1 == a2) {
            return 0;
        } else {
            return 1;
        }
    }
}
