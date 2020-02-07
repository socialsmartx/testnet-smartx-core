/**
 Copyright (c) 2017-2018 The Smartx Developers
 <p>
 Distributed under the MIT software license, see the accompanying file
 LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.smartx;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.xml.sax.SAXException;

import com.smartx.api.SmartXApiService;
import com.smartx.config.SystemProperties;
import com.smartx.core.SmartxCore;
import com.smartx.core.blockchain.SATObjFactory;
import com.smartx.crypto.Key;
import com.smartx.db.DatabaseFactory;
import com.smartx.event.KernelBootingEvent;
import com.smartx.event.PubSub;
import com.smartx.event.PubSubFactory;
import com.smartx.net.ChannelManager;
import com.smartx.net.NodeManager;
import com.smartx.net.PeerClient;
import com.smartx.net.PeerServer;
import com.smartx.wallet.Key25519;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

/**
 * Kernel holds references to each individual components.
 */
public class Kernel {
    // Fix JNA issue: There is an incompatible JNA native library installed
    static {
        System.setProperty("jna.nosys", "true");
    }
    private static Logger logger = Logger.getLogger(Kernel.class);
    private static final PubSub pubSub = PubSubFactory.getDefault();
    public enum State {
        STOPPED, BOOTING, RUNNING, STOPPING
    }
    protected State state = State.STOPPED;
    protected SystemProperties config;
    protected Key25519 wallet;
    protected Key coinbase;
    protected DatabaseFactory dbFactory;
    protected PeerClient client;
    protected ChannelManager channelMgr;
    protected NodeManager nodeMgr;
    protected PeerServer p2p;
    protected SmartXApiService api;
    protected Thread consThread;
    SmartxCore smartxCore;
    /**
     * Creates a kernel instance and initializes it.
     *
     * @param config   the config instance
     * @param wallet   the wallet instance
     * @param coinbase the coinbase key
     * @prarm genesis the genesis instance
     */
    //TODO:需要读取创世块的json文件
    public Kernel(SystemProperties config, Key25519 wallet, Key coinbase) {
        this.config = config;
        this.wallet = wallet;
        this.coinbase = coinbase;
    }
    /**
     * Sets up uPnP port mapping.
     */
    protected void setupUpnp() {
        try {
            GatewayDiscover discover = new GatewayDiscover();
            Map<InetAddress, GatewayDevice> devices = discover.discover();
            for (Map.Entry<InetAddress, GatewayDevice> entry : devices.entrySet()) {
                GatewayDevice gw = entry.getValue();
                logger.info(String.format("Found a gateway device: local address = {%s}, external address = {%s}", gw.getLocalAddress().getHostAddress(), gw.getExternalIPAddress()));
                gw.deletePortMapping(config.p2pListenPort(), "TCP");
                gw.addPortMapping(config.p2pListenPort(), config.p2pListenPort(), gw.getLocalAddress().getHostAddress(), "TCP", "Semux P2P network");
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            logger.info("Failed to add port mapping", e);
        }
    }
    public void InitEventMQ() {
        pubSub.start();
        SATObjFactory.GetCommand();
    }
    /**
     * Start the kernel.
     */
    public synchronized void start() {
        InitEventMQ();
        if (state != State.STOPPED) return;
        else {
            state = State.BOOTING;
            pubSub.publish(new KernelBootingEvent());
        }
        logger.info(config.getClientId());
        logger.info(String.format("System booting up: network = {%s}, networkVersion = {%s}, coinbase = {%s}", config.network(), config.network(), coinbase));
        // printSystemInfo();
        // TimeUtil.startNtpProcess();
        client = new PeerClient(config, coinbase);
        channelMgr = SATObjFactory.channelManager = new ChannelManager(this);
        nodeMgr = new NodeManager(this);
        nodeMgr.start();
        SATObjFactory.GetMessageHandle();
        if (config.getRole() != SystemProperties.SMARTX_ROLE_TERMINAL) {
            p2p = new PeerServer(this);
            p2p.start();
        }
        new Thread(this::setupUpnp, "upnp").start();
        if (config.getRole() != SystemProperties.SMARTX_ROLE_TERMINAL || config.getRole() != SystemProperties.SMARTX_ROLE_RULE) {
            api = new SmartXApiService(this);
            if (config.apiEnabled()) {
                api.start();
            }
        }
        smartxCore = new SmartxCore();
        smartxCore.start();
        //TODO:register shutdown hook
        Launcher.registerShutdownHook("kernel", this::stop);
        state = State.RUNNING;
    }
    protected void relocateDatabaseIfNeeded() {
    }
    /**
     * Moves database to another directory.
     *
     * @param srcDir
     * @param dstDir
     */
    private void moveDatabase(File srcDir, File dstDir) {
        // store the sub-folders
        File[] files = srcDir.listFiles();
        // create the destination folder
        dstDir.mkdirs();
        // move to destination
        for (File f : Objects.requireNonNull(files)) {
            f.renameTo(new File(dstDir, f.getName()));
        }
    }
    /**
     * Prints system info.
     */
    protected void printSystemInfo() {
        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            // computer system
            ComputerSystem cs = hal.getComputerSystem();
            logger.info("Computer: manufacturer = {}, model = {}" + cs.getManufacturer() + cs.getModel());
            // operating system
            OperatingSystem os = si.getOperatingSystem();
            logger.info("OS: name = {}" + os);
            // cpu
            CentralProcessor cp = hal.getProcessor();
            logger.info("CPU: processor = {}, cores = {} / {}" + cp + cp.getPhysicalProcessorCount() + cp.getLogicalProcessorCount());
            // memory
            GlobalMemory m = hal.getMemory();
            long mb = 1024L * 1024L;
            logger.info("Memory: total = {} MB, available = {} MB, swap total = {} MB, swap available = {} MB" + m.getTotal() / mb + m.getAvailable() / mb + m.getSwapTotal() / mb + (m.getSwapTotal() - m.getSwapUsed()) / mb);
            // disk
            for (HWDiskStore disk : hal.getDiskStores()) {
                logger.info("Disk: name = {}, size = {} MB" + disk.getName() + disk.getSize() / mb);
            }
            // network
            for (NetworkIF net : hal.getNetworkIFs()) {
                logger.info("Network: name = {}, ip = [{}]" + net.getDisplayName() + String.join(",", net.getIPv4addr()));
            }
            // java version
            logger.info("Java: version = {}, xmx = {} MB" + System.getProperty("java.version") + Runtime.getRuntime().maxMemory() / mb);
        } catch (RuntimeException e) {
            logger.error("Unable to retrieve System information.", e);
        }
    }
    /**
     * Stops the kernel.
     */
    public synchronized void stop() {
        if (state != State.RUNNING) {
            return;
        } else {
            state = State.STOPPING;
        }
        // stop consensus
        // stop API and p2p
        // api.stop();
        p2p.stop();
        // stop pending manager and node manager
        //pendingMgr.stop();
        nodeMgr.stop();
        // close client
        client.close();
        // make sure no thread is reading/writing the state
        //        ReentrantReadWriteLock.WriteLock lock = chain.getStateLock().writeLock();
        //        lock.lock();
        //        try {
        //            for (DatabaseName name : DatabaseName.values()) {
        //                dbFactory.getDB(name).close();
        //            }
        //        } finally {
        //            lock.unlock();
        //        }
        state = State.STOPPED;
    }
    /**
     * Returns the kernel state.
     *
     * @return
     */
    public State state() {
        return state;
    }
    public SmartxCore getSmartxCore() {
        return smartxCore;
    }
    /**
     * Returns the wallet.
     *
     * @return
     */
    public Key25519 getWallet() {
        return wallet;
    }
    /**
     * Returns the coinbase.
     *
     * @return
     */
    public Key getCoinbase() {
        return coinbase;
    }
    /**
     * Returns the blockchain.
     *
     * @return
     */
    //    public Blockchain getBlockchain() {
    //        return chain;
    //    }
    /**
     * Returns the peer client.
     *
     * @return
     */
    public PeerClient getClient() {
        return client;
    }
    /**
     * Returns the pending manager.
     *
     * @return
     */
    //    public PendingManager getPendingManager() {
    //        return pendingMgr;
    //    }
    /**
     * Returns the channel manager.
     *
     * @return
     */
    public ChannelManager getChannelManager() {
        return channelMgr;
    }
    /**
     * Returns the node manager.
     *
     * @return
     */
    public NodeManager getNodeManager() {
        return nodeMgr;
    }
    /**
     * Returns the node manager.
     *
     * @return
     */
    //    public SyncRequestManager getSyncRequestManager() {
    //        return syncReqMgr;
    //    }
    /**
     * Returns the config.
     *
     * @return
     */
    public SystemProperties getConfig() {
        return config;
    }
    /**
     * Get instance of Smartx API server
     *
     * @return API server
     */
    public SmartXApiService getApi() {
        return api;
    }
    /**
     * Returns the p2p server instance.
     *
     * @return a {@link PeerServer} instance or null
     */
    public PeerServer getP2p() {
        return p2p;
    }
    /**
     * Returns the database factory.
     *
     * @return
     */
    public DatabaseFactory getDbFactory() {
        return dbFactory;
    }
}
