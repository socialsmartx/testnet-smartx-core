package com.smartx.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.smartx.api.ResInfo;
import com.smartx.block.Block;
import com.smartx.core.blockchain.SatPeerManager;
// import com.smartx.util.HexUtil;
// import com.smartx.util.HexUtil2;
import com.smartx.core.consensus.SatException;

public class ECDSAMain {
    static String src = "suxianhua";
    public static String[] getPairKey() {
        try {
            KeyPair keyPair = getKeyPair();
            ECPublicKey ecPublicKey = getRSPublicKey(keyPair);
            ECPrivateKey ecPrivateKey = getESAPrivateKey(keyPair);
            byte[] publicKeyEnc = ecPublicKey.getEncoded();
            byte[] privateKeyEnc = ecPrivateKey.getEncoded();
            //			String str1 = HexUtil2.encodeHexStr(publicKeyEnc);
            //			System.out.println("hex2-pub:" + str1);
            //			System.out.println("length:" + publicKeyEnc.length);
            //
            //			String str2 = HexUtil2.encodeHexStr(privateKeyEnc);
            //			System.out.println("hex2-prv:" + str2);
            //			System.out.println("length:" + privateKeyEnc.length);
            //
            //			String pubstr =  Base64.encodeBase64String(publicKeyEnc);
            //			String prvstr = Base64.encodeBase64String(privateKeyEnc);
            //
            //			String[] strs = new String[2];
            //			strs[0] = pubstr;
            //			strs[1] = prvstr;
            //	return strs;
            return null;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    // 密钥组1
    // privkey:MD4CAQAwEAYHKoZIzj0CAQYFK4EEAAoEJzAlAgEBBCAn4Zh5XMNURRIj8aphLSkaDTQCzziRDKPP
    // CjTF23lh/g==
    // pubkey:MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEqLxjgFOU6A1y40bgR9oY5DOEg6cVlGET/WknHh9R7P14
    //akDiMG9bD4gA9631NtPjC01EYf6QlcJ0DnhfueowZQ==
    //
    // 密钥组2
    //
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, SignatureException, SatException, InterruptedException {
        // get priv pub key
        KeyPair keyPair = getKeyPair();
        ECPublicKey ecPublicKey = getRSPublicKey(keyPair);
        ECPrivateKey ecPrivateKey = getESAPrivateKey(keyPair);
        byte[] publicKeyEnc = ecPublicKey.getEncoded();
        byte[] privateKeyEnc = ecPrivateKey.getEncoded();
        System.out.println("publickey-length:" + publicKeyEnc.length);
        System.out.println("privkey-length:" + privateKeyEnc.length);
        String pubstr = Base64.encodeBase64String(publicKeyEnc);
        String prvstr = Base64.encodeBase64String(privateKeyEnc);
        System.out.println("privkey:" + prvstr);
        System.out.println("pubkey:" + pubstr);
        // sign
        byte[] result = sign(privateKeyEnc);
        System.out.println("after sign:" + Base64.encodeBase64String(result));
        // verify
        boolean ok = verify(publicKeyEnc, result);
        System.out.println("verify result:" + ok);
        //		String pubkey2 = HexUtil.Bytes2HexString(publicKeyEnc);
        //		String prvkey2 = HexUtil.Bytes2HexString(privateKeyEnc);
        //
        //		byte[] privkey = HexUtil.HexString2Bytes(pubkey2);
        //
        //		System.out.println("pub2:" + pubkey2);

		/*
		try
		{
			General_mining.CreateAddr();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
        Block blk = new Block();//General_mining.CreateMainBlock();
        blk.header.timestamp = 23123131;
        String data = blk.toString();
        System.out.println("data:" + data);
        String[] strs = ECDSAMain.getPairKey();
        System.out.println("pub:[" + strs[0] + "] priv:[" + strs[1] + "]");
        String crptoStr = ECDSAMain.signString(strs[1], data);
        //System.out.println("crpto:[" + crptoStr + "]");
        String getstr1 = "http://106.75.168.107:8001/xfer?from=0f7859b6fcc8c12a95c38f942fb5497ac6fa3b3dc16ce840c3e4e353cf2a2113&to=eef22eaf97724e3220c9344cb5fcaadd636d4596ef5870cf54c95a01bd58400a&amount=10";
        //String getstr2 = "http://106.75.168.107:8003/xfer?from=0f7859b6fcc8c12a95c38f942fb5497ac6fa3b3dc16ce840c3e4e353cf2a2113&to=eef22eaf97724e3220c9344cb5fcaadd636d4596ef5870cf54c95a01bd58400a&amount=10";
        String getstr3 = "http://106.75.132.200:8002/xfer?from=0f7859b6fcc8c12a95c38f942fb5497ac6fa3b3dc16ce840c3e4e353cf2a2113&to=eef22eaf97724e3220c9344cb5fcaadd636d4596ef5870cf54c95a01bd58400a&amount=10";
        //String getstr4 = "http://106.75.132.200:8004/xfer?from=0f7859b6fcc8c12a95c38f942fb5497ac6fa3b3dc16ce840c3e4e353cf2a2113&to=eef22eaf97724e3220c9344cb5fcaadd636d4596ef5870cf54c95a01bd58400a&amount=10";
        ArrayList<String> getstrs = new ArrayList<>();
        getstrs.add(getstr1);
        //getstrs.add(getstr2);
        getstrs.add(getstr3);
        //getstrs.add(getstr4);
        SatPeerManager peer = new SatPeerManager();
        Random rd = new Random();
        for (int i = 0; i < 50000000; i++) {
            int tmp = rd.nextInt(2);
            String myurl = getstrs.get(tmp);
            System.out.println(myurl);
            String ok1 = peer.PutJsonCmd(myurl, "");
            Gson gson = new GsonBuilder().create();
            ResInfo info = gson.fromJson(ok1, new TypeToken<ResInfo>() {
            }.getType());
            System.out.println(info.ret);
            Thread.sleep(100);
        }
    }
    /**
     verify sign

     @param publicKeyEnc
     @param result
     @return
     @throws NoSuchAlgorithmException
     @throws InvalidKeySpecException
     @throws InvalidKeyException
     @throws SignatureException        */
    public static boolean verify(byte[] publicKeyEnc, byte[] result) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyEnc);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        // tongguo publicKeyEnc get pub key
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Signature signature = Signature.getInstance("SHA1withECDSA");
        signature.initVerify(publicKey);
        // src = src + "w";
        signature.update(src.getBytes());
        boolean ok = signature.verify(result); // verify result
        return ok;
    }
    // 使用公钥验证
    public static int signVerify(String pubKey, String data, String src) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        byte[] publicKeyEnc = Base64.decodeBase64(pubKey);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyEnc);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        // tongguo publicKeyEnc get pub key
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Signature signature = Signature.getInstance("SHA1withECDSA");
        signature.initVerify(publicKey);
        signature.update(src.getBytes());
        boolean ok = signature.verify(data.getBytes()); // verify result
        if (ok) {
            return 0;
        }
        return 1;
    }
    // 使用私钥进行签名
    public static String signString(String privKey, String data) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        byte[] privkeysrc = Base64.decodeBase64(privKey);
        KeyFactory keyFactory;
        keyFactory = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privkeysrc);
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        // build sign
        Signature signature = Signature.getInstance("SHA1withECDSA");
        signature.initSign(priKey);
        signature.update(data.getBytes());
        byte[] result = signature.sign(); // afer sign
        return new String(result);
    }
    /**
     sign 签名

     @param privateKeyEnc
     @return
     @throws NoSuchAlgorithmException
     @throws InvalidKeySpecException
     @throws InvalidKeyException
     @throws SignatureException        */
    public static byte[] sign(byte[] privateKeyEnc) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyEnc);
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        // build sign
        Signature signature = Signature.getInstance("SHA1withECDSA");
        signature.initSign(priKey);
        signature.update(src.getBytes());
        byte[] result = signature.sign(); // afer sign
        return result;
    }
    /**
     general priv key

     @param keyPair
     @return
     */
    public static ECPrivateKey getESAPrivateKey(KeyPair keyPair) {
        ECPrivateKey ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();
        return ecPrivateKey;
    }
    /**
     general pub key

     @param keyPair
     @return
     */
    public static ECPublicKey getRSPublicKey(KeyPair keyPair) {
        ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();
        return ecPublicKey;
    }
    /**
     general pair

     @return
     @throws NoSuchAlgorithmException
     */
    private static KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
        try {
            keyPairGenerator.initialize(ecGenParameterSpec, new SecureRandom());
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //keyPairGenerator.initialize(128);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }
}
