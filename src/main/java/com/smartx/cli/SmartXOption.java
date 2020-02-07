package com.smartx.cli;
/**
 Smartx launcher options.
 */
public enum SmartXOption {
    HELP("help"), VERSION("version"), ACCOUNT("account"), CHANGE_PASSWORD("changepassword"), DATA_DIR("datadir"), COINBASE("coinbase"), PASSWORD("password"), DUMP_PRIVATE_KEY("dumpprivatekey"), IMPORT_PRIVATE_KEY("importprivatekey"), NETWORK("network"), HD_WALLET("hdwallet"), RPCCLIENT("rpcclient"), RPCSERVER("rpcserver"), REINDEX("reindex");
    private final String name;
    SmartXOption(String s) {
        name = s;
    }
    @Override
    public String toString() {
        return this.name;
    }
}