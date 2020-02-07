package com.smartx.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 @author Tenpay(Tencent) */
public class SatConfig extends PropertiesConfiguration {
    /**
     *
     */
    public final static String RelayIpKeyName = "RELAYIP";
    /**
     *
     */
    public final static String RelayPortKeyName = "RELAYPORT";
    /**
     *
     */
    public final static String RelayTimeoutKeyName = "RELAYTIMEOUT";
    /**
     *
     */
    public final static String CommSpidKeyName = "COMM_SPID";
    /**
     *
     */
    public final static String RelayVersionKeyName = "RELAY_VERSION";
    /**
     *
     */
    public final static String EnvironmentKeyName = "ENVIRONMENT";
    /**
     *
     */
    public final static String SenderServerIpKey = "SENDER_SERVER_IP";
    /**
     *
     */
    public final static String SenderServerPortKey = "SENDER_SERVER_PORT";
    /**
     *
     */
    public final static String BackerServerIpKey = "BACKER_SERVER_IP";
    /**
     *
     */
    public final static String BackerServerPortKey = "BACKER_SERVER_PORT";
    public final static String BankServerIpKey = "BANK_SERVER_IP";
    public final static String BankServerPortKey = "BANK_SERVER_PORT";
    /**
     *
     */
    private static SatConfig instance;
    /**
     *
     */
    private String ocppAppPath;
    /**
     *
     */
    private String ocppAppName;
    /**
     Constructor
     */
    public SatConfig() {
        this.ocppAppPath = null;
        this.ocppAppName = "None";
    }
    /**
     @return
     */
    public static synchronized SatConfig getInstance() {
        if (SatConfig.instance == null) {
            SatConfig.instance = new SatConfig();
        }
        return SatConfig.instance;
    }
    /**
     @param configFilename
     @throws ConfigurationException
     */
    public void initialize(String configFilename) throws ConfigurationException {
        //this.clear();
        //try {
        this.load(configFilename);
        //} catch (ConfigurationException e) {
        // TODO Auto-generated catch block
        //	e.printStackTrace();
        //}
    }
    /**
     *
     */
    @Override
    public void addProperty(String key, Object value) {
        Object previousValue = this.getProperty(key);
        if (previousValue != null) {
            this.clearProperty(key);
        }
        super.addProperty(key, value);
    }
    /**
     @return the ocppAppPath
     */
    public String getOcppAppPath() {
        return ocppAppPath;
    }
    /**
     @param ocppAppPath the ocppAppPath to set
     */
    public void setOcppAppPath(String ocppAppPath) {
        this.ocppAppPath = ocppAppPath;
    }
    /**
     @return
     */
    public String getOcppAppName() {
        return ocppAppName;
    }
    /**
     @param ocppAppName
     */
    public void setOcppAppName(String ocppAppName) {
        this.ocppAppName = ocppAppName;
    }
}
