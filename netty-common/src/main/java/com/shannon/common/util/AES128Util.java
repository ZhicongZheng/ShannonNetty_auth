package com.shannon.common.util;

import lombok.experimental.UtilityClass;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

@UtilityClass
public class AES128Util {

    public final String algorithmStr="AES/ECB/PKCS5Padding";

    public byte[] shannonEnCrypt(byte[] enCryptSourceData,Key key)
    {
        Cipher cipher; //加密运算对象
        byte[] enCryptedData=null;
        try {
            cipher=Cipher.getInstance(algorithmStr);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            enCryptedData=cipher.doFinal(enCryptSourceData);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return enCryptedData;

    }

    public byte[] shannonDeCrypt(byte[] deCryptSourceData,Key key) {

        byte[] originBytes=null;
        Cipher cipher2 = null;
        try {
            cipher2=Cipher.getInstance(algorithmStr);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }

        try {

            cipher2.init(Cipher.DECRYPT_MODE,key);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        //解密
        try {
            originBytes=cipher2.doFinal(deCryptSourceData);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return originBytes;
    }


}
