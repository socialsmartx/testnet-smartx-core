package com.smartx.config;

import java.io.File;
import java.util.*;

import org.apache.log4j.Logger;
import org.spongycastle.util.encoders.Hex;
import org.web3j.utils.Numeric;

import com.smartx.crypto.ECKey;
import com.smartx.crypto.Key;
import com.smartx.message.Dictionary;
import com.smartx.net.Capability;
import com.smartx.net.CapabilityTreeSet;
import com.smartx.net.NodeManager;
import com.smartx.net.msg.MessageCode;
import com.smartx.util.StringUtil;
import com.smartx.util.SystemUtil;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;

public class SystemProperties {
    private static Logger logger = Logger.getLogger("config");
    public static final int SMARTX_ROLE_RULE = 1;       //裁决节点
    public static final int SMARTX_ROLE_POOL = 2;       //矿池节点
    public static final int SMARTX_ROLE_ORDINARY = 3;   //普通节点
    public static final int SMARTX_ROLE_LIGHT = 4;      //轻节点
    public static final int SMARTX_ROLE_TERMINAL = 5;   //终端
    private Config config;
    private static SystemProperties CONFIG;
    public int role = 0;
    protected String uiUnit = "SAT";
    protected int uiFractionDigits = 9;
    protected boolean forkUniformDistributionEnabled = false;
    protected boolean forkVirtualMachineEnabled = false;
    protected boolean forkVotingPrecompiledUpgradeEnabled = false;
    //api相关变量开始
    protected String[] apiPublicServices = {"blockchain", "account", "delegate", "tool", "node", "wallet"};
    protected String[] apiPrivateServices = {};
    //api相关变量结束
    //bft相关变量开始
    private Set<MessageCode> netPrioritizedMessages = new HashSet<>(Arrays.asList(MessageCode.BFT_NEW_HEIGHT, MessageCode.BFT_NEW_VIEW, MessageCode.BFT_PROPOSAL, MessageCode.BFT_VOTE));
    //bft相关变量结束
    public static SystemProperties getDefault() {
        if (CONFIG == null) {
            CONFIG = new SystemProperties();
        }
        return CONFIG;
    }
    public SystemProperties() {
        this(ConfigFactory.empty());
    }
    public SystemProperties(Config apiConfig) {
        try {
            Config javaSystemProperties = ConfigFactory.load("no-such-resource-only-system-props");
            Config referenceConfig = ConfigFactory.parseResources("smartx.conf");
            //            logger.info("Config (" + (referenceConfig.entrySet().size() > 0 ? " yes " : " no  ") + "): default properties from resource 'smartx.conf'");
            config = apiConfig.withFallback(referenceConfig);
            config = javaSystemProperties.withFallback(config).resolve();
        } catch (Exception e) {
            logger.error("Can't read config.", e);
            throw new RuntimeException(e);
        }
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
    /**
     api服务器配置开始
     */
    public boolean apiEnabled() {
        return config.getBoolean("api.enabled");
    }
    public String apiListenIp() {
        return config.getString("api.listenIp");
    }
    public int apiListenPort() {
        return config.getInt("api.listenPort");
    }
    public String apiUsername() {
        return config.getString("api.username");
    }
    public String apiPassword() {
        return config.getString("api.password");
    }
    public String[] apiPublicServices() {
        return this.apiPublicServices;
    }
    public String[] apiPrivateServices() {
        return this.apiPrivateServices;
    }
    /**api服务器配置结束*/
    /**
     UI相关配置 开始
     */
    public Locale uiLocale() {
        return Locale.getDefault();
    }
    /**UI相关配置 结束*/
    /**
     p2p相关配置 开始
     */
    public String network() {
        return config.getString("p2p.network");
    }
    public long networkVersion() {
        return config.getLong("p2p.networkVersion");
    }
    public String getClientId() {
        return String.format("%s/v%s-%s/%s", Constants.CLIENT_NAME, Constants.CLIENT_VERSION, SystemUtil.getOsName().toString(), SystemUtil.getOsArch());
    }
    public CapabilityTreeSet getClientCapabilities() {
        return CapabilityTreeSet.of(Capability.SMARTX, Capability.FAST_SYNC);
    }
    public Optional<String> p2pDeclaredIp() {
        if (!config.hasPath("p2p.declaredIp") || config.getObject("p2p.declaredIp") == null) {
            return Optional.empty();
        }
        String p2pDeclaredIp = config.getString("p2p.declaredIp");
        return StringUtil.isNullOrEmpty(p2pDeclaredIp) ? Optional.empty() : Optional.of(p2pDeclaredIp);
    }
    public Dictionary getMysqlConf() {
        Dictionary dict = new Dictionary("0");
        if (null != config.getString("mysql.username")) dict.args.put("username", config.getString("mysql.username"));
        if (null != config.getString("mysql.password")) dict.args.put("password", config.getString("mysql.password"));
        if (null != config.getString("mysql.connection"))
            dict.args.put("connection", config.getString("mysql.connection"));
        return dict;
    }
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
    public String getHash512AlgName() {
        return config.getString("crypto.hash.alg512");
    }
    public String p2pListenIp() {
        return config.getString("p2p.listenIp");
    }
    public int p2pListenPort() {
        return config.getInt("p2p.listenPort");
    }
    public String getCryptoProviderName() {
        return config.getString("crypto.providerName");
    }
    public String getHash256AlgName() {
        return config.getString("crypto.hash.alg256");
    }
    public int role() {
        role = config.getInt("p2p.role");
        return role;
    }
    public int getRole() {
        return role;
    }
    public void setRole(int rol) {
        role = rol;
    }
    public Set<MessageCode> netPrioritizedMessages() {
        return netPrioritizedMessages;
    }
    public int getChannel() {
        return config.getInt("net.channel");
    }
    public int netMaxOutboundConnections() {
        if (!config.hasPath("net.maxOutboundConnections")) return 128;
        return config.getInt("net.maxOutboundConnections");
    }
    public int netMaxInboundConnections() {
        if (!config.hasPath("net.maxInboundConnections")) return 512;
        return config.getInt("net.maxInboundConnections");
    }
    public int netMaxInboundConnectionsPerIp() {
        if (!config.hasPath("net.maxInboundConnectionsPerIp")) return 5;
        return config.getInt("net.maxInboundConnectionsPerIp");
    }
    public int netMaxMessageQueueSize() {
        if (!config.hasPath("net.maxMessageQueueSize")) return 4096;
        return config.getInt("net.maxMessageQueueSize");
    }
    public int netMaxFrameBodySize() {
        if (!config.hasPath("net.maxFrameBodySize")) return 128 * 1024;
        return config.getInt("net.maxFrameBodySize");
    }
    public int netMaxPacketSize() {
        if (!config.hasPath("net.netMaxPacketSize")) return 16 * 1024 * 1024;
        return config.getInt("net.netMaxPacketSize");
    }
    public int netRelayRedundancy() {
        if (!config.hasPath("net.netRelayRedundancy")) return 8;
        return config.getInt("net.netRelayRedundancy");
    }
    public int netHandshakeExpiry() {
        if (!config.hasPath("net.netHandshakeExpiry")) return 5 * 60 * 1000;
        return config.getInt("net.netHandshakeExpiry");
    }
    public int netChannelIdleTimeout() {
        if (!config.hasPath("net.netChannelIdleTimeout")) return 2 * 60 * 1000;
        return config.getInt("net.netChannelIdleTimeout");
    }
    public List<String> netDnsSeedsMainNet() {
        if (!config.hasPath("net.dnsSeeds.mainNet")) return null;
        return config.getStringList("net.dnsSeeds.mainNet");
    }
    public List<String> netDnsSeedsTestNet() {
        if (!config.hasPath("net.dnsSeeds.testNet")) return null;
        return config.getStringList("net.dnsSeeds.testNet");
    }
    public List<NodeManager.Node> p2pSeedNodes() {
        List<String> nodes = config.getStringList("p2p.seedNodes");
        List<NodeManager.Node> p2pSeedNodes = new ArrayList<>();
        for (String node : nodes) {
            if (!node.trim().isEmpty()) {
                String[] tokens = node.trim().split(":");
                if (tokens.length == 2) {
                    p2pSeedNodes.add(new NodeManager.Node(tokens[0], Integer.parseInt(tokens[1])));
                } else {
                    p2pSeedNodes.add(new NodeManager.Node(tokens[0], Constants.DEFAULT_P2P_PORT));
                }
            }
        }
        return p2pSeedNodes;
    }
    /**p2p相关配置 结束*/
    /**
     core相关配置 开始
     */
    public String genesisHash() {
        return config.getString("core.genesishash");
    }
    public String getGenesisDate() {
        return config.getString("core.genesisdate");
    }
    public String getGenesisEpoch() {
        return config.getString("core.genesisepoch");
    }
    public String getNodeName() {
        return config.getString("core.nodename");
    }
    public String getSyncStart() {
        return config.getString("core.syncstart");
    }
    public int getSeedNode() {
        return config.getInt("core.seednode");
    }
    public int getDbtype() {
        return config.getInt("core.dbtype");
    }
    public String getNodeInfo() {
        return config.getString("core.node");
    }
    public String getLocalInfo() {
        return config.getString("core.local");
    }
    public String getRuleSignInfo() {
        return config.getString("core.rulesign");
    }
    public String getRuleSignInfo2() {
        return config.getString("core.rulesign2");
    }
    public boolean getMining() {
        return config.getBoolean("core.mining");
    }
    public String getGenesisHash() {
        return config.getString("core.genesishash");
    }
    public String dataDir() {
        return ".";
    }
    public File databaseDir(String network) {
        return new File(dataDir(), Constants.DATABASE_DIR + File.separator + network.toLowerCase(Locale.ROOT));
    }
    public File configDir() {
        return new File(dataDir(), Constants.CONFIG_DIR);
    }
    /**core相关配置 结束*/
    /**
     bft相关配置 开始
     */
    public long bftNewHeightTimeout() {
        if (!config.hasPath("bft.bftNewHeightTimeout")) return 3000L;
        return config.getLong("bft.bftNewHeightTimeout");
    }
    public long bftProposeTimeout() {
        if (!config.hasPath("bft.bftProposeTimeout")) return 3000L;
        return config.getLong("bft.bftProposeTimeout");
    }
    public long bftValidateTimeout() {
        if (!config.hasPath("bft.bftValidateTimeout")) return 3000L;
        return config.getLong("bft.bftValidateTimeout");
    }
    public long bftPreCommitTimeout() {
        if (!config.hasPath("bft.bftPreCommitTimeout")) return 3000L;
        return config.getLong("bft.bftPreCommitTimeout");
    }
    public long bftCommitTimeout() {
        if (!config.hasPath("bft.bftCommitTimeout")) return 3000L;
        return config.getLong("bft.bftCommitTimeout");
    }
    public long bftFinalizeTimeout() {
        if (!config.hasPath("bft.bftFinalizeTimeout")) return 3000L;
        return config.getLong("bft.bftFinalizeTimeout");
    }
    public long bftMaxBlockTimeDrift() {
        if (!config.hasPath("bft.bftMaxBlockTimeDrift")) return 3000L;
        return config.getLong("bft.bftMaxBlockTimeDrift");
    }
    //TODO:这里仅仅是为了测试裁决节点的2/3签名，后面正式版本不会用到这个函数
    public List<Key> bftPubKeys() {
        List<Key> accountList = new ArrayList<>();
        List<? extends ConfigObject> listObject = config.getObjectList("bft.rule.list");
        for (ConfigObject configObject : listObject) {
            Key account;
            if (configObject.get("privKey") != null) {
                String hexPrivKey = configObject.toConfig().getString("privKey");
                account = Key.fromRawPrivateKey(Hex.decode(hexPrivKey));
                accountList.add(account);
            }
        }
        return accountList;
    }
    /**bft相关配置 结束*/
}
