package com.smartx.wallet;

import java.io.File;
import java.io.FileInputStream;

import com.smartx.config.OSInfo;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

public class WalletCommand {
    private static final String myWalletDir;
    static {
        String path = System.getProperty("user.dir");
        if (OSInfo.isWindows()) {
            myWalletDir = path + "\\MyWallet\\";
        } else {
            myWalletDir = path + "/MyWallet/";
        }
    }
    public void create_account(String password) {
        SmartXWallet.createAccount(password, myWalletDir);
    }
    public void list_accounts() throws Exception {
        File file = new File(myWalletDir);
        File[] fs = file.listFiles();
        for (File f : fs) {
            if (!f.isDirectory() && f.getName().matches(".+\\.json")) {
                FileInputStream fileInputStream = new FileInputStream(f);
                byte[] keystoreByte = new byte[512];
                fileInputStream.read(keystoreByte);
                String keyStoreString = new String(keystoreByte);
                JSON keyStoreJson = JSONObject.fromObject(keyStoreString);
                String address = ((JSONObject) keyStoreJson).getString("address");
                System.out.println(address);
            }
        }
    }
}
