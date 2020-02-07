package com.smartx.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.junit.Test;

import com.smartx.block.Account;
import com.smartx.block.Block;

// 排序类
public class SortByChar implements Comparator {
    @Test
    public void TestSortByChar() throws InterruptedException {
        // TODO Auto-generated method stub
        ArrayList<Block> blks = new ArrayList<Block>();
        Block blk1 = new Block();
        blk1.header.timestamp = 10000;
        blks.add(blk1);
        Thread.sleep(100);
        Block blk2 = new Block();
        blk2.header.timestamp = 99;
        blks.add(blk2);
        Thread.sleep(100);
        Block blk3 = new Block();
        blk3.header.timestamp = 11;
        blks.add(blk3);
        Collections.sort(blks, new SortByChar());
        for (int i = 0; i < blks.size(); i++) {
            System.out.println("times: " + blks.get(i).header.timestamp);
        }
    }
    // 时间从小到大排序
    public int compare(Object o1, Object o2) {
        Account s1 = (Account) o1;
        Account s2 = (Account) o2;
        return s1.address.compareTo(s2.address);
    }
}
