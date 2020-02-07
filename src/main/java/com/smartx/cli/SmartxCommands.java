package com.smartx.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.block.Account;
import com.smartx.block.Block;
import com.smartx.block.Field;
import com.smartx.config.OSInfo;
import com.smartx.config.SystemProperties;
import com.smartx.core.SmartxCore;
import com.smartx.core.blockchain.*;
import com.smartx.core.consensus.*;
import com.smartx.core.ledger.GetHeightEvent;
import com.smartx.crypto.Hash;
import com.smartx.crypto.Key;
import com.smartx.crypto.Sha256;
import com.smartx.db.AccountDB;
import com.smartx.db.BlockStats;
import com.smartx.db.QueryDB;
import com.smartx.event.PubSub;
import com.smartx.event.PubSubEvent;
import com.smartx.event.PubSubFactory;
import com.smartx.event.PubSubSubscriber;
import com.smartx.message.Message;
import com.smartx.net.Channel;
import com.smartx.util.Tools;
import com.smartx.wallet.Key25519;
import com.smartx.wallet.SmartXWallet;

import io.github.novacrypto.base58.Base58;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

public class SmartxCommands implements PubSubSubscriber {
    private static final Logger log = Logger.getLogger("Smartx");
    public static String cmd = "";
    private static final PubSub pubSub = PubSubFactory.getDefault();
    private static long syncheight = 1;
    private static long totalheight = 1;
    private static final String myWalletDir;
    public static long totalblocks = 0;
    public SmartxCommands() {
        pubSub.subscribe(this, GetHeightEvent.class);
    }
    static {
        String path = System.getProperty("user.dir");
        if (OSInfo.isWindows()) {
            myWalletDir = path + "\\MyWallet\\";
        } else {
            myWalletDir = path + "/MyWallet/";
        }
    }
    public void onPubSubEvent(PubSubEvent event) {
        String json = event.GetMessage();
        Message message = Message.FromJson(json);
        if (message.args.get("command").equals(Message.MESSAGE_GET_HEIGHT)) {
            syncheight = Long.parseLong(message.args.get("height"));
        } else if (message.args.get("command").equals(Message.MESSAGE_TOTAlBLOCKS)) {
            totalblocks = Long.parseLong(message.args.get("totalblocks"));
        } else if (message.args.get("command").equals(Message.MESSAGE_GET_LATESTHEIGHT)) {
            totalheight = Long.parseLong(message.args.get("height"));
        }
    }
    public static void Mining() {
        GeneralMine.g_stop_general_mining = !GeneralMine.g_stop_general_mining;
    }
    public static String Status() {
        String info = "";
        if (DataBase.G_Status.GetStatus() == SmartxStatus.STATUS.SMARTX_STATUS_INIT) {
            info = "normal";
        } else if (DataBase.G_Status.GetStatus() == SmartxStatus.STATUS.SMARTX_STATUS_LOADDB) {
            info = ("loading");
        } else if (DataBase.G_Status.GetStatus() == SmartxStatus.STATUS.SMARTX_STATUS_NORMAL) {
            info = ("normal");
        } else if (DataBase.G_Status.GetStatus() == SmartxStatus.STATUS.SMARTX_STATUS_MINING) {
            info = ("mining");
        } else if (DataBase.G_Status.GetStatus() == SmartxStatus.STATUS.SMARTX_STATUS_NETSYNC) {
            info = ("syncing");
        } else {
            System.out.println("unkonw state:" + DataBase.G_Status.GetStatus());
        }
        return info;
    }
    public static String ShowBalance() {
        QueryDB querydb = SATObjFactory.GetQueryDB();
        try {
            return querydb.ShowBalance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public static String GetCoinBase() {
        if (SmartXWallet.wallettype == SmartXWallet.SECP_WALLETTYPE) {
            return SmartxCore.G_Wallet.getAddress();
        } else {
            if (SmartxCore.G_Wallet.baseKey == null) System.out.println("");
            else return SmartxCore.G_Wallet.baseKey.toAddressString();
        }
        return "";
    }
    public static void SetCoinBase(String value) {
        if (SmartXWallet.wallettype == SmartXWallet.SECP_WALLETTYPE) {
            SmartXWallet wallet = SmartxCore.GetWallet(value);
            SmartxCore.SetWallet(wallet);
        } else {
            Key25519 wallet = SmartxCore.G_Wallet.fastkeys;
            for (int i = 0; i < wallet.getAccounts().size(); i++) {
                Key key = wallet.getAccount(i);
                if (value.equals(key.toAddressString())) {
                    SmartxCore.G_Wallet.baseKey = key;
                }
            }
        }
    }
    public void tested25519() throws SatException, SQLException, IOException {
        // setUp();
        File file = new File("c:\\www\\wallet.dat");
        Key25519 wallet = new Key25519(file, "mainnet");
        if (!wallet.unlock("123") || !wallet.flush()) {
            System.out.println("password error");
            return;
        }
        //wallet.addAccount(key);
        ///com.smartx.crypto.Key srckey = wallet.getAccount(0)
        com.smartx.crypto.Key srckey = wallet.addAccountRandom();
        String rawstr = "helloworld";
        byte[] rawhash = Hash.h256(rawstr.getBytes());
        System.out.println(srckey.toAddressString());
        String sigstr = Key25519.sign(rawstr, srckey);//Base58.base58Encode(srckey.sign(rawhash).toBytes());
        System.out.println(sigstr);
        // [4] 将短hash和签名进行base58编码以方便阅读和调试
        String base58EncodeHash = Base58.base58Encode(rawhash);
        //String base58EncodeSign = Base58.base58Encode(sigstr);
        System.out.println(Key25519.verify(base58EncodeHash, sigstr, srckey.toAddressString()));
    }
    public static String ShowStats() throws SatException, SQLException {
        BlockStats.GetStats();
        BlockStats.GetPointer();
        String info = String.format("Statistics for ours and maximum known parameters:\n " + "Total Blocks: %d\n " + "Height: %d\n" + " syncing %d\n" + " role:%d\n" + " Now top block: %s\n" + " Now MC block: %s\n" + " 4hr hashrate: %s of %s", BlockStats.Ntotal, totalheight, syncheight, SystemProperties.getDefault().getRole(), BlockStats.GetTopHash(), BlockStats.GetMctopHash(), GeneralMine.minePowerOur.GetPower(), GeneralMine.minePowerTotal.GetPower());
        return info;
    }
    public static void SortBlockFront(String value) {
        try {
            if (!value.equals("")) {
                String cmd = value;
                int count = Integer.parseInt(cmd);
                SATObjFactory.GetTravBlock().SortBlockFront(count);
            } else {
                SATObjFactory.GetTravBlock().SortBlockFront(10);
            }
        } catch (SatException e) {
            log.error(e.toString());
        } catch (SQLException | SignatureException e) {
            log.error(e.toString());
        }
    }
    public void SortBlock(String count) {
        TraverBlock tvblock = SATObjFactory.GetTravBlock();
        tvblock.SortBlockBack(Integer.parseInt(count));
    }
    public void ShowMcBlock(long height) {
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        Block blk = blockdag.GetMCBlock(height);
        System.out.println(Tools.ToJson(blk));
    }
    public void CreateAccount(String value) {
        SmartxCore core = new SmartxCore();
        if (SmartXWallet.wallettype == SmartXWallet.SECP_WALLETTYPE) {
            SmartXWallet.createAccount("123", myWalletDir);
            core.ReadAccounts();
        } else {
            Key key = SmartxCore.G_Wallet.fastkeys.addAccountRandom();
            System.out.println(key.toAddressString());
            SmartxCore.G_Wallet.fastkeys.flush();
        }
    }
    public static String list_accounts() {
        try {
            String info = "";
            if (SmartXWallet.wallettype == SmartXWallet.SECP_WALLETTYPE) {
                AccountDB accdb = SATObjFactory.GetAccDB();
                File file = new File(myWalletDir);
                File[] fs = file.listFiles();
                for (File f : fs) {
                    //if (f.getName().equals("UTC--2019-09-18T08.json")) continue;
                    if (!f.isDirectory() && f.getName().matches(".+\\.json")) {
                        FileInputStream fileInputStream = new FileInputStream(f);
                        byte[] keystoreByte = new byte[512];
                        fileInputStream.read(keystoreByte);
                        String keyStoreString = new String(keystoreByte);
                        JSON keyStoreJson = JSONObject.fromObject(keyStoreString);
                        String address = ((JSONObject) keyStoreJson).getString("address");
                        Account acc = accdb.GetAccount(address);
                        System.out.println(address);
                    }
                }
            } else {
                ///////////
                List<Key> keys = SmartxCore.G_Wallet.fastkeys.getAccounts();
                for (int i = 0; i < keys.size(); i++) {
                    info += keys.get(i).toAddressString();
                    info += "\n";
                    System.out.println(keys.get(i).toAddressString());
                }
                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public void quit() {
        System.out.println("quit now");
        System.exit(0);
    }
    public static String help() {
        return String.format("	stats - stats important parameters of statistical system\n" + "	help - help guide\n" + "	mining - start or stop system mining\n" + "	block - view block information\n" + "	state - show the system status info\n" + "	account - look accounts in dir\n" + "	create - create a account\n" + "	exit - exit the smartx \n" + "	balance - get all user balance\n" + "	main - show latest main block\n" + "	setcoinbase - set the mining address\n" + "	getcoinbase - get the mining address\n" + "	xfer - power transfer tools\n" + "	net - show p2p network infomation");
    }
    public static String Transfer(String to, String amount) {
        try {
            long xferamount = Long.parseLong(amount);
            Block blk = MakeTransfer(to, xferamount);
            BlockDAG blkdag = SATObjFactory.GetBlockDAG();
            blkdag.AddBlock(blk);
            return blk.header.hash;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public static Block MakeTransfer(String to, long amount) throws SatException, SignatureException {
        BlockMainTop top = SATObjFactory.GetMainTop();
        Block topblk = top.GetTopBlock();
        if (null == to || to.equals("")) return null;
        long tm = SmartxEpochTime.get_timestamp();
        Block blk = new Block();
        blk.header.headtype = 1;
        blk.header.btype = Block.BLKType.SMARTX_TXS;
        blk.header.timestamp = tm;
        blk.time = Tools.TimeStamp2DateEx(tm);
        blk.epoch = SmartxEpochTime.EpochTime(SmartxEpochTime.StrToStamp(blk.time));
        assert SmartxCore.G_Wallet != null;
        if (SmartXWallet.wallettype == SmartXWallet.FAST_WALLETTYPE)
            blk.header.address = SmartxCore.G_Wallet.getFastAddress();
        else blk.header.address = SmartxCore.G_Wallet.getAddress();
        blk.timenum = SmartxEpochTime.CalTimeEpochNum(tm);
        blk.header.nonce = Tools.getUUID();
        blk.header.amount = new BigInteger("0");
        blk.nodename = DataBase.G_NAME;
        blk.diff = "1";
        blk.header.random = "1";
        Field fieldfrom = new Field();
        fieldfrom.amount = BigInteger.valueOf(amount);
        fieldfrom.type = Field.FldType.SAT_FIELD_IN;
        fieldfrom.hash = SmartxCore.G_Wallet.getRealAddress();
        Field fieldto = new Field();
        fieldto.amount = BigInteger.valueOf(amount);
        fieldto.type = Field.FldType.SAT_FIELD_OUT;
        fieldto.hash = to;
        blk.Flds.add(fieldfrom);
        blk.Flds.add(fieldto);
        blk.header.hash = Sha256.getH256(blk);
        BlockDAG blockdag = SATObjFactory.GetBlockDAG();
        blk.sign = blockdag.SignBlock(blk.header.hash, SmartxCore.G_Wallet);
        //boolean result = Key25519.verify2(blk.ToSignStringBase58(), blk.sign, blk.header.address);
        log.info("------------------------------------------------------------------------------");
        log.info("TXS_create:[" + blk.time + "] " + blk.header.hash);
        return blk;
    }
    public static String shownet() {
        String info = "";
        try {
            List<Channel> list = SATObjFactory.GetChannelMrg().getActiveChannels();
            System.out.println("node count " + list.size());
            for (Channel c : list) {
                info += c.toString();
                info += "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }
}
