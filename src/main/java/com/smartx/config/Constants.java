package com.smartx.config;

import java.math.BigInteger;

public class Constants {
    private static final int MAXIMUM_EXTRA_DATA_SIZE = 32;
    private static final int MIN_GAS_LIMIT = 125000;
    private static final int GAS_LIMIT_BOUND_DIVISOR = 1024;
    private static final BigInteger MINIMUM_DIFFICULTY = BigInteger.valueOf(131072);
    private static final BigInteger DIFFICULTY_BOUND_DIVISOR = BigInteger.valueOf(2048);
    private static final int EXP_DIFFICULTY_PERIOD = 100000;
    private static final int UNCLE_GENERATION_LIMIT = 7;
    private static final int UNCLE_LIST_LIMIT = 2;
    private static final int BEST_NUMBER_DIFF_LIMIT = 100;
    public static final String HASH_ALGORITHM = "BLAKE2B-256";
    public static final String CONFIG_DIR = "config";
    public static final String DEFAULT_DATA_DIR = ".";
    /**
     Version of this client.
     */
    public static final String CLIENT_VERSION = "1.0.0";
    public static final String CLIENT_NAME = "Smartx";
    public static final String DATABASE_DIR = "database";
    public static final int DEFAULT_P2P_PORT = 5161;
    public static final int DEFAULT_CONNECT_TIMEOUT = 4000;
    /**
     The number of blocks per day.
     */
    public static final long BLOCKS_PER_DAY = 2L * 60L * 24L;
    //private static final BigInteger BLOCK_REWARD = EtherUtil.convert(1500, EtherUtil.Unit.FINNEY);  // 1.5 ETH
    private static final BigInteger SECP256K1N = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);
    private static final int LONGEST_CHAIN = 192;
    public int getDURATION_LIMIT() {
        return 8;
    }
    public BigInteger getInitialNonce() {
        return BigInteger.ZERO;
    }
    public int getMAXIMUM_EXTRA_DATA_SIZE() {
        return MAXIMUM_EXTRA_DATA_SIZE;
    }
    public int getMIN_GAS_LIMIT() {
        return MIN_GAS_LIMIT;
    }
    public int getGAS_LIMIT_BOUND_DIVISOR() {
        return GAS_LIMIT_BOUND_DIVISOR;
    }
    public BigInteger getMINIMUM_DIFFICULTY() {
        return MINIMUM_DIFFICULTY;
    }
    public BigInteger getDIFFICULTY_BOUND_DIVISOR() {
        return DIFFICULTY_BOUND_DIVISOR;
    }
    public int getEXP_DIFFICULTY_PERIOD() {
        return EXP_DIFFICULTY_PERIOD;
    }
    public int getUNCLE_GENERATION_LIMIT() {
        return UNCLE_GENERATION_LIMIT;
    }
    public int getUNCLE_LIST_LIMIT() {
        return UNCLE_LIST_LIMIT;
    }
    public int getBEST_NUMBER_DIFF_LIMIT() {
        return BEST_NUMBER_DIFF_LIMIT;
    }
    //    public BigInteger getBLOCK_REWARD() {
    //        return BLOCK_REWARD;
    //    }
    public int getMAX_CONTRACT_SZIE() {
        return Integer.MAX_VALUE;
    }
    /**
     Introduced in the Homestead release
     */
    public boolean createEmptyContractOnOOG() {
        return true;
    }
    /**
     New DELEGATECALL opcode introduced in the Homestead release. Before Homestead this opcode should generate
     exception
     */
    public boolean hasDelegateCallOpcode() {
        return false;
    }
    /**
     Introduced in the Homestead release
     */
    public static BigInteger getSECP256K1N() {
        return SECP256K1N;
    }
    public static int getLONGEST_CHAIN() {
        return LONGEST_CHAIN;
    }
}
