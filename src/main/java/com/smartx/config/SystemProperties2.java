package com.smartx.config;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.spongycastle.util.encoders.Hex;
import org.web3j.utils.Numeric;

import com.smartx.crypto.ECKey;
import com.smartx.util.BuildInfo;
import com.smartx.util.Utils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;

public class SystemProperties2 {
    private static Logger logger = Logger.getLogger("general");
    public static final int SMARTX_ROLE_RULE = 1;       //裁决节点
    public static final int SMARTX_ROLE_POOL = 2;       //矿池节点
    public static final int SMARTX_ROLE_ORDINARY = 3;   //普通节点
    /**
     Marks config accessor methods which need to be called (for value validation)
     upon config creation or modification
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ValidateMe {
    }
    private Boolean discoveryEnabled = null;
    private Config config;
    private String databaseDir = null;
    private Boolean databaseReset = null;
    private String projectVersion = null;
    private String projectVersionModifier = null;
    protected Integer databaseVersion = null;
    private String bindIp = null;
    private String externalIp = null;
    private static SystemProperties2 CONFIG;
    private static boolean useOnlySpringConfig = false;
    private String generatedNodePrivateKey;
    private final ClassLoader classLoader;
    private GenerateNodeIdStrategy generateNodeIdStrategy = null;
    static boolean isUseOnlySpringConfig() {
        return useOnlySpringConfig;
    }
    /**
     Returns the static config instance. If the config is passed
     as a Spring bean by the application this instance shouldn't
     be used
     This method is mainly used for testing purposes
     (Autowired fields are initialized with this static instance
     but when running within Spring context they replaced with the
     bean config instance)
     */
    public static SystemProperties2 getDefault() {
        return useOnlySpringConfig ? null : getSpringDefault();
    }
    static SystemProperties2 getSpringDefault() {
        if (CONFIG == null) {
            CONFIG = new SystemProperties2();
        }
        return CONFIG;
    }
    public SystemProperties2() {
        this(ConfigFactory.empty());
    }
    public SystemProperties2(File configFile) {
        this(ConfigFactory.parseFile(configFile));
    }
    public SystemProperties2(String configResource) {
        this(ConfigFactory.parseResources(configResource));
    }
    public SystemProperties2(Config apiConfig) {
        this(apiConfig, SystemProperties2.class.getClassLoader());
    }
    public SystemProperties2(Config apiConfig, ClassLoader classLoader) {
        try {
            this.classLoader = classLoader;
            Config javaSystemProperties = ConfigFactory.load("no-such-resource-only-system-props");
            Config referenceConfig = ConfigFactory.parseResources("smartx.conf");
            logger.info("Config (" + (referenceConfig.entrySet().size() > 0 ? " yes " : " no  ") + "): default properties from resource 'smartx.conf'");
            config = apiConfig.withFallback(referenceConfig);
            //logger.debug("Config trace: " + config.root().render(ConfigRenderOptions.defaults().setComments(false).setJson(false)));
            config = javaSystemProperties.withFallback(config).resolve();     // substitute variables in config if any
            List<InputStream> iStreams = loadResources("version.properties", this.getClass().getClassLoader());
            for (InputStream is : iStreams) {
                Properties props = new Properties();
                props.load(is);
                if (props.getProperty("versionNumber") == null || props.getProperty("databaseVersion") == null) {
                    continue;
                }
                this.projectVersion = props.getProperty("versionNumber");
                this.projectVersion = this.projectVersion.replaceAll("'", "");
                if (this.projectVersion == null) this.projectVersion = "-.-.-";
                this.projectVersionModifier = "master".equals(BuildInfo.buildBranch) ? "RELEASE" : "SNAPSHOT";
                this.databaseVersion = Integer.valueOf(props.getProperty("databaseVersion"));
                this.generateNodeIdStrategy = new GetNodeIdFromPropsFile(databaseDir()).withFallback(new GenerateNodeIdRandomly(databaseDir()));
                break;
            }
        } catch (Exception e) {
            logger.error("Can't read config.", e);
            throw new RuntimeException(e);
        }
    }
    /**
     Loads resources using given ClassLoader assuming, there could be several resources
     with the same name
     */
    public static List<InputStream> loadResources(final String name, final ClassLoader classLoader) throws IOException {
        final List<InputStream> list = new ArrayList<InputStream>();
        final Enumeration<URL> systemResources = (classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader).getResources(name);
        while (systemResources.hasMoreElements()) {
            list.add(systemResources.nextElement().openStream());
        }
        return list;
    }
    public Config getConfig() {
        return config;
    }
    public <T> T getProperty(String propName, T defaultValue) {
        if (!config.hasPath(propName)) return defaultValue;
        String string = config.getString(propName);
        if (string.trim().isEmpty()) return defaultValue;
        return (T) config.getAnyRef(propName);
    }
    @ValidateMe
    public int syncPeerCount() {
        return config.getInt("sync.peer.count");
    }
    public Integer syncVersion() {
        if (!config.hasPath("sync.version")) {
            return null;
        }
        return config.getInt("sync.version");
    }
    @ValidateMe
    public String projectVersion() {
        return projectVersion;
    }
    @ValidateMe
    public String projectVersionModifier() {
        return projectVersionModifier;
    }
    @ValidateMe
    public String helloPhrase() {
        return config.getString("hello.phrase");
    }
    @ValidateMe
    public String rootHashStart() {
        return config.hasPath("root.hash.start") ? config.getString("root.hash.start") : null;
    }
    @ValidateMe
    public List<String> peerCapabilities() {
        return config.getStringList("peer.capabilities");
    }
    @ValidateMe
    public boolean peerDiscovery() {
        return discoveryEnabled == null ? config.getBoolean("peer.discovery.enabled") : discoveryEnabled;
    }
    public void setDiscoveryEnabled(Boolean discoveryEnabled) {
        this.discoveryEnabled = discoveryEnabled;
    }
    @ValidateMe
    public boolean peerDiscoveryPersist() {
        return config.getBoolean("peer.discovery.persist");
    }
    @ValidateMe
    public int peerDiscoveryWorkers() {
        return config.getInt("peer.discovery.workers");
    }
    @ValidateMe
    public int peerDiscoveryTouchPeriod() {
        return config.getInt("peer.discovery.touchPeriod");
    }
    @ValidateMe
    public int peerDiscoveryTouchMaxNodes() {
        return config.getInt("peer.discovery.touchMaxNodes");
    }
    @ValidateMe
    public int peerConnectionTimeout() {
        return config.getInt("peer.connection.timeout") * 1000;
    }
    @ValidateMe
    public int transactionApproveTimeout() {
        return config.getInt("transaction.approve.timeout") * 1000;
    }
    @ValidateMe
    public List<String> peerDiscoveryIPList() {
        return config.getStringList("peer.discovery.ip.list");
    }
    @ValidateMe
    public boolean databaseReset() {
        return databaseReset == null ? config.getBoolean("database.reset") : databaseReset;
    }
    public void setDatabaseReset(Boolean reset) {
        databaseReset = reset;
    }
    @ValidateMe
    public long databaseResetBlock() {
        return config.getLong("database.resetBlock");
    }
    @ValidateMe
    public boolean databaseFromBackup() {
        return config.getBoolean("database.fromBackup");
    }
    @ValidateMe
    public int databasePruneDepth() {
        return config.getBoolean("database.prune.enabled") ? config.getInt("database.prune.maxDepth") : -1;
    }
    @ValidateMe
    public Integer blockQueueSize() {
        return config.getInt("recvcache.blockQueueSize") * 1024 * 1024;
    }
    @ValidateMe
    public Integer headerQueueSize() {
        return config.getInt("recvcache.headerQueueSize") * 1024 * 1024;
    }
    @ValidateMe
    public Integer peerChannelReadTimeout() {
        return config.getInt("peer.channel.read.timeout");
    }
    @ValidateMe
    public Integer traceStartBlock() {
        return config.getInt("trace.startblock");
    }
    @ValidateMe
    public boolean recordBlocks() {
        return config.getBoolean("record.blocks");
    }
    @ValidateMe
    public boolean dumpFull() {
        return config.getBoolean("dump.full");
    }
    @ValidateMe
    public String dumpDir() {
        return config.getString("dump.dir");
    }
    @ValidateMe
    public String dumpStyle() {
        return config.getString("dump.style");
    }
    @ValidateMe
    public int dumpBlock() {
        return config.getInt("dump.block");
    }
    @ValidateMe
    public String databaseDir() {
        return databaseDir == null ? config.getString("database.dir") : databaseDir;
    }
    public String ethashDir() {
        return config.hasPath("ethash.dir") ? config.getString("ethash.dir") : databaseDir();
    }
    public void setDataBaseDir(String dataBaseDir) {
        this.databaseDir = dataBaseDir;
    }
    @ValidateMe
    public boolean dumpCleanOnRestart() {
        return config.getBoolean("dump.clean.on.restart");
    }
    public String privateKey() {
        if (config.hasPath("peer.privateKey")) {
            String key = config.getString("peer.privateKey");
            if (key.length() != 64 || !Utils.isHexEncoded(key)) {
                throw new RuntimeException("The peer.privateKey needs to be Hex encoded and 32 byte length");
            }
            return key;
        } else {
            return getGeneratedNodePrivateKey();
        }
    }
    private String getGeneratedNodePrivateKey() {
        try {
            if (generateNodeIdStrategy == null) {
                System.out.println("generateNodeIdStrategy is null");
            }
            if (generatedNodePrivateKey == null) {
                generatedNodePrivateKey = generateNodeIdStrategy.getNodePrivateKey();
            }
            return generatedNodePrivateKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public ECKey getMyKey() {
        return ECKey.fromPrivate(Hex.decode(privateKey()));
    }
    /**
     Home NodeID calculated from 'peer.privateKey' property
     */
    public byte[] nodeId() {
        return getMyKey().getNodeId();
    }
    @ValidateMe
    public int networkId() {
        return config.getInt("peer.networkId");
    }
    @ValidateMe
    public int maxActivePeers() {
        return config.getInt("peer.maxActivePeers");
    }
    @ValidateMe
    public boolean eip8() {
        return config.getBoolean("peer.p2p.eip8");
    }
    @ValidateMe
    public int listenPort() {
        return config.getInt("peer.listen.port");
    }
    /**
     This can be a blocking call with long timeout (thus no ValidateMe)
     */
    public String bindIp() {
        if (!config.hasPath("peer.discovery.bind.ip") || config.getString("peer.discovery.bind.ip").trim().isEmpty()) {
            if (bindIp == null) {
                logger.info("Bind address wasn't set, Punching to identify it...");
                try (Socket s = new Socket("www.google.com", 80)) {
                    bindIp = s.getLocalAddress().getHostAddress();
                    logger.info(String.format("UDP local bound to: {%s}", bindIp));
                } catch (IOException e) {
                    logger.warn("Can't get bind IP. Fall back to 0.0.0.0: " + e);
                    bindIp = "0.0.0.0";
                }
            }
            return bindIp;
        } else {
            return config.getString("peer.discovery.bind.ip").trim();
        }
    }
    /**
     This can be a blocking call with long timeout (thus no ValidateMe)
     */
    public String externalIp() {
        if (!config.hasPath("peer.discovery.external.ip") || config.getString("peer.discovery.external.ip").trim().isEmpty()) {
            if (externalIp == null) {
                logger.info("External IP wasn't set, using checkip.amazonaws.com to identify it...");
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream()));
                    externalIp = in.readLine();
                    if (externalIp == null || externalIp.trim().isEmpty()) {
                        throw new IOException("Invalid address: '" + externalIp + "'");
                    }
                    try {
                        InetAddress.getByName(externalIp);
                    } catch (Exception e) {
                        throw new IOException("Invalid address: '" + externalIp + "'");
                    }
                    logger.info(String.format("External address identified: {%s}", externalIp));
                } catch (IOException e) {
                    externalIp = bindIp();
                    logger.warn("Can't get external IP. Fall back to peer.bind.ip: " + externalIp + " :" + e);
                }
            }
            return externalIp;
        } else {
            return config.getString("peer.discovery.external.ip").trim();
        }
    }
    @ValidateMe
    public boolean isPublicHomeNode() {
        return config.getBoolean("peer.discovery.public.home.node");
    }
    @ValidateMe
    public String getHash256AlgName() {
        return config.getString("crypto.hash.alg256");
    }
    @ValidateMe
    public String getHash512AlgName() {
        return config.getString("crypto.hash.alg512");
    }
    @ValidateMe
    public String getCryptoProviderName() {
        return config.getString("crypto.providerName");
    }
    @ValidateMe
    public String getGenesisDate() {
        return config.getString("core.genesisdate");
    }
    @ValidateMe
    public String getGenesisEpoch() {
        return config.getString("core.genesisepoch");
    }
    @ValidateMe
    public String getNodeName() {
        return config.getString("core.nodename");
    }
    @ValidateMe
    public String getSyncStart() {
        return config.getString("core.syncstart");
    }
    @ValidateMe
    public int getSeedNode() {
        return config.getInt("core.seednode");
    }
    @ValidateMe
    public int getDbtype() {
        return config.getInt("core.dbtype");
    }
    @ValidateMe
    public String getNodeInfo() {
        return config.getString("core.node");
    }
    @ValidateMe
    public String getLocalInfo() {
        return config.getString("core.local");
    }
    @ValidateMe
    public String getRuleSignInfo() {
        return config.getString("core.rulesign");
    }
    @ValidateMe
    public boolean getMining() {
        return config.getBoolean("core.mining");
    }
    @ValidateMe
    public int getRole() {
        return config.getInt("peer.role");
    }
    @ValidateMe
    public String getGenesisHash() {
        return config.getString("core.genesishash");
    }
    //TODO:这里仅仅是为了测试裁决节点的2/3签名，后面正式版本不会用到这个函数
    @ValidateMe
    public List<ECKey> getBftPubKeys() {
        List<ECKey> ecKeyList = new ArrayList<>();
        List<? extends ConfigObject> listObject = config.getObjectList("bft.rule.list");
        for (ConfigObject configObject : listObject) {
            ECKey ecKey;
            if (configObject.get("privKey") != null) {
                String hexPrivKey = configObject.toConfig().getString("privKey");
                ecKey = ECKey.fromPrivate(Numeric.hexStringToByteArray(hexPrivKey));
                ecKeyList.add(ecKey);
            }
        }
        return ecKeyList;
    }
}
