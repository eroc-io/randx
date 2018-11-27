package eroc.io.randx.utils;

import eroc.io.randx.service.DeckDealer;
import eroc.io.randx.service.impl.PlayServiceImpl;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;

/**
 * 基于secp256r1曲线生成pk，sk
 */
public class Secp256r1 {

    //secp256r1基点
    private static final BigInteger XG = new BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16);
    private static final BigInteger YG = new BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16);

    /**
     * 生成keypair
     *
     * @param seed
     * @return
     * @throws Exception
     */
    public static KeyPair generateKeyPair(byte[] seed) throws Exception {
        BigInteger n = new BigInteger(seed);
        ECPoint G = new ECPoint(XG, YG);
        ECPoint scalmult = ECC.scalmult(G, n);
        ECPoint pk = new ECPoint(scalmult.getAffineX(), scalmult.getAffineY());
        X9ECParameters ecCurve = ECNamedCurveTable.getByName("secp256r1");
        ECParameterSpec ecps = new ECNamedCurveSpec("secp256r1", ecCurve.getCurve(), ecCurve.getG(), ecCurve.getN(), ecCurve.getH(), ecCurve.getSeed());
        ECPrivateKeySpec ecsk = new ECPrivateKeySpec(n, ecps);
        ECPublicKeySpec ecpk = new ECPublicKeySpec(pk, ecps);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(ecsk);
        PublicKey publicKey = keyFactory.generatePublic(ecpk);
        return new KeyPair(publicKey, privateKey);
    }


//    public static void main(String[] args) throws Exception {
//        BigInteger xg = new BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16);
//        BigInteger yg = new BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16);
//        ECPoint G = new ECPoint(xg, yg);
//        BigInteger n = new BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632550", 16);
//        ECPoint scalmult = ECC.scalmult(G, n);
//        ECPoint pk = new ECPoint(scalmult.getAffineX(), scalmult.getAffineY());
//        X9ECParameters ecCurve = ECNamedCurveTable.getByName("secp256r1");
//        ECParameterSpec ecps = new ECNamedCurveSpec("secp256r1", ecCurve.getCurve(), ecCurve.getG(), ecCurve.getN(), ecCurve.getH(), ecCurve.getSeed());
//        ECPrivateKeySpec ecsk = new ECPrivateKeySpec(n, ecps);
//        ECPublicKeySpec ecpk = new ECPublicKeySpec(pk, ecps);
//        KeyFactory keyFactory = KeyFactory.getInstance("EC");
//        PrivateKey privateKey = keyFactory.generatePrivate(ecsk);
//        PublicKey publicKey = keyFactory.generatePublic(ecpk);
//        KeyPair pair = new KeyPair(publicKey, privateKey);
//
//        System.out.println(Arrays.toString(pair.getPrivate().getEncoded()));
//        System.out.println(Arrays.toString(n.toByteArray()));
//        System.out.println(publicKey);
//        System.out.println(Arrays.toString(pair.getPublic().getEncoded()));
//        long l1 = System.currentTimeMillis();
//        byte[] bytes = CryptoUtils.ECDHEncrypt(publicKey.getEncoded(), "hello ECC! hello ECIES!".getBytes());
//        long l2 = System.currentTimeMillis();
//        System.out.println(l2 - l1);
//        byte[] bytes1 = CryptoUtils.ECDHDecrypt(privateKey.getEncoded(), bytes);
//        System.out.println(new String(bytes1));
//        System.out.println(System.currentTimeMillis() - l2);
//
//        byte[] sign = CryptoUtils.sign(privateKey.getEncoded(), "123".getBytes());
//
//        boolean verify = CryptoUtils.verify(publicKey.getEncoded(), sign, "123".getBytes());
//        System.out.println(verify);
//    }


}
