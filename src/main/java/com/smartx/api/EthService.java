package com.smartx.api;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;

import com.smartx.message.CliMessages;
import com.smartx.util.Tools;

public class EthService {
    //private static String URL = CliMessages.get("EthNodeService1");
    private static String URL = CliMessages.get("EthNodeService1");
    private static Logger logger = Logger.getLogger(EthService.class);
    private static HttpService httpService;
    public static Web3j initWeb3j() {
        return Web3j.build(getService());
    }
    public static Admin initAdmin() {
        return Admin.build(getService());
    }
    public static String getVersion() {
        Web3j web3 = initWeb3j();
        try {
            Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().sendAsync().get();
            return web3ClientVersion.getWeb3ClientVersion();
        } catch (Exception e) {
        }
        return "";
    }
    private static HttpService getService() {
        if (httpService == null) {
            httpService = new HttpService(URL);
        }
        return httpService;
    }
    public static Credentials LoadAccont(String filename, String passwd) {
        Credentials ct = null;
        try {
            ct = WalletUtils.loadCredentials(passwd, filename);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }
        return ct;
    }
    public static double getTokenBalance(String address) throws ExecutionException, InterruptedException {
        Web3j web3j = Web3j.build(new HttpService(URL));
        Function function = new Function("balanceOf", Arrays.asList(new Address(address)),  // Solidity Types in smart contract functions
                Arrays.asList(new TypeReference<Type>() {
                }));
        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(address, "0x1f0f468ee03a6d99cd8a09dd071494a83dc1c0e5",
                                        encodedFunction), DefaultBlockParameterName.LATEST).sendAsync().get();
        String returnValue = response.getValue(); //返回16进制余额
        if (null == returnValue) return 0;
        returnValue = returnValue.substring(2);
        BigInteger balance = new BigInteger(returnValue, 16);
        return  Double.parseDouble(balance.toString())/10000;
    }
    @Test
    public void TestBatchShowBalance() throws ExecutionException, InterruptedException {
        String path = "c:\\mywallet";//CliMessages.get("ETHWindowsPath1");
        File file = new File(path);
        File[] fs = file.listFiles();
        for(File f:fs){
            if(!f.isDirectory()) {
                //System.out.println(f);
                //System.out.println(f.getAbsolutePath());
                Credentials credentials = LoadAccont(f.getAbsolutePath(), "123");
                System.out.println(credentials.getAddress() +"#" +
                        EthService.getTokenBalance(credentials.getAddress()));
            }
        }
    }
    @Test
    public void TestGetPathFile(){
        String path = CliMessages.get("ETHWindowsPath1");
        File file = new File(path);
        File[] fs = file.listFiles();
        for(File f:fs){
            if(!f.isDirectory()) {
                //System.out.println(f);
                System.out.println(f.getAbsolutePath());
                Credentials credentials = LoadAccont(f.getAbsolutePath(), "123");
                System.out.println(f.getAbsolutePath() + "#########" + credentials.getAddress());
                Tools.WriteFile("c:\\erc.txt", credentials.getAddress()+"\r\n");
            }
        }
    }
}
