package com.smartx.mine;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.block.Block;
import com.smartx.core.SmartxCore;
import com.smartx.core.consensus.GeneralMine;
import com.smartx.core.consensus.SatException;

public class MineHelper {
    public static long getHashDiff(String hash) {
        BigInteger value = new BigInteger(hash, 16);
        BigInteger maxValue = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
        BigInteger valueFFFF = new BigInteger("7fffffffffffffff", 16);
        // 0x7fffffffffffffff - 0x7fffffffffffffff* (  value / maxValue  )
        BigInteger value1 = valueFFFF.subtract(valueFFFF.multiply(value).divide(maxValue));
        return value1.longValue();
    }
    public static String DiffLong2String(long ldiff) {
        String sdiff = Long.toHexString(ldiff);
        // 位数不足补0
        while (sdiff.length() < 16) {
            sdiff = "0" + sdiff;
        }
        return sdiff;
    }
    // 难度值比较
    public static boolean cmpDiff(long diff1, long diff2) {
        return diff1 > diff2;
    }
    // hash值难度比较
    public static boolean cmpHashDiff(String hash1, String hash2) {
        long ldiff1 = getHashDiff(hash1);
        long ldiff2 = getHashDiff(hash2);
        return ldiff1 > ldiff2;
    }
    // 难度值比较
    public static boolean cmpDiff(String diff1, String diff2) {
        long ldiff1 = Long.parseLong(diff1, 16);
        long ldiff2 = Long.parseLong(diff2, 16);
        return ldiff1 > ldiff2;
    }
    public static double computingPower(String diff) {
        if (diff == null) return 0;
        // 位数不足补0
        while (diff.length() < 16) diff = "0" + diff;
        double power = 1;
        char[] array = diff.toCharArray();
        // 第一位
        for (int i = 0; i < 1; i++) {
            double value = Long.parseLong("" + array[i], 16);
            value = 16f / (16f - ((value) * 2 + 1));
            power = (double) (power * value);
        }
        boolean isF = true; // 是否连续F
        for (int i = 1; i < array.length; i++) {
            double value = Long.parseLong("" + array[i], 16);
            value = 16f / (16f - value);
            power = (double) (power * value);
            if (!isF) break;
            isF = array[i] == 'f' || array[i] == 'F';
        }
        return power * 64;
    }
    public static String GetPowerCompany(double power) {
        int place = 0;
        double value = power;
        while ((value / 1000) > 1) {
            value = value / 1000;
            place = place + 1;
        }
        String company = "";
        if (place == 1) company = "K";
        else if (place == 2) company = "M";
        else if (place == 3) company = "G";
        else if (place == 4) company = "T";
        else if (place == 5) company = "P";
        else if (place == 6) company = "E";
        return String.format("%1.2f%s", value, company);
    }
    static public class MinePower {
        List<Double> diffs = new ArrayList<>();
        double difftotal = 0l;
        public void computingPower(Block blk) {
            if (blk == null) return;
            computingPower(blk.diff);
        }
        public void computingPower(String tempdiff) {
            if (tempdiff == null && tempdiff.equals("")) return;
            double power = MineHelper.computingPower(tempdiff);
            if (difftotal != 0) {
                //power = Math.min(difftotal * 10, power);
                //power = Math.max(difftotal * 0.1, power);
            }
            diffs.add(power);
            if (diffs.size() > 480) // 24*4*2 4hr
                diffs.remove(0);
            for (int i = 0; i < diffs.size(); i++) {
                difftotal = difftotal + diffs.get(i);
            }
            difftotal = difftotal / diffs.size();
        }
        public String GetPower() {
            return MineHelper.GetPowerCompany(difftotal);
        }
    }
    public static void main(String[] args) throws SatException, SQLException {
        org.apache.log4j.Logger log = Logger.getLogger(GeneralMine.class);
        //		String tmp1 = computingPower( "7fffffffffffffff" ) ;
        //		String tmp2 = computingPower( "790b62e18846a8d5" ) ;
        //		String tmp3 = computingPower( "6a39e2e05f6da0f5" ) ;
        //		String tmp4 = computingPower( "6af36b0ba417913e" ) ;
        //		String tmp5 = computingPower( "000fffffffff9fc7" ) ;
        //		String tmp6 = computingPower( "1af528c85de71c4d" ) ;
        //		String tmp7 = computingPower( "1527227ce4daa3fc" ) ;
        //		String tmp8 = computingPower( "ffda21c44c59a80d" ) ;
        //		String tmp10 = computingPower( "7ffffffffffffff" ) ;
        //		String tmp11 = computingPower( "0fffffffffffff" ) ;
        SmartxCore core = new SmartxCore();
        core.InitStorage();
        core.InitGenesisEpoch();
        core.ReadConfig();
        core.ReadAccounts();
        PoolThread pool = new PoolThread();
        Block blk = GeneralMine.CreateMainBlock();
        List<Double> diffs = new ArrayList<>();
        double difftotal = 0l;
        for (; ; ) {
            long tm = System.currentTimeMillis();
            pool.doMiningWork(blk);
            for (int i = 0; i < 10000000; i++) {
                pool.mining();
            }
            tm = (System.currentTimeMillis() - tm) / 1000;
            blk.header.random = pool.getMiningWork(blk);
            double diff = computingPower(blk.diff);
            String tmp12 = GetPowerCompany(diff);
            //log.info("1M算力 diff: " + blk.diff + "估算: " + tmp12);
            if (difftotal != 0) {
                diff = Math.min(difftotal * 10, diff);
                diff = Math.max(difftotal * 0.1, diff);
            }
            diffs.add(diff);
            if (diffs.size() > 96) diffs.remove(0);
            for (int i = 0; i < diffs.size(); i++) {
                difftotal = difftotal + diffs.get(i);
            }
            difftotal = difftotal / diffs.size();
            log.info("10M算力 48周期平均算力: " + GetPowerCompany(difftotal));
        }
    }
}
