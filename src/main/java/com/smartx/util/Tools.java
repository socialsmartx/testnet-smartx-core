package com.smartx.util;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.net.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smartx.block.Block;
import com.smartx.config.OSInfo;
import com.smartx.config.SystemProperties;
import com.smartx.core.blockchain.DataBase;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.consensus.SatException;
import com.smartx.core.coordinate.RuleSign;
import com.smartx.db.TransDB;

/**
 @author hyshen */
public final class Tools {
    public static final String messageXmlContentType = "application/xml; charset=utf-8";
    public static final String dateFormat = "yyyyMMdd";
    public static final String dateTimeFormat = "yyyyMMdd HH:mm:ss";
    public static final String dateTimeFormat1 = "yyyy-MM-dd HH:mm:ss";
    public static final String dateTimeFormat2 = "yyyy-MM-dd'T'HH:mm:ss";
    private static final Logger log = Logger.getLogger("core");
    private static SystemProperties Config = null;
    static {
        Config = SystemProperties.getDefault();
    }
    public static String getStackTrace() {
        final StringBuilder stackBuilder = new StringBuilder();
        final StackTraceElement[] stack = (new Throwable()).getStackTrace();
        ;
        for (int index = 0; index < stack.length; index++) {
            final StackTraceElement ste = stack[index];
            final String stInfo = String.format("at %s.<%s>(%s:%d)", ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber());
            stackBuilder.append(stInfo);
            stackBuilder.append("\r\n");
        }
        return stackBuilder.toString();
    }
    public static String addSuffix(final int i, String str) {
        str += "_";
        str += i;
        return str;
    }
    public static boolean isEmpty(final String hash) {
        if (hash != null && !hash.equals("")) {
            return false;
        }
        return true;
    }
    public static String trimNull(final String str) {
        if (str == null) {
            return "";
        }
        return str;
    }
    public static void getAllIP() throws SocketException {
        final Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        while (allNetInterfaces.hasMoreElements()) {
            final NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            System.out.println(netInterface.getName());
            final Enumeration addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                ip = (InetAddress) addresses.nextElement();
                if (ip != null && ip instanceof Inet4Address) {
                    System.out.println("本机的IP = " + ip.getHostAddress());
                }
            }
        }
    }
    public static String[] getAllLocalHostIP() {
        final List<String> res = new ArrayList<String>();
        final Enumeration netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (netInterfaces.hasMoreElements()) {
                final NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                final Enumeration nii = ni.getInetAddresses();
                while (nii.hasMoreElements()) {
                    ip = (InetAddress) nii.nextElement();
                    if (ip.getHostAddress().indexOf(":") == -1) {
                        res.add(ip.getHostAddress());
                        //System.out.println("本机的ip=" + ip.getHostAddress());
                    }
                }
            }
        } catch (final SocketException e) {
            e.printStackTrace();
        }
        return (String[]) res.toArray(new String[0]);
    }
    public static KeyStore loadKeyStore(final String type, final String keystoreFile, final String keystorePassword) throws SatException {
        FileInputStream is = null;
        try {
            final File file = new File(keystoreFile);
            is = new FileInputStream(file);
            final KeyStore keystore = KeyStore.getInstance(type);
            keystore.load(is, keystorePassword.toCharArray());
            return keystore;
        } catch (final FileNotFoundException e) {
            throw new SatException(String.format(" KeyStore ", keystoreFile, type), "error");
        } catch (final KeyStoreException e) {
            throw new SatException(String.format(" KeyStore ", keystoreFile, type), "error");
        } catch (final NoSuchAlgorithmException e) {
            throw new SatException(String.format(" KeyStore ", keystoreFile, type), "error");
        } catch (final CertificateException e) {
            throw new SatException(String.format(" KeyStore ", keystoreFile, type), "error");
        } catch (final IOException e) {
            throw new SatException(String.format(" KeyStore ", keystoreFile, type), "error");
        } finally {
            try {
                if (is != null) is.close();
            } catch (final IOException e) {
                // cannot do anything.
            }
        }
    }
    public static Map<String, String> formData2Dic(final String formData) {
        final Map<String, String> result = new HashMap<>();
        if (formData == null || formData.trim().length() == 0) {
            return result;
        }
        final String[] items = formData.split("&");
        Arrays.stream(items).forEach(item -> {
            final String[] keyAndVal = item.split("=");
            if (keyAndVal.length == 2) {
                try {
                    final String key = URLDecoder.decode(keyAndVal[0], "utf8");
                    final String val = URLDecoder.decode(keyAndVal[1], "utf8");
                    result.put(key, val);
                } catch (final UnsupportedEncodingException e) {
                }
            }
        });
        return result;
    }
    public static PublicKey readPublicKeyFromX509(final String certFile) throws SatException {
        final File file = new File(certFile);
        if (!file.exists()) {
            throw new SatException("1101", String.format("ָ%s", certFile));
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            final int length = fis.available();
            final byte[] buffer = new byte[length];
            int bytesRead = 0;
            while (bytesRead < length) {
                bytesRead += fis.read(buffer, bytesRead, length - bytesRead);
            }
            final ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            final CertificateFactory factory = CertificateFactory.getInstance("X.509");
            final Certificate cert = factory.generateCertificate(bais);
            return cert.getPublicKey();
        } catch (final FileNotFoundException e) {
            throw new SatException("1000", String.format("%s", certFile));
        } catch (final IOException e) {
            throw new SatException("1000", String.format("%s", certFile));
        } catch (final CertificateException e) {
            throw new SatException("1000", String.format("%s", certFile));
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (final IOException e) {
                    // cannot do anything
                }
            }
        }
    }
    public static byte[] Int2ByteArray(final int iSource) {
        final byte[] bLocalArr = new byte[4];
        for (int i = 0; i < 4; i++) {
            bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
        }
        return bLocalArr;
    }
    public static int ByteArray2Int(final byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;
        for (int i = 0; i < 4; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }
    /**
     byteת

     @param bArray
     @return
     */
    public static final String bytesToHexString1(final byte[] bArray) {
        final StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) sb.append(0);
            sb.append(sTemp.toLowerCase());
        }
        return sb.toString();
    }
    public static String fenToyuan(final long lfen) {
        final long lyuan = lfen / 100;
        final long lxiaoshu = lfen % 100;
        if (lxiaoshu < 10) {
            return lyuan + ".0" + lxiaoshu;
        } else {
            return lyuan + "." + lxiaoshu;
        }
    }
    public static long getYuanToFeni(final String yuan) {
        final BigDecimal bgdAmount = new BigDecimal(yuan);
        final BigDecimal bgdTemp = new BigDecimal("100.00");
        return bgdAmount.multiply(bgdTemp).longValue();
    }
    public static String getSystemEncode() {
        final String ecode = System.getProperty("file.encoding", "GBK");
        return ecode;
    }
    public static String getUUID() {
        final String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "").toUpperCase();
    }
    public static void addNode(final List<String> nodes, final String node) {
        nodes.add(node);
    }
    public static Block FromJson(final String json) {
        try {
            final Gson gson = new GsonBuilder().create();
            final Block blk = gson.fromJson(json, new TypeToken<Block>() {
            }.getType());
            return blk;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String ToJson(final Block blk) {
        final Gson gson = new GsonBuilder().create();
        return gson.toJson(blk);
    }
    public static int removeNode(final List<String> nodes, final String node) {
        final Iterator<String> it = nodes.iterator();
        int ret = 0;
        while (it.hasNext()) {
            final String str = it.next();
            if (str.equals(node)) {
                it.remove();
                ret = 1;
            }
        }
        return ret;
    }
    public static void main(final String[] args) throws ParseException {
        System.out.println(System.getProperty("os.name").toLowerCase());
        final String path = System.getProperty("user.dir");
        System.out.println(path);
        System.out.println(getProcessID());
        System.exit(0);
        final long tm = System.currentTimeMillis();
        System.out.println(Tools.TimeStamp2DateEx((tm)));
        Tools.gtthanTime("2019-03-31 22:25:30", 50);

    	/*System.out.println(getUUID());

    	String test1 = "测试";
    	System.out.println(getURLEncoderString(test1));
    	System.out.println(getURLDecoderString(getURLEncoderString(test1)));
    	*/
        final String[] strs = Tools.getAllLocalHostIP();
        for (int i = 0; i < strs.length; i++) {
            System.out.println(strs[i]);
        }
    }
    public static boolean isNumeric(final String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    public static String TimeStamp2Date(final String timestamp) {
        final Long ts = Long.parseLong(timestamp) * 1000;
        final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ts));
        return date;
    }
    public static String TimeStamp2DateEx(final Long timestamp) {
        final String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
        return date;
    }
    public static String getURLEncoderString(final String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLEncoder.encode(str, "utf-8");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static String getURLDecoderString(final String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLDecoder.decode(str, "utf-8");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static String ExcShell(final String cmd, final int buffsize) throws IOException {
        Process pid = null;
        pid = Runtime.getRuntime().exec(cmd);
        String info = "";
        if (pid != null) {
            final InputStreamReader reader = new InputStreamReader(pid.getInputStream());
            final BufferedReader buff = new BufferedReader(reader, buffsize);
            String line = null;
            while ((line = buff.readLine()) != null) {
                info += line;
            }
        } else {
            throw new IOException();
        }
        return info;
    }
    public static long CalTimeEpoch2Num(final long epoch) {
        return epoch - DataBase.genesisEpoch + 1;
    }
    public static String getYesDate(final Date dt) {
        final Calendar c = Calendar.getInstance();
        c.setTime(dt);
        final int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day - 1);
        final String dayBefore = new SimpleDateFormat("yyyyMMdd").format(c.getTime());
        return dayBefore;
    }
    public static void showremove(final List<String> hashs) {
        log.warn("remove:" + hashs.size());
        for (int i = 0; i < hashs.size(); i++)
            log.warn("  " + hashs.get(i));
    }
    public static void dispay(final String title, final Block blk) {
        String str = (title + " " + "hash:" + blk.header.hash + " num:" + blk.timenum + " height:" + blk.height + " time:" + blk.time + " type:" + blk.header.btype + " epoch:" + blk.epoch);
        if (blk.Flds.size() > 0) {
            str += " " + blk.Flds.get(0).hash;
        }
        System.out.println(str);
        log.debug(str);
    }
    public static String GetWalletPath() {
        String path = System.getProperty("user.dir");
        if (OSInfo.isWindows()) {
            path += "\\MyWallet\\UTC--2019-09-18T08.json";
        } else {
            path += "/MyWallet/UTC--2019-09-18T08.json";
        }
        return path;
    }
    public static String GetSortPath() {
        String path = System.getProperty("user.dir");
        if (OSInfo.isWindows()) {
            path += "\\logs\\sort.log";
        } else {
            path += "/logs/sort.log";
        }
        log.info(path);
        return path;
    }
    public static void dispay2(final String title, final Block blk) {
        final String str = (title + " " + "hash:" + blk.header.hash + " num:" + blk.timenum + " time:" + blk.time + " type:" + blk.header.btype + " epoch:" + blk.epoch);
        System.out.println(str);
        log.debug(str);
    }
    public static String GetYesDate(final String yesdate) {
        final SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        Date s_date = null;
        try {
            s_date = (Date) sf.parse(yesdate);
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return getYesDate(s_date);
    }
    public static boolean gtthanTime(final String cmptime, final int gtnum) throws ParseException {
        final Date dt = new Date();
        final String time1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dt.getTime());
        final long ntm1 = Long.parseLong(Tools.DateToStamp(time1));
        final long ntm2 = Long.parseLong(Tools.DateToStamp(cmptime));
        final int intval = 1000 * gtnum;
        final long ncmp = ntm1 - ntm2;
        if (ncmp > intval) return true;
        else return false;
    }
    public static void WriteFile(final String path, final String content) {
        try {
            final String data = content;
            final File file = new File(path);
            final BufferedWriter bw = null;
            if (!file.exists()) {
                file.createNewFile();
            }
            final FileWriter fileWritter = new FileWriter(file.getAbsoluteFile(), true);
            fileWritter.write(data);
            fileWritter.flush();
            fileWritter.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    public static String DateToStamp(final String s) {
        final String res;
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(s);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        final long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }
    public static boolean gtthanTime(final Date dt, final int gtnum) {
        final Calendar c = Calendar.getInstance();
        c.setTime(dt);
        final int day = c.get(Calendar.MINUTE);
        c.set(Calendar.MINUTE, day + gtnum);
        final String time1 = new SimpleDateFormat("yyyyMMdd").format(c.getTime());
        return true;
    }
    public static String getLastweekDate(final Date dt) {
        final Calendar c = Calendar.getInstance();
        c.setTime(dt);
        final int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day - 7);
        final String dayBefore = new SimpleDateFormat("yyyyMMdd").format(c.getTime());
        return dayBefore;
    }
    public static String ReadFile(final String path) {
        String strtmp = "";
        final BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(path), "utf-8"));
            String str = null;
            while ((str = in.readLine()) != null) {
                //System.out.println(str);
                strtmp += str;
            }
            in.close();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strtmp;
    }
    public static final int getProcessID() {
        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        System.out.println(runtimeMXBean.getName());
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
    }
    public static List<RuleSign> GetRuleSignListByJson(final String json) {
        try {
            final Gson gson = new GsonBuilder().create();
            final ArrayList<RuleSign> signs = gson.fromJson(json, new TypeToken<ArrayList<RuleSign>>() {
            }.getType());
            return signs;
        } catch (final Exception e) {
            e.printStackTrace();
            log.error("====>" + json);
            return null;
        }
    }
    public static String ToRuleSignListByList(final List<RuleSign> signs) {
        if (signs == null || signs.size() == 0) return "";
        String json = "";
        try {
            final Gson gson = new GsonBuilder().create();
            json = gson.toJson(signs);
            return json;
        } catch (final Exception e) {
            e.printStackTrace();
            log.error("====>" + json);
            return json;
        }
    }
    public static String GetLastWeekDate(final String yesdate) {
        final SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        Date s_date = null;
        try {
            s_date = (Date) sf.parse(yesdate);
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return getLastweekDate(s_date);
    }
    public static String getWeekday(final String date) {
        final SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat sdw = new SimpleDateFormat("E");
        Date d = null;
        try {
            d = sd.parse(date);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return sdw.format(d);
    }
    public static String ConvertYesDate(final String yesdate) {
        final String year = yesdate.substring(0, 4);
        final String month = yesdate.substring(4, 6);
        final String day = yesdate.substring(6, 8);
        return year + "-" + month + "-" + day;
    }
    public static void show3(final Block blk) throws SatException, SQLException {
        final TransDB smartxdb = SATObjFactory.GetTxDB();
        for (int i = 0; i < blk.Flds.size(); i++) {
            final Block tmpblk = smartxdb.GetBlock(blk.Flds.get(i).hash, DataBase.SMARTX_BLOCK_EPOCH);
            log.error(" hash:" + blk.Flds.get(i).hash + " type:" + tmpblk.header.btype);
        }
    }
    public static void show(final ArrayList<Block> m) {
        for (int i = 0; i < m.size(); i++) {
            final String resp = ("hash:" + m.get(i).header.hash + " height:" + m.get(i).height + " address:" + m.get(i).header.address + " rulelen:" + Tools.ToRuleSignListByList(m.get(i).ruleSigns).length() + " diff: " + m.get(i).diff + " num:" + m.get(i).timenum + " timestamp:" + m.get(i).header.timestamp + " btype:" + m.get(i).header.btype + " time:" + m.get(i).time + " epoch:" + m.get(i).epoch + " nodename:" + m.get(i).nodename);
            System.out.println(resp);
        }
    }
    public static void show2(final ArrayList<Block> m) {
        for (int i = 0; i < m.size(); i++) {
            dispay2("show", m.get(i));
        }
    }
}
