package com.smartx.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.smartx.block.Field;

// 排序类
public class SortByTimeForField implements Comparator {
    public static void main(String[] args) throws InterruptedException {
        // TODO Auto-generated method stub
        ArrayList<Field> blks = new ArrayList<Field>();
        Field fld1 = new Field();
        fld1.time = "2019-05-01 16:56:00";
        fld1.hash = "aaa";
        blks.add(fld1);
        Field blk2 = new Field();
        blk2.time = "2018-05-01 16:55:01";
        blk2.hash = "dd";
        blks.add(blk2);
        Field blk3 = new Field();
        blk3.time = "2018-05-01 16:55:01";
        blk3.hash = "ccc";
        blks.add(blk3);
        Collections.sort(blks, new SortByTimeForField());
        for (int i = 0; i < blks.size(); i++) {
            System.out.println("times: " + blks.get(i).time + " " + blks.get(i).hash);
        }
    }
    // 时间从小到大排序
    public int compare(Object o1, Object o2) {
        Field s1 = (Field) o1;
        Field s2 = (Field) o2;
        if (Long.parseLong(Tools.DateToStamp(s1.time)) > Long.parseLong(Tools.DateToStamp(s2.time))) return 1;
        else if (Long.parseLong(Tools.DateToStamp(s1.time)) == Long.parseLong(Tools.DateToStamp(s2.time))) {
            // 如果时间一样计算哈希 哈希先入库 必须保证哈希不能重复
            return s1.hash.compareTo(s2.hash);
        } else return -1;
    }
}
