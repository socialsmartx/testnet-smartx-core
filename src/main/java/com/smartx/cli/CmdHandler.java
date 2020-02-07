package com.smartx.cli;

import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;

import com.smartx.core.blockchain.DataBase;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.consensus.GeneralMine;
import com.smartx.core.consensus.SatException;
import com.smartx.core.ledger.GetHeightEvent;
import com.smartx.event.PubSub;
import com.smartx.event.PubSubEvent;
import com.smartx.event.PubSubFactory;
import com.smartx.event.PubSubSubscriber;
import com.smartx.message.Message;

public class CmdHandler implements PubSubSubscriber {
    public SmartxCommands commandexcute = SATObjFactory.GetCommand();
    private static final PubSub pubSub = PubSubFactory.getDefault();
    private static long storageheight = 0;
    public void CmdHandler() {
        pubSub.subscribe(this, GetHeightEvent.class);
    }
    public void onPubSubEvent(PubSubEvent event) {
        String json = event.GetMessage();
        Message message = Message.FromJson(json);
        if (message.args.get("command").equals(Message.MESSAGE_STORAGE_LATESTHEIGHT)) {
            storageheight = Long.parseLong(message.args.get("height"));
        }
    }
    public int StartCmd(String value) throws SatException, SQLException {
        if (value.equals("stats")) {
            System.out.println(SmartxCommands.ShowStats());
        } else if (value.equals("mining")) {
            GeneralMine.g_stop_general_mining = !GeneralMine.g_stop_general_mining;
        } else if (value.equals("state")) {
            System.out.println(SmartxCommands.Status());
        } else if (value.contains("block")) {
            String[] strs = value.split("\\s+");
            if (strs.length > 1) {
                DataBase.ShowBlock(StringUtils.trim(strs[1]));
            }
        } else if (value.contains("xfer")) {
            String[] strs = value.split("\\s+");
            if (strs.length != 3) {
                return 1;
            }
            String amount = StringUtils.trim(strs[2]);
            String to = StringUtils.trim(strs[1]);
            SmartxCommands.Transfer(to, amount);
        } else if (value.contains("balance")) {
            SmartxCommands.ShowBalance();
        } else if (value.contains("getcoinbase")) {
            System.out.println(SmartxCommands.GetCoinBase());
        } else if (value.contains("setcoinbase")) {
            String[] strs = value.split("\\s+");
            if (strs.length != 2) {
                return 1;
            }
            SmartxCommands.SetCoinBase(strs[1]);
        } else if (value.contains("account")) {
            SmartxCommands.list_accounts();
        } else if (value.equals("create")) {
            commandexcute.CreateAccount(value);
        } else if (value.contains("mcheight")) {
            String[] strs = value.split("\\s+");
            if (strs.length != 2) {
                return 1;
            }
            commandexcute.ShowMcBlock(Long.parseLong(strs[1]));
        } else if (value.contains("main")) {
            commandexcute.SortBlock("5");
        } else if (value.contains("sort")) {
            String len = "0";
            String[] strs = value.split("\\s+");
            if (strs.length > 1) {
                len = strs[1];
            }
            SmartxCommands.SortBlockFront(len);
        } else if (value.equals("net")) {
            System.out.println(SmartxCommands.shownet());
        } else if (value.contains("exit")) {
            System.exit(0);
        } else if (value.contains("help")) {
            System.out.println(SmartxCommands.help());
        }
        return 0;
    }
}
