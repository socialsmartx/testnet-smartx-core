package com.smartx.config;

import org.junit.Test;

public class OSInfo {
    public static int main() {
        System.out.println(OSInfo.isWindows());
        return 0;
    }
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static OSInfo _instance = new OSInfo();
    @Test
    public void testos() {
        System.out.println(isWindows());
        System.out.println(isLinux());
        System.out.println(isMacOSX());
    }
    public OSInfo() {
    }
    public static boolean isLinux() {
        return OS.indexOf("linux") >= 0;
    }
    public static boolean isMacOS() {
        return OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") < 0;
    }
    public static boolean isMacOSX() {
        return OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") > 0;
    }
    public static boolean isWindows() {
        return OS.indexOf("windows") >= 0;
    }
}


