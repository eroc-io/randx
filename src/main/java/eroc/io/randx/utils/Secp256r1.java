package eroc.io.randx.utils;


//import eroc.io.randx.pojo.Buffer;
//import org.bouncycastle.asn1.x9.ECNamedCurveTable;
//import org.bouncycastle.asn1.x9.X9ECParameters;
//import org.bouncycastle.jce.spec.ECNamedCurveSpec;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;

/**
 * 基于secp256r1曲线生成pk，sk
 */
public class Secp256r1 {

    private static final String SK_SECP256R1 = "3041020100301306072a8648ce3d020106082a8648ce3d030107042730250201010420";
    private static final String PK_SECP256R1 = "3059301306072A8648CE3D020106082A8648CE3D030107034200";


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
        String hexSk = SK_SECP256R1 + TypeUtils.bytesToHexString(n.toByteArray());
        byte[] privatKey = TypeUtils.hexStringToByte(hexSk);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privatKey);
        KeyFactory skf = KeyFactory.getInstance("EC");
        PrivateKey sk = skf.generatePrivate(pkcs8EncodedKeySpec);
        BigInteger x = scalmult.getAffineX();
        BigInteger y = scalmult.getAffineY();
        String hexPk = PK_SECP256R1 + "04" + x.toString(16) + y.toString(16);
        byte[] publicKey = TypeUtils.hexStringToByte(hexPk);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory pkf = KeyFactory.getInstance("EC");
        PublicKey pk = pkf.generatePublic(x509EncodedKeySpec);
        return new KeyPair(pk, sk);
    }



//    public static KeyPair old(byte[] seed) throws NoSuchAlgorithmException, InvalidKeySpecException {
//        BigInteger n = new BigInteger(seed);
//        ECPoint G = new ECPoint(XG, YG);
//        ECPoint scalmult = ECC.scalmult(G, n);
//        ECPoint pk = new ECPoint(scalmult.getAffineX(), scalmult.getAffineY());
//        X9ECParameters ecCurve = ECNamedCurveTable.getByName("secp256r1");
//        ECParameterSpec ecps = new ECNamedCurveSpec("secp256r1", ecCurve.getCurve(), ecCurve.getG(), ecCurve.getN(), ecCurve.getH(), ecCurve.getSeed());
//        ECPrivateKeySpec ecsk = new ECPrivateKeySpec(n, ecps);
//        ECPublicKeySpec ecpk = new ECPublicKeySpec(pk, ecps);
//        KeyFactory keyFactory = KeyFactory.getInstance("EC");
//        PrivateKey privateKey = keyFactory.generatePrivate(ecsk);
//        PublicKey publicKey = keyFactory.generatePublic(ecpk);
//        return new KeyPair(publicKey, privateKey);
//    }


    public static void main(String[] args) throws Exception {
        String msg = "hello world";
        BigInteger b = new BigInteger("e549e4c45e21c8d3479775a11429e7b78e603dbe58674627d4c1e3aae4644965", 16);

        KeyPair pair = generateKeyPair(b.toByteArray());
        System.out.println(pair.getPublic());
        System.out.println(TypeUtils.bytesToHexString(pair.getPublic().getEncoded()));
        System.out.println(TypeUtils.bytesToHexString(pair.getPrivate().getEncoded()));
//
//        Buffer.EciesBody eciesBody = CryptoUtils.ECDHEncrypt(pair.getPublic().getEncoded(), msg.getBytes(), pair);
//        byte[] bytes = CryptoUtils.ECDHDecrypt(pair.getPrivate().getEncoded(), eciesBody);
//        System.out.println(new String(bytes));


//        KeyPair old = old(b.toByteArray());
//        System.out.println(TypeUtils.bytesToHexString(old.getPublic().getEncoded()));
//        System.out.println(TypeUtils.bytesToHexString(old.getPrivate().getEncoded()));
//
//        Buffer.EciesBody eb = CryptoUtils.ECDHEncrypt(old.getPublic().getEncoded(), msg.getBytes(), old);
//        byte[] b1 = CryptoUtils.ECDHDecrypt(old.getPrivate().getEncoded(), eb);
//        System.out.println(new String(b1));





    }


}
