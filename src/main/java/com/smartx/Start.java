package com.smartx;

import java.util.ArrayList;
import java.util.List;

public class Start {
    private static final String CLI = "--cli";
    private static final String GUI = "--gui";
    public static void main(String[] args) {
        List<String> startArgs = new ArrayList<>();
        boolean startGui = false;
        for (String arg : args) {
            if (CLI.equals(arg)) {
                startGui = false;
            } else if (GUI.equals(arg)) {
                startGui = true;
            } else {
                startArgs.add(arg);
            }
        }
        SmartXCli.main(startArgs.toArray(new String[0]));
    }
}
