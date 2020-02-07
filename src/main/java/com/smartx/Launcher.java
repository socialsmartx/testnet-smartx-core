package com.smartx;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.smartx.cli.SmartXOption;
import com.smartx.config.*;
import com.smartx.config.LoggerConfigurator;
import com.smartx.event.PubSubFactory;
import com.smartx.message.CliMessages;
import com.smartx.util.SystemUtil;
import com.smartx.util.exception.LauncherException;

public abstract class Launcher {
    private static Logger logger = Logger.getLogger(Launcher.class);
    private static final String ENV_SEMUX_WALLET_PASSWORD = "SEMUX_WALLET_PASSWORD";
    /**
     Here we make sure that all shutdown hooks will be executed in the order of
     registration. This is necessary to be manually maintained because
     ${@link Runtime#addShutdownHook(Thread)} starts shutdown hooks concurrently
     in unspecified order.
     */
    private static final List<Pair<String, Runnable>> shutdownHooks = Collections.synchronizedList(new ArrayList<>());
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(Launcher::shutdownHook, "shutdown-hook"));
    }
    private final Options options = new Options();
    private String dataDir = Constants.DEFAULT_DATA_DIR;
    private Integer coinbase = null;
    private String password = null;
    private String network = null;
    private Boolean hdWalletEnabled = null;
    public Launcher() {
        Option dataDirOption = Option.builder().longOpt(SmartXOption.DATA_DIR.toString()).desc(CliMessages.get("SpecifyDataDir")).hasArg(true).numberOfArgs(1).optionalArg(false).argName("path").type(String.class).build();
        addOption(dataDirOption);
        Option networkOption = Option.builder().longOpt(SmartXOption.NETWORK.toString()).desc(CliMessages.get("SpecifyNetwork")).hasArg(true).numberOfArgs(1).optionalArg(false).argName("name").type(String.class).build();
        addOption(networkOption);
        Option coinbaseOption = Option.builder().longOpt(SmartXOption.COINBASE.toString()).desc(CliMessages.get("SpecifyCoinbase")).hasArg(true).numberOfArgs(1).optionalArg(false).argName("index").type(Number.class).build();
        addOption(coinbaseOption);
        Option passwordOption = Option.builder().longOpt(SmartXOption.PASSWORD.toString()).desc(CliMessages.get("WalletPassword")).hasArg(true).numberOfArgs(1).optionalArg(false).argName("password").type(String.class).build();
        addOption(passwordOption);
        Option hdOption = Option.builder().longOpt(SmartXOption.HD_WALLET.toString()).desc(CliMessages.get("SpecifyHDWallet")).hasArg(true).numberOfArgs(1).optionalArg(false).argName("hd").type(Boolean.class).build();
        addOption(hdOption);
    }
    /**
     Creates an instance of {@link Config} based on the given `--network` option.
     <p>
     Defaults to MainNet.

     @return the configuration
     */
    public SystemProperties getConfig() {
        return SystemProperties.getDefault();
    }
    /**
     Returns the network.

     @return
     */
    public String getNetwork() {
        return network;
    }
    /**
     Returns the data directory.

     @return
     */
    public String getDataDir() {
        return dataDir;
    }
    /**
     Returns the coinbase.

     @return The specified coinbase, or NULL
     */
    public Integer getCoinbase() {
        return coinbase;
    }
    /**
     Returns the provided password if any.

     @return The specified password, or NULL
     */
    public String getPassword() {
        return password;
    }
    /**
     Parses options from the given arguments.

     @param args
     @return
     @throws ParseException
     */
    protected CommandLine parseOptions(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(getOptions(), args);
        if (cmd.hasOption(SmartXOption.DATA_DIR.toString())) {
            setDataDir(cmd.getOptionValue(SmartXOption.DATA_DIR.toString()));
        }
        if (cmd.hasOption(SmartXOption.NETWORK.toString())) {
            String option = cmd.getOptionValue(SmartXOption.NETWORK.toString());
            if (option.isEmpty()) {
                option = "mainnet";
            }
            setNetwork(option);
        }
        if (cmd.hasOption(SmartXOption.COINBASE.toString())) {
            setCoinbase(((Number) cmd.getParsedOptionValue(SmartXOption.COINBASE.toString())).intValue());
        }
        // Priority: arguments => system property => console input
        if (cmd.hasOption(SmartXOption.PASSWORD.toString())) {
            setPassword(cmd.getOptionValue(SmartXOption.PASSWORD.toString()));
        } else if (System.getenv(ENV_SEMUX_WALLET_PASSWORD) != null) {
            setPassword(System.getenv(ENV_SEMUX_WALLET_PASSWORD));
        }
        if (cmd.hasOption(SmartXOption.HD_WALLET.toString())) {
            setHdWalletEnabled(Boolean.parseBoolean(cmd.getOptionValue(SmartXOption.HD_WALLET.toString())));
        }
        return cmd;
    }
    /**
     Set up customized logger configuration.

     @param args
     @throws ParseException
     */
    protected void setupLogger(String[] args) throws ParseException {
        // parse options
        parseOptions(args);
        LoggerConfigurator.configure(new File(dataDir));
    }
    /**
     Set up pubsub service.
     */
    protected void setupPubSub() {
        PubSubFactory.getDefault().start();
        registerShutdownHook("pubsub-default", () -> PubSubFactory.getDefault().stop());
    }
    /**
     Returns all supported options.

     @return
     */
    public Options getOptions() {
        return options;
    }
    /**
     Adds a supported option.

     @param option
     */
    protected void addOption(Option option) {
        options.addOption(option);
    }
    /**
     Sets the network.

     @param network
     */
    protected void setNetwork(String network) {
        this.network = network;
    }
    /**
     Sets the data directory.

     @param dataDir
     */
    protected void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }
    /**
     Sets the coinbase.

     @param coinbase
     */
    protected void setCoinbase(int coinbase) {
        this.coinbase = coinbase;
    }
    /**
     Sets the password.

     @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    public Optional<Boolean> isHdWalletEnabled() {
        return Optional.ofNullable(hdWalletEnabled);
    }
    public void setHdWalletEnabled(Boolean hdWalletEnabled) {
        this.hdWalletEnabled = hdWalletEnabled;
    }
    /**
     Check runtime prerequisite.
     */
    protected static void checkPrerequisite() {
        switch (SystemUtil.getOsName()) {
            case WINDOWS:
                if (!SystemUtil.isWindowsVCRedist2012Installed()) {
                    throw new LauncherException("Microsoft Visual C++ 2012 Redistributable Package is not installed. Please visit: https://www.microsoft.com/en-us/download/details.aspx?id=30679");
                }
                break;
            default:
        }
    }
    /**
     Registers a shutdown hook which will be executed in the order of
     registration.

     @param name
     @param runnable
     */
    public static synchronized void registerShutdownHook(String name, Runnable runnable) {
        shutdownHooks.add(Pair.of(name, runnable));
    }
    /**
     Call registered shutdown hooks in the order of registration.
     */
    private static synchronized void shutdownHook() {
        // shutdown hooks
        for (Pair<String, Runnable> r : shutdownHooks) {
            try {
                logger.info(String.format("Shutting down {%s}", r.getLeft()));
                r.getRight().run();
            } catch (Exception e) {
                logger.info(String.format("Failed to shutdown {%s %s}", r.getLeft(), e));
            }
        }
        // flush log4j async loggers
        LogManager.shutdown();
    }
}
