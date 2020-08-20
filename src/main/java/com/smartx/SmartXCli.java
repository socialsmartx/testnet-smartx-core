package com.smartx;

import java.io.File;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import com.smartx.cli.*;
import com.smartx.config.Constants;
import com.smartx.config.SystemProperties;
import com.smartx.config.exception.ConfigException;
import com.smartx.core.SmartxCore;
import com.smartx.core.blockchain.DataBase;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.core.consensus.SmartxEpochTime;
import com.smartx.core.exception.WalletLockedException;
import com.smartx.crypto.Hex;
import com.smartx.crypto.Key;
import com.smartx.crypto.Native;
import com.smartx.crypto.ed25519.bip39.MnemonicGenerator;
import com.smartx.message.CliMessages;
import com.smartx.util.ConsoleUtil;
import com.smartx.util.SystemUtil;
import com.smartx.util.TimeUtil;
import com.smartx.util.exception.LauncherException;
import com.smartx.wallet.Key25519;
import com.smartx.wallet.SmartXWallet;

public class SmartXCli extends Launcher {
    public static final boolean ENABLE_HD_WALLET_BY_DEFAULT = false;
    private static Logger logger = Logger.getLogger(Start.class);
    public SystemProperties config = SystemProperties.getDefault();
    public static void main(String[] args) {
        main(args, new SmartXCli());
    }
    public static void main(String args[], SmartXCli cli) {
        try {
            Native.disable();
            // check jvm version
            if (SystemUtil.is32bitJvm()) {
                logger.error(CliMessages.get("Jvm32NotSupported"));
                SystemUtil.exit(SystemUtil.Code.JVM_32_NOT_SUPPORTED);
            }
            // system system prerequisites
            // checkPrerequisite();
            // start CLI
            cli.setupLogger(args);
            cli.start(args);
        } catch (LauncherException | ConfigException | IOException exception) {
            logger.error(exception.getMessage());
        } catch (ParseException exception) {
            logger.error(CliMessages.get("ParsingFailed", exception.getMessage()));
        }
    }
    /**
     Creates a new SmartX CLI instance.
     */
    public SmartXCli() {
        SystemUtil.setLocale(getConfig().uiLocale());
        Option helpOption = Option.builder().longOpt(SmartXOption.HELP.toString()).desc(CliMessages.get("PrintHelp")).build();
        addOption(helpOption);
        Option versionOption = Option.builder().longOpt(SmartXOption.VERSION.toString()).desc(CliMessages.get("ShowVersion")).build();
        addOption(versionOption);
        Option accountOption = Option.builder().longOpt(SmartXOption.ACCOUNT.toString()).desc(CliMessages.get("ChooseAction")).hasArg(true).numberOfArgs(1).optionalArg(false).argName("action").type(String.class).build();
        addOption(accountOption);
        Option changePasswordOption = Option.builder().longOpt(SmartXOption.CHANGE_PASSWORD.toString()).desc(CliMessages.get("ChangeWalletPassword")).build();
        addOption(changePasswordOption);
        Option dumpPrivateKeyOption = Option.builder().longOpt(SmartXOption.DUMP_PRIVATE_KEY.toString()).desc(CliMessages.get("PrintHexKey")).hasArg(true).optionalArg(false).argName("address").type(String.class).build();
        addOption(dumpPrivateKeyOption);
        Option importPrivateKeyOption = Option.builder().longOpt(SmartXOption.IMPORT_PRIVATE_KEY.toString()).desc(CliMessages.get("ImportHexKey")).hasArg(true).optionalArg(false).argName("key").type(String.class).build();
        addOption(importPrivateKeyOption);
        Option reindexOption = Option.builder().longOpt(SmartXOption.REINDEX.toString()).desc(CliMessages.get("ReindexDescription")).hasArg(true).optionalArg(true).argName("to").type(String.class).build();
        addOption(reindexOption);
        Options options = getOptions();
        options.addOption(SmartXOption.RPCCLIENT.toString(), SmartXOption.RPCCLIENT.toString(), true, CliMessages.get("RpcClientDescription"));
        options.addOption(SmartXOption.RPCSERVER.toString(), SmartXOption.RPCSERVER.toString(), true, CliMessages.get("RpcServerDescription"));
    }
    public void start(String[] args) throws ParseException, IOException {
        // parse common options
        CommandLine cmd = parseOptions(args);
        // parse remaining options
        if (cmd.hasOption(SmartXOption.HELP.toString())) {
            printHelp();
        } else if (cmd.hasOption(SmartXOption.VERSION.toString())) {
            printVersion();
        } else if (cmd.hasOption(SmartXOption.ACCOUNT.toString())) {
            String action = cmd.getOptionValue(SmartXOption.ACCOUNT.toString()).trim();
            if ("create".equals(action)) {
                createAccount();
            } else if ("list".equals(action)) {
                listAccounts();
            }
        } else if (cmd.hasOption(SmartXOption.CHANGE_PASSWORD.toString())) {
            changePassword();
        } else if (cmd.hasOption(SmartXOption.DUMP_PRIVATE_KEY.toString())) {
            dumpPrivateKey(cmd.getOptionValue(SmartXOption.DUMP_PRIVATE_KEY.toString()).trim());
        } else if (cmd.hasOption(SmartXOption.IMPORT_PRIVATE_KEY.toString())) {
            importPrivateKey(cmd.getOptionValue(SmartXOption.IMPORT_PRIVATE_KEY.toString()).trim());
        } else {
            DataBase.rpcclient = cmd.getOptionValue(SmartXOption.RPCCLIENT.toString());
            if (DataBase.rpcclient != null) config.setRole(SystemProperties.SMARTX_ROLE_TERMINAL);
            if (cmd.hasOption(SmartXOption.PASSWORD.toString())) {
                DataBase.password = cmd.getOptionValue(SmartXOption.PASSWORD.toString()).trim();
                if (!DataBase.password.equals("")) setPassword(DataBase.password);
            }
            start();
        }
        if (cmd.hasOption(SmartXOption.RPCCLIENT.toString())) {
            System.out.println("rpcclient:" + DataBase.rpcclient);
            SmartxTerminal terminal = new SmartxTerminal();
            terminal.Terminal();
        } else if (cmd.hasOption(SmartXOption.RPCSERVER.toString())) {
            DataBase.rpcserver = cmd.getOptionValue(SmartXOption.RPCSERVER.toString()).trim();
            System.out.println("rpcserver:" + DataBase.rpcserver);
            TerminalServer.start();
            while (true) {
                SmartxEpochTime.Sleep(10000);
            }
        } else {
            TerminalServer.start();
            try {
                CliServer server = new CliServer();
                Thread gwServer = new Thread(server, "CliServer");
                gwServer.setPriority(Thread.MAX_PRIORITY);
                gwServer.start();
            } catch (Exception e) {
                System.out.println(e);
            }
            Command consolecmd = new Command();
            consolecmd.run();
        }
    }
    protected void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(200);
        formatter.printHelp("./smartx-cli.sh [options]", getOptions());
    }
    protected void printVersion() {
        System.out.println(Constants.CLIENT_VERSION);
    }
    protected void start() throws IOException {
        SmartXWallet smartxwallet = new SmartXWallet();
        Key25519 wallet = null;
        if (SmartXWallet.wallettype == SmartXWallet.FAST_WALLETTYPE) {
            // ed25519
            if (loadWallet().exists() == true) {
                wallet = loadAndUnlockWallet();
                smartxwallet.fastkeys = wallet;
            } else wallet = SmartXWallet.createfastAccount();
            smartxwallet.fastkeys = wallet;
            if (smartxwallet.fastkeys.getAccounts().size() > 0)
                smartxwallet.baseKey = smartxwallet.fastkeys.getAccount(0);
        } else {
            //secp
        }
        if (wallet == null) {
            return;
        }
        SmartxCore.G_Wallet = smartxwallet;
        if (SystemUtil.isPosix()) {
            if (!wallet.isPosixPermissionSecured()) {
                logger.warn(CliMessages.get("WarningWalletPosixPermission"));
            }
        }
        long timeDrift = TimeUtil.getTimeOffsetFromNtp();
        if (Math.abs(timeDrift) > 5000L) {
            logger.warn(CliMessages.get("SystemTimeDrift"));
        }
        if (isHdWalletEnabled().orElse(ENABLE_HD_WALLET_BY_DEFAULT)) {
            if (!wallet.isHdWalletInitialized()) {
                initializedHdSeed(wallet);
            }
        }
        List<Key> accounts = wallet.getAccounts();
        if (accounts.isEmpty()) {
            Key key;
            if (isHdWalletEnabled().orElse(ENABLE_HD_WALLET_BY_DEFAULT)) {
                key = wallet.addAccountWithNextHdKey();
            } else {
                key = wallet.addAccountRandom();
            }
            wallet.flush();
            accounts = wallet.getAccounts();
            logger.info(CliMessages.get("NewAccountCreatedForAddress", key.toAddressString()));
            smartxwallet.baseKey = key;
        }
        int coinbase = getCoinbase() == null ? 0 : getCoinbase();
        if (coinbase < 0 || coinbase >= accounts.size()) {
            logger.error(CliMessages.get("CoinbaseDoesNotExist"));
            exit(SystemUtil.Code.ACCOUNT_NOT_EXIST);
            return;
        }
        try {
            SATObjFactory.kernel = startKernel(getConfig(), wallet, wallet.getAccount(coinbase));
        } catch (Exception e) {
            logger.error("Uncaught exception during kernel startup.", e);
            exit(SystemUtil.Code.FAILED_TO_LAUNCH_KERNEL);
        }
    }
    protected Kernel startKernel(SystemProperties config, Key25519 wallet, Key coinbase) {
        logger.info("Ed25519 base: " + coinbase.toAddressString());
        Kernel kernel = new Kernel(config, wallet, coinbase);
        kernel.start();
        return kernel;
    }
    protected void createAccount() {
        Key25519 wallet = loadAndUnlockWallet();
        Key key;
        if (isHdWalletEnabled().orElse(ENABLE_HD_WALLET_BY_DEFAULT)) {
            key = wallet.addAccountWithNextHdKey();
        } else {
            key = wallet.addAccountRandom();
        }
        if (wallet.flush()) {
            logger.info(CliMessages.get("NewAccountCreatedForAddress", key.toAddressString()));
            logger.info(CliMessages.get("PublicKey", Hex.encode(key.getPublicKey())));
        }
    }
    protected void listAccounts() {
        Key25519 wallet = loadAndUnlockWallet();
        List<Key> accounts = wallet.getAccounts();
        if (accounts.isEmpty()) {
            logger.info(CliMessages.get("AccountMissing"));
        } else {
            for (int i = 0; i < accounts.size(); i++) {
                logger.info(CliMessages.get("ListAccountItem", i, accounts.get(i).toString()));
            }
        }
    }
    protected void changePassword() {
        Key25519 wallet = loadAndUnlockWallet();
        try {
            String newPassword = readNewPassword("EnterNewPassword", "ReEnterNewPassword");
            if (newPassword == null) {
                return;
            }
            wallet.changePassword(newPassword);
            boolean isFlushed = wallet.flush();
            if (!isFlushed) {
                logger.error(CliMessages.get("WalletFileCannotBeUpdated"));
                exit(SystemUtil.Code.FAILED_TO_WRITE_WALLET_FILE);
                return;
            }
            logger.info(CliMessages.get("PasswordChangedSuccessfully"));
        } catch (WalletLockedException exception) {
            logger.error(exception.getMessage());
        }
    }
    public void exit(int code) {
        SystemUtil.exit(code);
    }
    protected String readPassword() {
        return ConsoleUtil.readPassword();
    }
    protected String readPassword(String prompt) {
        return ConsoleUtil.readPassword(prompt);
    }
    /**
     Read a new password from input and require confirmation

     @return new password, or null if the confirmation failed
     */
    public String readNewPassword(String newPasswordMessageKey, String reEnterNewPasswordMessageKey) {
        String newPassword = readPassword(CliMessages.get(newPasswordMessageKey));
        String newPasswordRe = readPassword(CliMessages.get(reEnterNewPasswordMessageKey));
        if (!newPassword.equals(newPasswordRe)) {
            logger.error(CliMessages.get("ReEnterNewPasswordIncorrect"));
            exit(SystemUtil.Code.PASSWORD_REPEAT_NOT_MATCH);
            return null;
        }
        return newPassword;
    }
    protected void dumpPrivateKey(String address) {
        Key25519 wallet = loadAndUnlockWallet();
        byte[] addressBytes = Hex.decode0x(address);
        Key account = wallet.getAccount(addressBytes);
        if (account == null) {
            logger.error(CliMessages.get("AddressNotInWallet"));
            exit(SystemUtil.Code.ACCOUNT_NOT_EXIST);
        } else {
            System.out.println(CliMessages.get("PrivateKeyIs", Hex.encode(account.getPrivateKey())));
        }
    }
    protected void importPrivateKey(String key) {
        try {
            Key25519 wallet = loadAndUnlockWallet();
            byte[] keyBytes = Hex.decode0x(key);
            Key account = new Key(keyBytes);
            boolean accountAdded = wallet.addAccount(account);
            if (!accountAdded) {
                logger.error(CliMessages.get("PrivateKeyAlreadyInWallet"));
                exit(SystemUtil.Code.ACCOUNT_ALREADY_EXISTS);
                return;
            }
            boolean walletFlushed = wallet.flush();
            if (!walletFlushed) {
                logger.error(CliMessages.get("WalletFileCannotBeUpdated"));
                exit(SystemUtil.Code.FAILED_TO_WRITE_WALLET_FILE);
                return;
            }
            logger.info(CliMessages.get("PrivateKeyImportedSuccessfully"));
            logger.info(CliMessages.get("Address", account.toAddressString()));
            logger.info(CliMessages.get("PublicKey", Hex.encode(account.getPublicKey())));
        } catch (InvalidKeySpecException exception) {
            logger.error(CliMessages.get("PrivateKeyCannotBeDecoded", exception.getMessage()));
            exit(SystemUtil.Code.INVALID_PRIVATE_KEY);
        } catch (WalletLockedException exception) {
            logger.error(exception.getMessage());
            exit(SystemUtil.Code.WALLET_LOCKED);
        }
    }
    public Key25519 loadAndUnlockWallet() {
        setPassword(DataBase.password);
        Key25519 wallet = loadWallet();
        if (getPassword() == null) {
            if (wallet.unlock("")) {
                setPassword("");
            } else {
                // 根据配置如果有参数则直接使用参数的密码
                setPassword(readPassword());
            }
        }
        if (!wallet.unlock(getPassword())) {
            logger.error("Invalid password");
            exit(SystemUtil.Code.FAILED_TO_UNLOCK_WALLET);
        }
        return wallet;
    }
    /**
     Create a new wallet with a new password from input and save the wallet file
     to disk

     @return created new wallet, or null if it failed to create the wallet
     */
    protected Key25519 createNewWallet() {
        String newPassword = readNewPassword("EnterNewPassword", "ReEnterNewPassword");
        if (newPassword == null) {
            return null;
        }
        setPassword(newPassword);
        Key25519 wallet = loadWallet();
        if (!wallet.unlock(newPassword) || !wallet.flush()) {
            logger.error("CreateNewWalletError");
            exit(SystemUtil.Code.FAILED_TO_WRITE_WALLET_FILE);
            return null;
        }
        return wallet;
    }
    public Key25519 loadWallet() {
        return new Key25519(new File(getConfig().dataDir(), "wallet.data"), getConfig().network());
    }
    protected void initializedHdSeed(Key25519 wallet) {
        if (wallet.isUnlocked() && !wallet.isHdWalletInitialized()) {
            // HD Mnemonic
            System.out.println(CliMessages.get("HdWalletInitialize"));
            MnemonicGenerator generator = new MnemonicGenerator();
            String phrase = generator.getWordlist(Key25519.MNEMONIC_ENTROPY_LENGTH, Key25519.MNEMONIC_LANGUAGE);
            System.out.println(CliMessages.get("HdWalletMnemonic", phrase));
            String repeat = ConsoleUtil.readLine(CliMessages.get("HdWalletMnemonicRepeat"));
            repeat = String.join(" ", repeat.trim().split("\\s+"));
            if (!repeat.equals(phrase)) {
                logger.error(CliMessages.get("HdWalletInitializationFailure"));
                SystemUtil.exit(SystemUtil.Code.FAILED_TO_INIT_HD_WALLET);
                return;
            }
            wallet.initializeHdWallet(phrase);
            wallet.flush();
            logger.info(CliMessages.get("HdWalletInitializationSuccess"));
        }
    }
}
