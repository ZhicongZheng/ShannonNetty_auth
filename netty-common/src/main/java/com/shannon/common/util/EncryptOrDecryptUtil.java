package com.shannon.common.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class EncryptOrDecryptUtil {

    private static MessageDigest md5Digest;

    static {
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        Map<String, String> initKeys = generatorKey();
        Map<String, String>  tempKeys = generatorKey();

        System.out.println("*******************************************************");
        String serPubKey = initKeys.get("serPubKey");
        System.out.println("【服务端初始化公钥serPubKey】" + serPubKey);
        String serPriKey = initKeys.get("serPriKey");
        System.out.println("【服务端初始化私钥serPriKey】" + serPriKey);
        String cliPubKey = initKeys.get("cliPubKey");
        System.out.println("【客户端初始化公钥cliPubKey】" + cliPubKey);
        String cliPriKey = initKeys.get("cliPriKey");
        System.out.println("【客户端初始化私钥cliPriKey】" + cliPriKey);

        String tempSerPubKey = tempKeys.get("serPubKey");
        System.out.println("【服务端临时公钥tempSerPubKey】" + tempSerPubKey);
        String tempSerPriKey = tempKeys.get("serPriKey");
        System.out.println("【服务端临时私钥tempSerPriKey】" + tempSerPriKey);
        String tempCliPubKey = tempKeys.get("cliPubKey");
        System.out.println("【客户端临时公钥tempCliPubKey】" + tempCliPubKey);
        String tempCliPriKey = tempKeys.get("cliPriKey");
        System.out.println("【客户端临时公钥tempCliPriKey】" + tempCliPriKey);
        System.out.println("*******************************************************");


        String request = "{\"serviceHeader\":{\"serviceId\":\"1010\",\"responseCode\":\"000000\",\"requestMsg\":\"请求成功\"},\"serviceBody\":{\"sessionId\":\"bf2d5af85239439aa56db5a149ddaaac\",\"userId\":null,\"deviceId\":\"990009263463476\",\"lastAccessTime\":\"2018-06-11 14:30:21\"}}";
        String response = "{\"serviceId\":\"1010\",\"responseCode\":\"000000\",\"responseMsg\":\"请求成功\"}";

        //客户端加密
        System.out.println("start-------");
        Map<String, String> clientRequest = clientEncryption(request, tempCliPriKey, cliPriKey, serPubKey);
        System.out.println("end-------client encrypt result: 【secretContent】" + clientRequest.get("secretContent") + " 【signData】" + clientRequest.get("signData"));

        //服务端解密
        System.out.println("start-------");
        Map<String, String> serverResult = serverDecryption(clientRequest.get("secretContent"), clientRequest.get("signData"), tempCliPubKey, serPriKey, cliPubKey);
        System.out.println("end-------server decrypt result: " + serverResult);

        //服务端加密
        System.out.println("start-------");
        Map<String, String> serverResponse = serverEncryption(response, tempSerPriKey, cliPubKey, serPriKey);
        System.out.println("end-------server encrypt result: " + serverResponse);

        //客户端解密
        System.out.println("start-------");
        Map<String, String> clientResult = clientDecryption(serverResponse.get("secretContent"), serverResponse.get("signData"), tempSerPubKey, cliPriKey, serPubKey);
        System.out.println("end-------client encrypt result: " + clientResult);


    }

    /**
     * 生成密钥公共方法
     * @return
     */
    public static Map<String, String> generatorKey(){
        Map<String, String> keys = new HashMap();
        Provider provider = new BouncyCastleProvider();
        KeyPairGenerator keyPairGeneratorServer = null;
        KeyPairGenerator keyPairGeneratorClient = null;
        try {
            //1.服务端初始化密钥
            keyPairGeneratorServer = KeyPairGenerator.getInstance("ECDH", provider);
            keyPairGeneratorServer.initialize(256);
            KeyPair keyPairSer = keyPairGeneratorServer.generateKeyPair();

            //生成服务端密钥
            String serpubKey = Base64.getMimeEncoder().encodeToString(keyPairSer.getPublic().getEncoded());
            String serPriKey = Base64.getMimeEncoder().encodeToString(keyPairSer.getPrivate().getEncoded());

            //2.客户端初始化密钥
            keyPairGeneratorClient = KeyPairGenerator.getInstance("ECDH", provider);
            keyPairGeneratorClient.initialize(256);
            KeyPair keyPairCli = keyPairGeneratorClient.generateKeyPair();

            //生成客户端密钥
            String cliPubKey = Base64.getMimeEncoder().encodeToString(keyPairCli.getPublic().getEncoded());
            String cliPriKey = Base64.getMimeEncoder().encodeToString(keyPairCli.getPrivate().getEncoded());
            keys.put("serPubKey", serpubKey);
            keys.put("serPriKey", serPriKey);
            keys.put("cliPubKey", cliPubKey);
            keys.put("cliPriKey", cliPriKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return keys;
    }

    /**
     * Client加密
     * @param content   要加密的数据
     * @param temCliPriKey 客户端临时私钥
     * @param cliPriKey 客户端初始化私钥
     * @param serPubKey 服务端初始化公钥
     * @return
     */
    public static Map<String, String> clientEncryption(String content, String temCliPriKey, String cliPriKey, String serPubKey){
        System.out.println("开始数据加密........");
        Map<String, String> result = new HashMap();

        //生成签名数据
        String signData = sign(content, cliPriKey);
        System.out.println("签名：" + "【" + signData + "】");

        //密钥磋商
        String key = ecdhKey(temCliPriKey, serPubKey);
        System.out.println("加锁钥匙：ecdhKey【" + key + "】");
        String secretContent = doAES(content, key, Cipher.ENCRYPT_MODE);
        result.put("secretContent", secretContent);
        result.put("signData", signData);

        return result;
    }

    /**
     * Client解密
     * @param secretContent 加密数据
     * @param signData  数据签名
     * @param tmpSerPubKey  服务端临时公钥
     * @param cliPriKey 客户端初始化私钥
     * @param serPubKey 服务端初始化公钥
     * @return
     */
    public static Map<String, String> clientDecryption(String secretContent, String signData, String tmpSerPubKey, String cliPriKey, String serPubKey){
        System.out.println("开始客户端解密......");
        Map<String, String> result = new HashMap();
        String key = ecdhKey(cliPriKey, tmpSerPubKey);
        System.out.println("开锁钥匙：ecdhKey【" + key + "】");


        String textContent = doAES(secretContent, key, Cipher.DECRYPT_MODE);
        System.out.println("解密后内容：" + textContent);

        //验签
        boolean res = verify(textContent, serPubKey, signData);
        if(!res){
            System.err.println("验签失败！");
            return null;
        }
        result.put("textContent", textContent);
        result.put("res", res+"");
        return result;
    }


    /**
     * Server加密
     * @param content 明文请求
     * @param temSerPriKey 服务端临时私钥
     * @param cliPubKey 客户端初始化公钥
     * @param serPriKey 服务端初始化私钥
     * @return
     */
    public static Map<String, String> serverEncryption(String content, String temSerPriKey, String cliPubKey, String serPriKey){
        System.out.println("开始服务端解密......");
        Map<String, String> result = new HashMap();
        String signData = sign(content, serPriKey);
        System.out.println("加签【" + signData + "】");

        //密钥磋商
        String key = ecdhKey(temSerPriKey, cliPubKey);
        System.out.println("加密钥匙：ecdhKey【" + key + "】");
        String secretContent = doAES(content, key, Cipher.ENCRYPT_MODE);
        result.put("secretContent", secretContent);
        result.put("signData", signData);

        return result;
    }

    /**
     * Server解密
     * @param secrtContent 加密数据
     * @param signData  数据签名
     * @param tmpCliPubKey 客户端临时公钥
     * @param serPriKey 服务端初始化私钥
     * @param cliPubKey 客户端初始化公钥
     * @return
     */
    public static Map<String, String> serverDecryption(String secrtContent, String signData, String tmpCliPubKey, String serPriKey, String cliPubKey){

        Map<String, String> result = new HashMap();
        String key = ecdhKey(serPriKey, tmpCliPubKey);
        System.out.println("开锁钥匙【" + key + "】");


        String textContent = doAES(secrtContent, key, Cipher.DECRYPT_MODE);
        System.out.println("解密后内容：" + textContent);

        //验签
        boolean res = verify(textContent, cliPubKey, signData);
        result.put("textContent", textContent);
        result.put("res", res+"");

        return result;
    }

    /**
     * 生成签名数据
     * @param content 加密数据
     * @param priKey 私钥
     * @return
     */
    private static String sign(String content, String priKey) {
        try {
            byte[] keyBytes = Base64.getMimeDecoder().decode(priKey.getBytes("utf-8"));
            byte[] contentBytes = Base64.getMimeDecoder().decode(content.getBytes("utf-8"));

            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            Signature signature = Signature.getInstance("SHA1withECDSA");
            signature.initSign(privateKey);
            signature.update(contentBytes);
            return Base64.getMimeEncoder().encodeToString(signature.sign());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 密钥磋商
     * @param priKey 私钥
     * @param pubKey 公钥
     * @return
     */
    private static String ecdhKey(String priKey, String pubKey) {

        //使用ECDH-BC方法添加进环境信息内
        Security.addProvider(new BouncyCastleProvider());
        KeyAgreement akeyAgree = null;
        try {
            //初始化ECDH，KeyFactory
            KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "BC");
            //处理私钥
            byte[] priKeybytes = Base64.getMimeDecoder().decode(priKey.getBytes("utf-8"));
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(priKeybytes);
            PrivateKey ecPriKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            //处理公钥
            byte[] pubKeyBytes = Base64.getMimeDecoder().decode(pubKey.getBytes("utf-8"));
            X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(pubKeyBytes);
            PublicKey ecPubKey = keyFactory.generatePublic(pubX509);

            //密钥磋商生成新的密钥Byte数组
            akeyAgree = KeyAgreement.getInstance("ECDH", "BC");
            akeyAgree.init(ecPriKey);
            akeyAgree.doPhase(ecPubKey, true);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return Base64.getMimeEncoder().encodeToString(akeyAgree.generateSecret());
    }

    /**
     * AES加密数据
     * @param data
     * @param key
     * @param mode
     * @return
     */
    private static String doAES(String data, String key, int mode) {
        try {
            if (StringUtils.isEmpty(data) || StringUtils.isEmpty(key)) {
                return null;
            }
            boolean encrypt = mode == Cipher.ENCRYPT_MODE;
            byte[] content;
            //true 加密内容 false 解密内容
            if (encrypt) {
                content = data.getBytes("UTF-8");
            } else {
                content = parseHexStr2Byte(data);
            }
            SecretKeySpec keySpec = new SecretKeySpec(md5Digest.digest(key.getBytes("UTF-8")), "AES");//构造一个密钥
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(mode, keySpec);// 初始化
            byte[] result = cipher.doFinal(content);//加密或解密
            if (encrypt) {
                return parseByte2HexStr(result);
            } else {
                return new String(result, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将二进制转化成十六进制
     * @param result
     * @return
     */
    private static String parseByte2HexStr(byte[] result) {
        StringBuffer buffer = new StringBuffer();
        for(int i=0;i < result.length;i++){
            String hex = Integer.toHexString(result[i] & 0xFF);
            if(hex.length() == 1){
                hex = '0' + hex;
            }
            buffer.append(hex.toUpperCase());
        }
        return buffer.toString();
    }

    /**
     * 十六进制转换成二进制
     * @param content
     * @return
     */
    private static byte[] parseHexStr2Byte(String content) {

        if(content.length() < 1){
            return null;
        }
        int hexStr = content.length() / 2;
        byte[] result = new byte[hexStr];
        for(int i=0; i < hexStr; i++){
            int high = Integer.parseInt(content.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(content.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 验签方法
     * @param textContent 解密明文
     * @param pubKey 公钥
     * @param signData 签名
     * @return
     */
    private static boolean verify(String textContent, String pubKey, String signData) {
        try {
            byte[] keyBytes = Base64.getMimeDecoder().decode(pubKey.getBytes("utf-8"));
            byte[] contentBytes = Base64.getMimeDecoder().decode(textContent.getBytes("utf-8"));
            byte[] signBytes = Base64.getMimeDecoder().decode(signData.getBytes("utf-8"));
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

            Signature signature = Signature.getInstance("SHA1withECDSA");
            signature.initVerify(publicKey);
            signature.update(contentBytes);

            return signature.verify(signBytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return Boolean.parseBoolean(null);
    }

}
