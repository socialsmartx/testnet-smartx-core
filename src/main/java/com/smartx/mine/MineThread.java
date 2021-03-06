package com.smartx.mine;

import java.util.HashMap;

import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.SatPeerManager;
import com.smartx.core.consensus.GeneralMine;
import com.smartx.core.consensus.SatException;
import com.smartx.crypto.Sha256;
import com.smartx.message.Message;

public class MineThread implements Runnable {
    public static boolean g_do_mining = true;
    public String find_nonce(String content, int start, int attempts, int step) {
        return "";
    }
    public long diffCur = 0;
    public String random = "";
    public String Miningblk;
    long start = 0;
    long end = 0;
    long taskid = 0;
    long height = 0;
    public String powerTotal = "";
    public void OnNewMineTask(String blk, long taskid, long height, long start, long end) {
        synchronized (this) {
            if (null == blk) return;
            Miningblk = blk;
            this.taskid = taskid;
            this.start = start;
            this.end = end;
            this.height = height;
            diffCur = 0;
            random = "";
        }
    }
    public void SetMineDiff(long diff, String hex, String temp_Miningblk) {
        if (Miningblk != null && temp_Miningblk != null && Miningblk.equals(temp_Miningblk))
            if (diffCur == 0 || MineHelper.cmpDiff(diff, diffCur)) {
                synchronized (this) {
                    diffCur = diff;
                    random = hex;
                }
            }
    }
    public void GetMineTask() throws SatException {
        {
            //if (start >= end || Miningblk==null)
            {
                // 如果异常 停止挖矿等待 OnNewMineTask才会继续
                Message message = new Message(Message.MESSAGE_GET_MINE_TASK);
                message.args = new HashMap<String, String>();
                message.args.put("height", String.valueOf(height));
                synchronized (this) {
                    message.args.put("random", random);
                }
                message.args.put("taskid", String.valueOf(taskid));
                message.args.put("address", Address);
                SatPeerManager client = new SatPeerManager();
                String url = PoolUrl;
                Message resp = client.QueryMessageV1(url, message);
                if (resp != null && resp.args.get("miningblk") != null) {
                    if (diffCur != 0) GeneralMine.minePowerOur.computingPower(MineHelper.DiffLong2String(diffCur));
                    height = Long.valueOf(resp.args.get("height"));
                    taskid = Long.valueOf(resp.args.get("taskid"));
                    start = Long.valueOf(resp.args.get("start"));
                    end = Long.valueOf(resp.args.get("end"));
                    powerTotal = resp.args.get("powerTotal");
                    synchronized (this) {
                        Miningblk = resp.args.get("miningblk");
                        diffCur = 0;
                    }
                    System.out.println("taskid start: " + start + " end:" + end);
                    System.out.println("next task Miningblk: " + Miningblk);
                }
            }
        }
    }
    public void run() {
        String temp_Miningblk = null;
        long temp_diffCur = 0;
        String rtemp_andom = "";
        long lasttime = 0;
        while (true == g_do_mining) {
            try {
                if (true == g_do_mining) {
                    long time = System.currentTimeMillis();
                    if (time - lasttime > 1000) {
                        lasttime = time;
                        //synchronized (this)
                        {
                            SetMineDiff(temp_diffCur, rtemp_andom, temp_Miningblk);
                            temp_Miningblk = Miningblk;
                            rtemp_andom = "";
                            temp_diffCur = 0;
                        }
                    }
                    if (temp_Miningblk != null && !temp_Miningblk.equals("")) {
                        java.util.Random temp = new java.util.Random(System.currentTimeMillis());
                        long maxValue = Long.MAX_VALUE;
                        long value = (long) (temp.nextLong() % maxValue);
                        String hex = Long.toHexString(value);
                        String hash = Sha256.getH256(temp_Miningblk + hex);
                        long diff = MineHelper.getHashDiff(hash);
                        if (temp_diffCur == 0 || MineHelper.cmpDiff(diff, temp_diffCur)) {
                            synchronized (this) {
                                temp_diffCur = diff;
                                rtemp_andom = hex;
                            }
                        }
                    } else {
                        Thread.sleep(10);
                    }
                } else {
                    Thread.sleep(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public String Address = "";
    public String PoolUrl = "127.0.0.1:8001";
    public static void main(String[] args) {
        System.out.println("cmd format: smartx.exe -address:your_address -poolurl:ip:port -threads:1");
        MineThread mineThread = SATObjFactory.GetMineThread();
        int nThreads = Runtime.getRuntime().availableProcessors() * 2 - 1;
        for (String str : args) {
            str = str.replace(" ", "");
            str = str.replace("	", "");
            str = str.replace("-", "");
            str = str.replace("0x", "");
            if (str.indexOf("address") == 0) {
                mineThread.Address = str.replace("address:", "");
            } else if (str.indexOf("poolurl") == 0) {
                mineThread.PoolUrl = str.replace("poolurl:", "");
            } else if (str.indexOf("threads") == 0) {
                nThreads = Integer.valueOf(str.replace("threads:", ""));
            }
            System.out.println(str);
        }
        System.out.println("mining threads: " + nThreads);
        for (int i = 0; i < nThreads; i++) {
            Thread thread = new Thread(mineThread, "MineThread");
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        Runtime run = Runtime.getRuntime();//当前 Java 应用程序相关的运行时对象。
        run.addShutdownHook(new Thread() { //注册新的虚拟机来关闭钩子
            @Override
            public void run() {
                //程序结束时进行的操作
                g_do_mining = false;
            }
        });
        for (; ; ) {
            long tm = System.currentTimeMillis();
            //synchronized(mineThread)
            {
                String info = String.format("cur height: %d      taskid: %d      random:%s      diff:%s      4hr hashrate: %s of %s", mineThread.height, mineThread.end, mineThread.random, MineHelper.DiffLong2String(mineThread.diffCur), GeneralMine.minePowerOur.GetPower(), mineThread.powerTotal);
                System.out.println(info);
            }
            try {
                mineThread.GetMineTask();
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
