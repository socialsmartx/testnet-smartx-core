package com.smartx.core.consensus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import com.smartx.core.blockchain.DataBase;
import com.smartx.util.Tools;

public class SmartxEpochTime {
    private static final int BLOCK_REFER_TIME = 5;
    private static final int BLOCK_BROADCAST_TIME = 15;
    public static final int RULESIGN_TIME = 20;
    public static int G_STARTS[] = new int[3];
    public static int MAIN_CHAIN_PERIOD = 1000 * 30;    // 30s
    static {
        G_STARTS[0] = SmartxEpochTime.BLOCK_REFER_TIME;
        G_STARTS[1] = SmartxEpochTime.BLOCK_BROADCAST_TIME;
        G_STARTS[2] = SmartxEpochTime.RULESIGN_TIME;
    }
    public static long get_timestamp() {
        return System.currentTimeMillis();
    }
    public static String GetSystime() {
        return TimeStamp2DateEx(System.currentTimeMillis());
    }
    public static Long StrToStamp(String date_str) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long stamp = sdf.parse(date_str).getTime();
            return stamp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }
    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "").toUpperCase();
    }
    public static long CalTimeEpochNum(long stamp) {
        long epoch = SmartxEpochTime.EpochTime(stamp);
        return epoch - DataBase.genesisEpoch + 1;
    }
    public static long CalTimeEpoch(long stamp) {
        long epoch = SmartxEpochTime.EpochTime(stamp);
        return epoch;
    }
    @Test
    public static long GetEndTimeEpoch() {
        long tm = SmartxEpochTime.get_timestamp();
        long total = (EpochTime(tm) + 1) * SmartxEpochTime.MAIN_CHAIN_PERIOD;
        return total;
    }
    public static String GetEndTimeNum(long timenum) {
        long total = (timenum + 1 + DataBase.genesisEpoch) * SmartxEpochTime.MAIN_CHAIN_PERIOD;
        return TimeStamp2DateEx(total);
    }
    public static String GetBeginTimeNum(long timenum) {
        long total = (timenum + DataBase.genesisEpoch) * SmartxEpochTime.MAIN_CHAIN_PERIOD;
        return TimeStamp2DateEx(total);
    }
    public static long GetBeginTimeEpoch() {
        long tm = SmartxEpochTime.get_timestamp();
        long total = EpochTime(tm) * SmartxEpochTime.MAIN_CHAIN_PERIOD;
        return total;
    }
    public static void Sleep(long stamp) {
        try {
            Thread.sleep(stamp);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static long GetCurTimeNum() {
        long tm = get_timestamp();
        return CalTimeEpochNum(tm);
    }
    public static void main(String[] args) {
        /*System.out.println("start:" + SmartxEpochTime.TimeStamp2DateEx(GetBeginTimeEpoch()) +
                                   " now:" + SmartxEpochTime.GetSystime() + " end:" +
                                        SmartxEpochTime.TimeStamp2DateEx(GetEndTimeEpoch()));*/
        long tm = SmartxEpochTime.StrToStamp("2019-10-18 10:34:13");
        System.out.println(tm);
    }
    public static long EpochTime3(String date_str) {
        long stamp = StrToStamp(date_str);
        return EpochTime(stamp);
    }
    public static String EpochToTime(long epoch) {
        long tm = SmartxEpochTime.MAIN_CHAIN_PERIOD * epoch;
        // System.out.println(Tools.TimeStamp2DateEx(tm));
        return Tools.TimeStamp2DateEx(tm);
    }
    // MAIN_CHAIN_PERIOD
    public static int EpochTime2(String stamp) {
        try {
            int ts = Integer.parseInt(stamp);
            return ts / (SmartxEpochTime.MAIN_CHAIN_PERIOD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    // MAIN_CHAIN_PERIOD的单位是毫秒
    public static long EpochTime(Long stamp) {
        try {
            Long ts = stamp;
            return ts / (MAIN_CHAIN_PERIOD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }
    public static String TimeStamp2Date(String timestamp) {
        Long ts = Long.parseLong(timestamp) * 1000;
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ts));
        return date;
    }
    public static String TimeStamp2DateEx(Long timestamp) {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
        return date;
    }
}
