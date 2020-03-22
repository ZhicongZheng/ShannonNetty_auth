package com.shannon.common.util;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;

public class ECComputeUtil {
    public static void main(String[] args) {
        // 曲线参数
        X9ECParameters ecp = SECNamedCurves.getByName("secp256r1");
        ECDomainParameters domainParameters = new ECDomainParameters(ecp.getCurve(), ecp.getG(), ecp.getN(), ecp.getH(), ecp.getSeed());

        // 生成公私钥对
        AsymmetricCipherKeyPair keyPair;
        ECKeyGenerationParameters keyGenerationParameters = new ECKeyGenerationParameters(domainParameters, new SecureRandom());
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        generator.init(keyGenerationParameters);
        keyPair = generator.generateKeyPair();

        ECPublicKeyParameters publicKeyParameters = (ECPublicKeyParameters) keyPair.getPublic();
        ECPrivateKeyParameters privateKeyParameters = (ECPrivateKeyParameters) keyPair.getPrivate();

        BigInteger privateKey = privateKeyParameters.getD();

        System.out.println("privateKey： "+privateKey.toString(16));
        System.out.println("publicKey x: "+publicKeyParameters.getQ().getXCoord().toBigInteger().toString(16));
        System.out.println("publicKey y: "+publicKeyParameters.getQ().getYCoord().toBigInteger().toString(16));

        ECPoint Q = keyGenerationParameters.getDomainParameters().getG().multiply(privateKey);
        // 好像在 1.60 版本之前不需要这一步， 这个搞了我好久
        Q = ECAlgorithms.importPoint(Q.getCurve(), Q).normalize();

        System.out.println(" multiply x: " + Q.getXCoord().toBigInteger().toString(16));
        System.out.println(" multiply y: " + Q.getYCoord().toBigInteger().toString(16));
    }

}
