package com.smartx.mine;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class BlockMine {
    private final static Logger logger = Logger.getLogger("blockchain");
    private static long executeTime = 0;
    public BlockMine() {
        ScheduledExecutorService mcBlockExecutor = Executors.newSingleThreadScheduledExecutor();
        mcBlockExecutor.scheduleWithFixedDelay(() -> {
            //logger.info("blockchain excutor run for the {} time",executeTime++);
        }, 1, 5, TimeUnit.SECONDS);
    }
    //    public  void addBlockTask(BlockTask blockTask){
    //    }
    //
    //    private void sendMainBlock(BlockTask blockTask){
    //        if(blockTask != null){
    //            blockTask.execute();
    //        }
    //    }
}
