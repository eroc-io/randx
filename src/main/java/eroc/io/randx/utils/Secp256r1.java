package eroc.io.randx.utils;


//import eroc.io.randx.pojo.Buffer;
//import org.bouncycastle.asn1.x9.ECNamedCurveTable;
//import org.bouncycastle.asn1.x9.X9ECParameters;
//import org.bouncycastle.jce.spec.ECNamedCurveSpec;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECPoint;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * 基于secp256r1曲线生成pk，sk
 */
public class Secp256r1 {

    private static final String SK_SECP256R1 = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCA=";
    private static final String PK_SECP256R1 = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgA";

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
//        byte[] nb = n.toByteArray();
        byte[] privateKey = Base64.getDecoder().decode(SK_SECP256R1);
        int nl = seed.length, sl = privateKey.length;
        privateKey = Arrays.copyOf(privateKey, nl + sl);
        System.arraycopy(seed, 0, privateKey, sl, nl);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory skf = KeyFactory.getInstance("EC");
        PrivateKey sk = skf.generatePrivate(pkcs8EncodedKeySpec);
        BigInteger x = scalmult.getAffineX();
        BigInteger y = scalmult.getAffineY();
        byte[] xb = TypeUtils.lastNBytes(x.toByteArray(), 32);
        byte[] yb = TypeUtils.lastNBytes(y.toByteArray(), 32);
        byte[] pb = Base64.getDecoder().decode(PK_SECP256R1);
        int xl = xb.length, yl = yb.length, pl = pb.length;
        byte[] publicKey = new byte[xl + yl + pl + 1];
        System.arraycopy(pb, 0, publicKey, 0, pl);
        publicKey[pl] = (byte) 4;
        System.arraycopy(xb, 0, publicKey, pl + 1, xl);
        System.arraycopy(yb, 0, publicKey, pl + xl + 1, yl);
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



}
