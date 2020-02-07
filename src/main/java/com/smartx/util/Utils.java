package com.smartx.util;

import static java.lang.Math.min;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;

import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;

public class Utils {
    //    private static final DataWord DIVISOR = DataWord.of(64);
    private static SecureRandom random = new SecureRandom();
    /**
     @param number should be in form '0x34fabd34....'
     @return String
     */
    public static BigInteger unifiedNumericToBigInteger(String number) {
        boolean match = Pattern.matches("0[xX][0-9a-fA-F]+", number);
        if (!match) return (new BigInteger(number));
        else {
            number = number.substring(2);
            number = number.length() % 2 != 0 ? "0".concat(number) : number;
            byte[] numberBytes = Hex.decode(number);
            return (new BigInteger(1, numberBytes));
        }
    }
    /**
     Return formatted Date String: yyyy.MM.dd HH:mm:ss
     Based on Unix's time() input in seconds

     @param timestamp seconds since start of Unix-time
     @return String formatted as - yyyy.MM.dd HH:mm:ss
     */
    public static String longToDateTime(long timestamp) {
        Date date = new Date(timestamp * 1000);
        DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return formatter.format(date);
    }
    public static String longToTimePeriod(long msec) {
        if (msec < 1000) return msec + "ms";
        if (msec < 3000) return String.format("%.2fs", msec / 1000d);
        if (msec < 60 * 1000) return (msec / 1000) + "s";
        long sec = msec / 1000;
        if (sec < 5 * 60) return (sec / 60) + "m" + (sec % 60) + "s";
        long min = sec / 60;
        if (min < 60) return min + "m";
        long hour = min / 60;
        if (min < 24 * 60) return hour + "h" + (min % 60) + "m";
        long day = hour / 24;
        return day + "d" + (hour % 24) + "h";
    }
    public static ImageIcon getImageIcon(String resource) {
        URL imageURL = ClassLoader.getSystemResource(resource);
        ImageIcon image = new ImageIcon(imageURL);
        return image;
    }
    static BigInteger _1000_ = new BigInteger("1000");
    public static String getValueShortString(BigInteger number) {
        BigInteger result = number;
        int pow = 0;
        while (result.compareTo(_1000_) == 1 || result.compareTo(_1000_) == 0) {
            result = result.divide(_1000_);
            pow += 3;
        }
        return result.toString() + "\u00b7(" + "10^" + pow + ")";
    }
    /**
     Decodes a hex string to address bytes and checks validity

     @param hex - a hex string of the address, e.g., 6c386a4b26f73c802f34673f7248bb118f97424a
     @return - decode and validated address byte[]
     */
    public static byte[] addressStringToBytes(String hex) {
        final byte[] addr;
        try {
            addr = Hex.decode(hex);
        } catch (DecoderException addressIsNotValid) {
            return null;
        }
        if (isValidAddress(addr)) return addr;
        return null;
    }
    public static boolean isValidAddress(byte[] addr) {
        return addr != null && addr.length == 20;
    }
    /**
     @param addr length should be 20
     @return short string represent 1f21c...
     */
    public static String getAddressShortString(byte[] addr) {
        if (!isValidAddress(addr)) throw new Error("not an address");
        String addrShort = Hex.toHexString(addr, 0, 3);
        StringBuffer sb = new StringBuffer();
        sb.append(addrShort);
        sb.append("...");
        return sb.toString();
    }
    public static SecureRandom getRandom() {
        return random;
    }
    public static double JAVA_VERSION = getJavaVersion();
    static double getJavaVersion() {
        String version = System.getProperty("java.version");
        // on android this property equals to 0
        if (version.equals("0")) return 0;
        int pos = 0, count = 0;
        for (; pos < version.length() && count < 2; pos++) {
            if (version.charAt(pos) == '.') count++;
        }
        return Double.parseDouble(version.substring(0, pos - 1));
    }
    public static String getHashListShort(List<byte[]> blockHashes) {
        if (blockHashes.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        String firstHash = Hex.toHexString(blockHashes.get(0));
        String lastHash = Hex.toHexString(blockHashes.get(blockHashes.size() - 1));
        return sb.append(" ").append(firstHash).append("...").append(lastHash).toString();
    }
    public static String getNodeIdShort(String nodeId) {
        return nodeId == null ? "<null>" : nodeId.substring(0, 8);
    }
    public static long toUnixTime(long javaTime) {
        return javaTime / 1000;
    }
    public static long fromUnixTime(long unixTime) {
        return unixTime * 1000;
    }
    public static <T> T[] mergeArrays(T[]... arr) {
        int size = 0;
        for (T[] ts : arr) {
            size += ts.length;
        }
        T[] ret = (T[]) Array.newInstance(arr[0].getClass().getComponentType(), size);
        int off = 0;
        for (T[] ts : arr) {
            System.arraycopy(ts, 0, ret, off, ts.length);
            off += ts.length;
        }
        return ret;
    }
    public static String align(String s, char fillChar, int targetLen, boolean alignRight) {
        if (targetLen <= s.length()) return s;
        String alignString = repeat("" + fillChar, targetLen - s.length());
        return alignRight ? alignString + s : s + alignString;
    }
    public static String repeat(String s, int n) {
        if (s.length() == 1) {
            byte[] bb = new byte[n];
            Arrays.fill(bb, s.getBytes()[0]);
            return new String(bb);
        } else {
            StringBuilder ret = new StringBuilder();
            for (int i = 0; i < n; i++) ret.append(s);
            return ret.toString();
        }
    }
    //    public static List<ByteArrayWrapper> dumpKeys(DbSource<byte[]> ds) {
    //
    //        ArrayList<ByteArrayWrapper> keys = new ArrayList<>();
    //
    //        for (byte[] key : ds.keys()) {
    //            keys.add(ByteUtil.wrap(key));
    //        }
    //        Collections.sort(keys);
    //        return keys;
    //    }
    //
    //    public static DataWord allButOne64th(DataWord dw) {
    //        DataWord divResult = dw.div(DIVISOR);
    //        return dw.sub(divResult);
    //    }
    /**
     Show std err messages in red and throw RuntimeException to stop execution.
     */
    public static void showErrorAndExit(String message, String... messages) {
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";
        System.err.println(ANSI_RED);
        System.err.println("");
        System.err.println("        " + message);
        for (String msg : messages) {
            System.err.println("        " + msg);
        }
        System.err.println("");
        System.err.println(ANSI_RESET);
        throw new RuntimeException(message);
    }
    /**
     Show std warning messages in red.
     */
    public static void showWarn(String message, String... messages) {
        final String ANSI_RED = "\u001B[31m";
        final String ANSI_RESET = "\u001B[0m";
        System.err.println(ANSI_RED);
        System.err.println("");
        System.err.println("        " + message);
        for (String msg : messages) {
            System.err.println("        " + msg);
        }
        System.err.println("");
        System.err.println(ANSI_RESET);
    }
    public static String sizeToStr(long size) {
        if (size < 2 * (1L << 10)) return size + "b";
        if (size < 2 * (1L << 20)) return String.format("%dKb", size / (1L << 10));
        if (size < 2 * (1L << 30)) return String.format("%dMb", size / (1L << 20));
        return String.format("%dGb", size / (1L << 30));
    }
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    public static boolean isHexEncoded(String value) {
        if (value == null) return false;
        if ("".equals(value)) return true;
        try {
            //noinspection ResultOfMethodCallIgnored
            new BigInteger(value, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    //    public static class ChunkConsumer extends LinkedBlockingQueue<Chunk> {
    //        ChunkStore destination;
    //        boolean synchronous = true;
    //
    //        public ChunkConsumer(ChunkStore destination) {
    //            this.destination = destination;
    //        }
    //
    //        @Override
    //        public boolean add(Chunk chunk) {
    //            if (synchronous) {
    //                destination.put(chunk);
    //                return true;
    //            } else {
    //                return super.add(chunk);
    //            }
    //        }
    //    }
    public static class ArrayReader implements SectionReader {
        byte[] arr;
        public ArrayReader(byte[] arr) {
            this.arr = arr;
        }
        @Override
        public long seek(long offset, int whence) {
            throw new RuntimeException("Not implemented");
        }
        @Override
        public int read(byte[] dest, int destOff) {
            return readAt(dest, destOff, 0);
        }
        @Override
        public int readAt(byte[] dest, int destOff, long readerOffset) {
            int len = min(dest.length - destOff, arr.length - (int) readerOffset);
            System.arraycopy(arr, (int) readerOffset, dest, destOff, len);
            return len;
        }
        @Override
        public long getSize() {
            return arr.length;
        }
    }
    // for testing purposes when the timer might be changed
    // to manage current time according to test scenarios
    public static Timer TIMER = new Timer();
    public static class Timer {
        public long curTime() {
            return System.currentTimeMillis();
        }
    }
    public static String getCommonPrefix(String s1, String s2) {
        int pos = 0;
        while (pos < s1.length() && pos < s2.length() && s1.charAt(pos) == s2.charAt(pos)) pos++;
        return s1.substring(0, pos);
    }
    public static String ipBytesToString(byte[] ipAddr) {
        StringBuilder sip = new StringBuilder();
        for (int i = 0; i < ipAddr.length; i++) {
            sip.append(i == 0 ? "" : ".").append(0xFF & ipAddr[i]);
        }
        return sip.toString();
    }
    //    public static <P extends StringTrie.TrieNode<P>> String dumpTree(P n) {
    //        return dumpTree(n, 0);
    //    }
    //
    //    private static <P extends StringTrie.TrieNode<P>> String dumpTree(P n, int indent) {
    //        String ret = Utils.repeat("  ", indent) + "[" + n.path + "] " + n + "\n";
    //        for (P c: n.getChildren()) {
    //            ret += dumpTree(c, indent + 1);
    //        }
    //        return ret;
    //    }
    public static byte[] uInt16ToBytes(int uInt16) {
        return new byte[]{(byte) ((uInt16 >> 8) & 0xFF), (byte) (uInt16 & 0xFF)};
    }
    public static long curTime() {
        return TIMER.curTime();
    }
    public static byte[] rlpEncodeLong(long n) {
        // TODO for now leaving int cast
        return RLP.encodeInt((int) n);
    }
    public static byte rlpDecodeByte(RLPElement elem) {
        return (byte) rlpDecodeInt(elem);
    }
    public static long rlpDecodeLong(RLPElement elem) {
        return rlpDecodeInt(elem);
    }
    public static int rlpDecodeInt(RLPElement elem) {
        byte[] b = elem.getRLPData();
        if (b == null) return 0;
        return ByteUtil.byteArrayToInt(b);
    }
    public static String rlpDecodeString(RLPElement elem) {
        byte[] b = elem.getRLPData();
        if (b == null) return null;
        return new String(b);
    }
    public static byte[] rlpEncodeList(Object... elems) {
        byte[][] encodedElems = new byte[elems.length][];
        for (int i = 0; i < elems.length; i++) {
            if (elems[i] instanceof Byte) {
                encodedElems[i] = RLP.encodeByte((Byte) elems[i]);
            } else if (elems[i] instanceof Integer) {
                encodedElems[i] = RLP.encodeInt((Integer) elems[i]);
            } else if (elems[i] instanceof Long) {
                encodedElems[i] = rlpEncodeLong((Long) elems[i]);
            } else if (elems[i] instanceof String) {
                encodedElems[i] = RLP.encodeString((String) elems[i]);
            } else if (elems[i] instanceof byte[]) {
                encodedElems[i] = ((byte[]) elems[i]);
            } else {
                throw new RuntimeException("Unsupported object: " + elems[i]);
            }
        }
        return RLP.encodeList(encodedElems);
    }
    public static SectionReader stringToReader(String s) {
        return new ArrayReader(s.getBytes(StandardCharsets.UTF_8));
    }
    public static String readerToString(SectionReader sr) {
        byte[] bb = new byte[(int) sr.getSize()];
        sr.read(bb, 0);
        String s = new String(bb, StandardCharsets.UTF_8);
        return s;
    }
}
