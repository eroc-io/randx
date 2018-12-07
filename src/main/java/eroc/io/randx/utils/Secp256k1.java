package eroc.io.randx.utils;

import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.*;

public class Secp256k1 {



//    public static void main(String[] args) throws Exception {
//
//        BigInteger xg = new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
//        BigInteger yg = new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);
//        ECPoint G = new ECPoint(xg, yg);
//        BigInteger n = new BigInteger("25F22FFFFFFFFFFFFFFF333FFF3F432EBAAEDCE6AF48A03BBFD25E8CD0364140", 16);
//        ECPoint scalmult = ECC.scalmult(G, n);
//        ECPoint pk = new ECPoint(scalmult.getAffineX(), scalmult.getAffineY());
//        X9ECParameters ecCurve = ECNamedCurveTable.getByName("secp256k1");
//        ECParameterSpec ecps = new ECNamedCurveSpec("secp256k1", ecCurve.getCurve(), ecCurve.getG(), ecCurve.getN(), ecCurve.getH(), ecCurve.getSeed());
//        ECPrivateKeySpec ecsk = new ECPrivateKeySpec(n, ecps);
//        ECPublicKeySpec ecpk = new ECPublicKeySpec(pk, ecps);
//        try {
//            KeyFactory keyFactory = KeyFactory.getInstance("EC");
//            PrivateKey privateKey = keyFactory.generatePrivate(ecsk);
//            PublicKey publicKey = keyFactory.generatePublic(ecpk);
//            System.out.println(privateKey.getEncoded().length);
//            System.out.println(publicKey);
//            System.out.println(publicKey.getEncoded().length);
//            long l1 = System.currentTimeMillis();
//            byte[] bytes = CryptoUtils.ECDHEncrypt(publicKey.getEncoded(), "hello ECC! hello ECIES!".getBytes());
//            long l2 = System.currentTimeMillis();
//            System.out.println(l2-l1);
//            byte[] bytes1 = CryptoUtils.ECDHDecrypt(privateKey.getEncoded(), bytes);
//            System.out.println(new String(bytes1));
//            System.out.println(System.currentTimeMillis()-l2);
//        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
//            e.printStackTrace();
//        }
//
//
//    }


}
