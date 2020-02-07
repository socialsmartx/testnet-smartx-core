package com.smartx.cli;

import java.util.Scanner;

import org.apache.log4j.Logger;

public class Command {
    protected static Logger logger = Logger.getLogger(Command.class);
    public static String G_cmdurl = "";
    public void run() {
        Scanner sc = new Scanner(System.in);
        CmdHandler cmdhler = new CmdHandler();
        while (true) {
            System.out.print("smartx>");
            String value = sc.nextLine();
            try {
                if (1 == cmdhler.StartCmd(value)) {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
