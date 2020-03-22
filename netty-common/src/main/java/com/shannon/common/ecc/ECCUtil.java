package com.shannon.common.ecc;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NullCipher;

import sun.security.ec.ECKeyFactory;
import sun.security.ec.ECPrivateKeyImpl;
import sun.security.ec.ECPublicKeyImpl;


public class ECCUtil {


    /*
     * 注：
     *
     * 1、椭圆曲线定义：一条椭圆曲线在射影平面上满足方程：Y2Z+a1XYZ+a3YZ2=X3+a2X2Z+a4XZ2+a6Z3的所有点的集合，
     * 且曲线上每个点都是非奇异的。该方程又名维尔维斯特拉斯方程，椭圆曲线的形状不是椭圆，
     * 只是因为其描述的方程类似于计算一个椭圆周长的方程。转换到笛卡尔坐标系下的方程为： y2+a1xy+a3y = x3+a2x2+a4x+a6
     *
     * 椭圆曲线密码体制来源于对椭圆曲线的研究，所谓椭圆曲线指的是由韦尔斯特拉斯（Weierstrass）方程：
     * y2+a1xy+a3y=x3+a2x2+a4x+a6 (1)所确定的平面曲线。其中系数ai(I=1,2,…,6)定义在某个域上，
     * 可以是有理数域、实数域、复数域，还可以是有限域GF（pr），椭圆曲线密码体制中用到的椭圆曲线都是定义在有限域上的。
     *
     * 椭圆曲线上所有的点外加一个叫做无穷远点的特殊点构成的集合连同一个定义的加法运算构成一个Abel群。
     * 在等式mP=P+P+…+P=Q (2)中，已知m和点P求点Q比较容易，反之已知点Q和点P求m却是相当困难的，
     * 这个问题称为椭圆曲线上点群的离散对数问题。椭圆曲线密码体制正是利用这个困难问题设计而来。
     * 椭圆曲线应用到密码学上最早是由Neal Koblitz 和Victor Miller在1985年分别独立提出的。
     *
     * 解椭圆曲线上的离散对数问题的最好算法是Pollard rho方法，其时间复杂度为，是完全指数阶的。
     * 其中n为等式(2)中m的二进制表示的位数。当n=234, 约为2117，需要1.6x1023 MIPS 年的时间。
     * 而我们熟知的RSA所利用的是大整数分解的困难问题，目前对于一般情况下的因数分解的最好算法的时间复杂度是子指数阶的，
     * 当n=2048时，需要2x1020MIPS年的时间。也就是说当RSA的密钥使用2048位时，ECC的密钥使用234位所获得的安全强度还高出许多。
     * 它们之间的密钥长度却相差达9倍，当ECC的密钥更大时它们之间差距将更大。
     * 更ECC密钥短的优点是非常明显的，随加密强度的提高，密钥长度变化不大。
     *
     * 对椭圆曲线来说最流行的有限域是以素数为模的整数域（参见 模运算）GF(p)，或是特征为2的伽罗华域GF（2m）。
     * 後者在专门的硬件实现上计算更为有效，而前者通常在通用处理器上更为有效。专利的问题也是相关的。
     * 一些其他素数的伽罗华域的大小和能力也已经提出了，但被密码专家认为有一点问题。
     *
     * 无穷远点：射影几何中直线有一个无穷远点():就是无穷远点，直线的两端交于无穷远点（可把直线看作封闭曲线）.
     * 两条平行的直线可以看作相交在无穷远点，所有的平行直线都交于同一个无穷远点·
     *
     * Abel群：阿贝尔群也称为交换群或可交换群，它是满足其元素的运算不依赖于它们的次序（交换律公理）的群。
     * 阿贝尔群推广了整数集合的加法运算。阿贝尔群以挪威数学家尼尔斯·阿贝尔命名。
     *
     * 加法运算：任意取椭圆曲线上两点P、Q （若P、Q两点重合，则做P点的切线）做直线交于椭圆曲线的另一点R’，
     * 过R’做y轴的平行线交于R。我们规定P+Q=R。
     *
     * 阶：椭圆曲线上一点P，存在正整数n，使得nP=O∞，则n为P的阶，若n不存在，则P是无限阶的，
     * 有限域上定义的椭圆曲线上所有点的阶都存在。O∞+P=P，O∞为零元；曲线上三个点A,B,C处于一条直线上，则A+B+C=O∞；
     *
     * 2、加密过程
     * A选定一条椭圆曲线Ep（a,b），并取曲线上一点作为基点G
     * A选择一个私钥k，并生成公钥K=kG
     * A将Ep（a,b）和k，G发送给B
     * B收到后将明文编码到Ep（a,b）上一点M，并产生一个随机数r
     * B计算点C1=M+rK，C2=rG
     * B将C1，C2传给A
     * A计算C1-kC2=M+rkG-krG=M
     * A对M解码得到明文
     * 攻击者只能得到Ep（a,b），G，K，C1，C2，没有k就无法得到M。
     *
     * 签名验签流程
     * A选定一条椭圆曲线Ep（a，b），并取曲线上一点作为基点G
     * A选择一个私钥k，并生成公钥K=kG
     * A产生一个随机数r，计算R(x,y)=rG
     * A计算Hash=SHA(M)，M‘=M(modp)
     * A计算S=（Hash+M'k）/r(modp)
     * B获得S和M'，Ep(a,b)，K，R(x,y)
     * B计算Hash=SHA(M)，M'=M(modp)
     * B计算R'=（Hash*G+M'*K）/S=(Hash*G+M'*kG)*r/(Hash+M'k)=rG=R（x,y），若R'=R，则验签成功。
     * 以上加解密和签名验签流程只是一个例子，具体应用时可以利用K=kG这一特性变幻出多种加解密方式。
     *
     * 密钥磋商过程：
     * 假设密钥交换双方为Alice、Bob，其有共享曲线参数（椭圆曲线E、阶N、基点G）。
     * 1) Alice生成随机整数a，计算A=a*G。Bob生成随机整数b，计算B=b*G。
     * 2) Alice将A传递给Bob。A的传递可以公开，即攻击者可以获取A。由于椭圆曲线的离散对数问题是难题，所以攻击者不可以通过A、G计算出a。Bob将B传递给Alice。同理，B的传递可以公开。
     * 3) Bob收到Alice传递的A，计算Q=b*A
     * 4) Alice收到Bob传递的B，计算Q‘=a*B
     * Alice、Bob双方即得Q=b*A=b*(a*G)=(b*a)*G=(a*b)*G=a*(b*G)=a*B=Q' (交换律和结合律)，即双方得到一致的密钥Q。
     *
     * 加密和解密程序：ECC ElGamal和ECIES；签名：ECRSA；密钥磋商：ECDH
     *
     * 3、JDK目录仅支持密钥的生成和解析，暂不支持加解密（使用NullCipher替换Cipher）
     */

    public static final String ALGORITHM = "EC";
    public static final String TRANSFORMATION = "EC";

    public static KeyPair generateKey() throws Exception {

        // x^163+x^7+x^6+x^3+1
        ECFieldF2m ecField = new ECFieldF2m(163, new int[]{ 7, 6, 3 }); // 有限域（finite field）// 特征为2的伽罗华域GF（2m）

        // 韦尔斯特拉斯（Weierstrass）方程: y^2+a1xy+a3y=x^3+a2x^2+a4x+a6
        // 域F2n(n>=1)上的椭圆曲线的Weierstrass方程式为：y^2+xy=x^3+a2x^2+a6
        // y^2+xy=x^3+x^2+1
        EllipticCurve ellipticCurve = new EllipticCurve(ecField, new BigInteger("1", 2), new BigInteger("1", 2));

        ECPoint g = new ECPoint(new BigInteger("2fe13c0537bbc11acaa07d793de4e6d5e5c94eee8", 16),
                new BigInteger("289070fb05d38ff58321f2e800536d538ccdaa3d9", 16)); // 基点
        BigInteger n = new BigInteger("5846006549323611672814741753598448348329118574063", 10); // G的阶（order of generator）
        int h = 2; // the cofactor

        ECParameterSpec ecParameterSpec = new ECParameterSpec(ellipticCurve, g, n, h);
        // 公钥
        ECPublicKey publicKey = new ECPublicKeyImpl(g, ecParameterSpec);
        BigInteger s = new BigInteger("1234006549323611672814741753598448348329118574063", 10);
        // 私钥
        ECPrivateKey privateKey = new ECPrivateKeyImpl(s, ecParameterSpec);

        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        return keyPair;
    }

    public static byte[] encrypt(byte[] content, byte[] key) throws Exception {
        return null;
        /*PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = ECKeyFactory.INSTANCE;
        ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(keySpec);
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKey.getS(), privateKey.getParams());

        Cipher cipher = new NullCipher(); // TODO Chipher不支持EC算法 未能实现
        cipher.init(Cipher.ENCRYPT_MODE, privateKey, privateKeySpec.getParams());
        return cipher.doFinal(content);*/
    }

    public static byte[] decrypt(byte[] content, byte[] key) throws Exception {
        return null;
        /*X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
        KeyFactory keyFactory = ECKeyFactory.INSTANCE;
        ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(keySpec);
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(publicKey.getW(), publicKey.getParams());

        Cipher cipher = new NullCipher(); // TODO Chipher不支持EC算法 未能实现
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, publicKeySpec.getParams());
        return cipher.doFinal(content);*/
    }

    public static void main(String[] args) throws Exception {

        KeyPair keyPair = generateKey();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        System.out.println(privateKey);
        System.out.println(publicKey);

        System.out.println(Arrays.toString(encrypt("椭圆曲线密码体制来源于对椭圆曲线的研究".getBytes(), privateKey.getEncoded())));
        System.out.println(new String(decrypt(encrypt("椭圆曲线密码体制来源于对椭圆曲线的研究".getBytes(), privateKey.getEncoded()), publicKey.getEncoded())));

        /*
         * 控制台输出：
         *
         * sun.security.ec.ECPrivateKeyImpl@ffffe9f5
         * Sun EC public key, 163 bits
         *   public x coord: 4373527398576640063579304354969275615843559206632
         *   public y coord: 3705292482178961271312284701371585420180764402649
         *   parameters: java.security.spec.ECParameterSpec@1ea21c07
         * [-26, -92, -83, -27, -100, -122, -26, -101, -78, -25, -70, -65, -27, -81, -122, -25, -96, -127, -28, -67, -109, -27, -120, -74, -26, -99, -91, -26, -70, -112, -28, -70, -114, -27, -81, -71, -26, -92, -83, -27, -100, -122, -26, -101, -78, -25, -70, -65, -25, -102, -124, -25, -96, -108, -25, -87, -74]
         * 椭圆曲线密码体制来源于对椭圆曲线的研究
         */
    }

}