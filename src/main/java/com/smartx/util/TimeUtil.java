package com.smartx.util;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.log4j.Logger;

/**
 @author Mikhail Kalinin
 @since 10.08.2015 */
public class TimeUtil {
    private static final Logger logger = Logger.getLogger("timer");
    public static final String DEFAULT_DURATION_FORMAT = "%02d:%02d:%02d";
    public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String NTP_POOL = "pool.ntp.org";
    private static final int TIME_RETRIES = 5;
    private static long timeOffsetFromNtp = 0;
    private static final ThreadFactory factory = new ThreadFactory() {
        private AtomicInteger cnt = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ntpUpdate-" + cnt.getAndIncrement());
        }
    };
    private static final ScheduledExecutorService ntpUpdateTimer = Executors.newSingleThreadScheduledExecutor(factory);
    /**
     Converts minutes to millis

     @param minutes time in minutes
     @return corresponding millis value
     */
    public static long minutesToMillis(long minutes) {
        return minutes * 60 * 1000;
    }
    /**
     Converts seconds to millis

     @param seconds time in seconds
     @return corresponding millis value
     */
    public static long secondsToMillis(long seconds) {
        return seconds * 1000;
    }
    /**
     Converts millis to minutes

     @param millis time in millis
     @return time in minutes
     */
    public static long millisToMinutes(long millis) {
        return Math.round(millis / 60.0 / 1000.0);
    }
    /**
     Converts millis to seconds

     @param millis time in millis
     @return time in seconds
     */
    public static long millisToSeconds(long millis) {
        return Math.round(millis / 1000.0);
    }
    /**
     Returns timestamp in the future after some millis passed from now

     @param millis millis count
     @return future timestamp
     */
    public static long timeAfterMillis(long millis) {
        return System.currentTimeMillis() + millis;
    }
    /**
     开始周期性更新NTP时间，每小时更新一次
     */
    public static void startNtpProcess() {
        updateNetworkTimeOffset();
        ntpUpdateTimer.scheduleAtFixedRate(TimeUtil::updateNetworkTimeOffset, 60 * 60 * 1000, 60, TimeUnit.MINUTES);
    }
    /**
     将持续时间格式化为00:00:00的格式
     */
    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        return String.format(DEFAULT_DURATION_FORMAT, seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
    }
    /**
     将long类型的时间戳格式化成yyyy-MM-dd HH:mm:ss格式
     */
    public static String formatTimestamp(Long timestamp) {
        return new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT).format(new Date(timestamp));
    }
    /**
     以yyyy-MM-dd HH:mm:ss格式展示当前时间
     */
    public static String getCurrentTimeFormat() {
        return new SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT).format(new Date(System.currentTimeMillis()));
    }
    public static long currentTimeMillis() {
        return System.currentTimeMillis() + timeOffsetFromNtp;
    }
    /**
     从NTP更新时间偏移
     */
    private static void updateNetworkTimeOffset() {
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(10000);
        for (int i = 0; i < TIME_RETRIES; i++) {
            try {
                client.open();
                InetAddress hostAddr = InetAddress.getByName(NTP_POOL);
                TimeInfo info = client.getTime(hostAddr);
                info.computeDetails();
                // update our current internal state
                timeOffsetFromNtp = info.getOffset();
                // break from retry loop
                return;
            } catch (IOException e) {
                logger.warn("Unable to retrieve NTP time");
            } finally {
                client.close();
            }
        }
    }
    /**
     返回本地时间跟NTP服务器时间的时间偏移
     */
    public static long getTimeOffsetFromNtp() {
        return timeOffsetFromNtp;
    }
    /**
     关闭NTP时间更新
     */
    public static void shutdownNtpUpdater() {
        ntpUpdateTimer.shutdownNow();
    }
    private TimeUtil() {
    }
}
