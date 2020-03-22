package com.shannon.common.util;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class ECSignUtil {
    public static final String ALGORITHM = "EC";
    public static final String SIGN_ALGORITHM = "SHA256withECDSA";

    // 初始化密钥对
    public static KeyPair initKey() {
        try {
            //KeyPairGenerator 类用于在非对称密钥加密算法中生成公钥和私钥对。密钥对生成器是使用 getInstance 工厂方法构造的
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            //比特币中使用的就是这个该曲线
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256k1");
            generator.initialize(ecGenParameterSpec, new SecureRandom());
            generator.initialize(256);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 获取公钥字节数组
    public static byte[] getPublicKey(KeyPair keyPair) {
        return keyPair.getPublic().getEncoded();
    }

    // 获取公钥字符串
    public static String getPublicKeyStr(KeyPair keyPair) {
        byte[] bytes = keyPair.getPublic().getEncoded();
        return encodeHex(bytes);
    }

    // 获取私钥字节数组
    public static byte[] getPrivateKey(KeyPair keyPair) {
        return keyPair.getPrivate().getEncoded();
    }

    // 获取私钥字符串
    public static String getPrivateKeyStr(KeyPair keyPair) {
        byte[] bytes = keyPair.getPrivate().getEncoded();
        return encodeHex(bytes);
    }

    /**
     * 数字签名
     * @param data 要签名的数据
     * @param privateKey 私钥
     * @param signAlgorithm 签名算法
     * @return
     */
    public static byte[] sign(byte[] data, byte[] privateKey, String signAlgorithm) {
        try {
            // 还原使用
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey priKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            // 1、实例化Signature
            Signature signature = Signature.getInstance(signAlgorithm);
            // 2、初始化Signature
            signature.initSign(priKey);
            // 3、更新数据
            signature.update(data);
            // 4、签名
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 验证
    public static boolean verify(byte[] data, byte[] publicKey, byte[] sign, String signAlgorithm) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            // 1、实例化Signature
            Signature signature = Signature.getInstance(signAlgorithm);
            // 2、初始化Signature
            signature.initVerify(pubKey);
            // 3、更新数据
            signature.update(data);
            // 4、签名
            return signature.verify(sign);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 16进制编码转为字符串
    public static String encodeHex(final byte[] data) {
        return encodeHex(data, true);
    }

    // 数据转16进制编码
    public static String encodeHex(final byte[] data, final boolean toLowerCase) {
        final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        final char[] toDigits = toLowerCase ? DIGITS_LOWER : DIGITS_UPPER;
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return new String(out);
    }

    public static void main(String[] args) {
        String str = "物联网远程养殖";
        byte[] data = str.getBytes();
        // 初始化密钥度
        KeyPair keyPair = initKey();
        System.out.println(getPublicKeyStr(keyPair));
        System.out.println(getPrivateKeyStr(keyPair));
        /*byte[] publicKey = getPublicKey(keyPair);
        byte[] privateKey = getPrivateKey(keyPair);
        // 签名
        byte[] sign = sign(data, privateKey, SIGN_ALGORITHM);
        // 验证
        boolean b = verify(data, publicKey, sign, SIGN_ALGORITHM);
        for (byte value : sign) {
            System.out.print(value);
        }*/

    }


}

