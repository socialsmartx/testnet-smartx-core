package com.smartx.wallet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

import javax.validation.constraints.NotNull;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.*;
import org.web3j.utils.Numeric;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartx.crypto.ECKey;

import io.github.novacrypto.base58.Base58;

public class SmartXFastWallet {
    private static final SecureRandom SECURE_RANDOM;
    private String address;
    private ECKeyPair ecKeyPair;
    private KeyPair keyPair;
    public static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    static final ECDomainParameters CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
    static final BigInteger HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
    static {
        SECURE_RANDOM = new SecureRandom();
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    public static byte recIdFromV(int v) {
        if (v >= 31) {
            // compressed
            v -= 4;
        }
        return (byte) (v - 27);
    }
    public SmartXFastWallet(ECKeyPair ecKeyPair) {
        this.ecKeyPair = ecKeyPair;
        this.address = Keys.getAddress(ecKeyPair);
    }
    public String getAddress() {
        return Keys.getAddress(ecKeyPair);
    }
    public String getPublicKey() {
        return Numeric.encodeQuantity(ecKeyPair.getPublicKey());
    }
    public String getPrivateKey() {
        return Numeric.encodeQuantity(ecKeyPair.getPrivateKey());
    }
    public ECKeyPair getEcKeyPair() {
        return this.ecKeyPair;
    }
    public ECKey getECKey() {
        return ECKey.fromPrivate(ecKeyPair.getPrivateKey());
    }
    public static Credentials loadAccount(String filename, String passwd) {
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
    // 通过助记词生成账户
    public static void createAccountByMnemonics() {
        File fileDir = new File("c:\\MyWallet");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
    }
    public static SmartXFastWallet createAccount(@NotNull String pwd, @NotNull String destinationDirectory) {
        try {
            File destDir = new File(destinationDirectory);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
            keyPairGenerator.initialize(ecGenParameterSpec, SECURE_RANDOM);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            ECKeyPair ecKeyPair = ECKeyPair.create(keyPair);
            //keystore文件名
            String filename = WalletUtils.generateWalletFile(pwd, ecKeyPair, destDir, false);
            String address = Keys.getAddress(ecKeyPair);
            String msg = "fileName:\n" + filename + "\nprivateKey:\n" + Numeric.encodeQuantity(ecKeyPair.getPrivateKey()) + "\nPublicKey:\n" + Numeric.encodeQuantity(ecKeyPair.getPublicKey()) + "\nAddress:\n" + address;
            System.out.println("create:" + msg);
            return new SmartXFastWallet(ecKeyPair);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static SmartXFastWallet fromKeyStore(String password, String filePath) throws Exception {
        StringBuilder keystoreString = new StringBuilder();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        WalletFile walletFile = null;
        InputStreamReader inputStreamReader = null;
        try {
            int charread = 0;
            char[] tempchars = new char[1024];
            inputStreamReader = new InputStreamReader(new FileInputStream(filePath));
            while ((charread = inputStreamReader.read(tempchars)) != -1) {
                // 屏蔽掉\r不显示
                if ((charread == tempchars.length) && (tempchars[tempchars.length - 1] != '\r')) {
                    System.out.print(tempchars);
                } else {
                    for (int i = 0; i < charread; i++) {
                        if (tempchars[i] == '\r') {
                            continue;
                        } else {
                            System.out.print(tempchars[i]);
                        }
                    }
                }
                keystoreString.append(tempchars);
            }
            walletFile = objectMapper.readValue(keystoreString.toString(), WalletFile.class);
            ECKeyPair keyPair = Wallet.decrypt(password, walletFile);
            System.out.println("Private Key: " + Numeric.encodeQuantity(keyPair.getPrivateKey()));
            System.out.println("Public Key: " + Numeric.encodeQuantity(keyPair.getPublicKey()));
            return new SmartXFastWallet(keyPair);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (inputStreamReader != null) inputStreamReader.close();
        }
    }
    public String sign(byte[] rawHash) {
        ECKey ecKey = getECKey();
        ECKey.ECDSASignature ecdsaSignature = ecKey.sign(rawHash);
        return ecdsaSignature.toBase58();
    }
    //    public static boolean verify(String message,String base58Sig) {
    //        byte[] rawhash = HashUtil.sha3(message.getBytes());
    //        ECKey.ECDSASignature recoverSig = ECKey.ECDSASignature.decodeFromBase58(base58Sig);
    //        byte[] pubKey = ECKey.recoverPubBytesFromSignature(recIdFromV(recoverSig.v), recoverSig, rawhash);
    //        return ECKey.verify(rawhash,recoverSig,pubKey);
    //    }
    public static boolean verify(String message, String base58Sig, byte[] pubKey) throws SignatureException {
        byte[] rawhash = Base58.base58Decode(message);
        ECKey.ECDSASignature recoverSig = ECKey.ECDSASignature.decodeFromBase58(base58Sig);
        ECKey ecKeyRec = ECKey.signatureToKey(rawhash, recoverSig);
        //compare the public key
        ECKey ecKeyRaw = ECKey.fromPublicOnly(pubKey);
        return ecKeyRec.equals(ecKeyRaw);
    }
    public static boolean verify(String message, String base58Sig, String address) throws SignatureException {
        byte[] rawhash = Base58.base58Decode(message);
        ECKey.ECDSASignature recoverSig = ECKey.ECDSASignature.decodeFromBase58(base58Sig);
        ECKey ecKeyRec = ECKey.signatureToKey(rawhash, recoverSig);
        String addressRec = Numeric.toHexStringNoPrefix(ecKeyRec.getAddress());
        // TODO
        // return addressRec.equals(address);
        return true;
    }
}
