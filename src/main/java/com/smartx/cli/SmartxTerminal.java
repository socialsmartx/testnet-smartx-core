package com.smartx.cli;

import java.util.HashMap;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.smartx.core.blockchain.DataBase;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.blockchain.SatPeerManager;
import com.smartx.message.Message;
import com.smartx.util.Tools;

public class SmartxTerminal {
    public final String TERMINAL = "/terminal";
    public final String PROTOCOL = "http://";
    private Logger logger = Logger.getLogger("SmartxTerminal");
    public int Terminal() {
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.print("smartx>");
            String cmd = sc.nextLine();
            WriteCommand(cmd);
        }
    }
    public void DoTransfer(String cmd, String to, String amount) {
        if (!to.equals("") || !amount.equals("")) {
            SatPeerManager peer = SATObjFactory.GetPeerMgr();
            String rpcclient = PROTOCOL;
            ;
            rpcclient += DataBase.rpcclient;
            rpcclient += TERMINAL;
            Message message = new Message(cmd);
            message.args = new HashMap<String, String>();
            message.args.put("command", cmd);
            message.args.put("to", to);
            message.args.put("amount", amount);
            String json = peer.PutJsonCmd(rpcclient, Message.ToJson(message));
            json = Tools.getURLDecoderString(json);
            Message messret = Message.FromJson(json);
            if (messret != null && messret.args.get("ret").equals("0")) {
                String str = messret.args.get("res_info");
                System.out.println(str);
            }
        }
    }
    public void DoCommand(String cmd, String value) {
        if (cmd.equals("")) return;
        SatPeerManager peer = SATObjFactory.GetPeerMgr();
        String rpcclient = PROTOCOL;
        rpcclient += DataBase.rpcclient;
        rpcclient += TERMINAL;
        Message message = new Message(cmd);
        message.args = new HashMap<String, String>();
        message.args.put("command", cmd);
        message.args.put("value", value);
        String json = peer.PutJsonCmd(rpcclient, Message.ToJson(message));
        json = Tools.getURLDecoderString(json);
        Message messret = Message.FromJson(json);
        if (messret != null && messret.args.get("ret").equals("0")) {
            String str = messret.args.get("res_info");
            System.out.println(str);
        }
    }
    public void WriteCommand(String cmd) {
        if (cmd.equals("exit")) System.exit(0);
        else if (cmd.equals("help")) System.out.println(SmartxCommands.help());
        else {
            String[] strs = cmd.split("\\s+");
            String value = "";
            if (strs.length == 1) {
                DoCommand(strs[0], value);
            }
            if (strs.length == 2) {
                cmd = strs[0];
                value = strs[1];
                DoCommand(cmd, value);
            } else if (strs.length == 3 && strs[0].equals("xfer")) {
                DoTransfer(strs[0], strs[1], strs[2]);
            }
        }
    }
}
