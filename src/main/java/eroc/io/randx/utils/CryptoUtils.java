package eroc.io.randx.utils;

import com.google.protobuf.ByteString;
import eroc.io.randx.pojo.Encoding;
import org.bouncycastle.asn1.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CryptoUtils {

    private CryptoUtils() {
    }


    /**
     * 生成keypair
     *
     * @param algorithm EC
     * @param stdName   secp256k1
     * @return
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     */
//    public static KeyPair generatorKeyPair(String algorithm, String stdName) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);//EC,DiffieHellman,DSA,RSA
//        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(stdName);
//        keyPairGenerator.initialize(256);
//        keyPairGenerator.initialize(ecGenParameterSpec, new SecureRandom());
//        return keyPairGenerator.generateKeyPair();
//    }

    /**
     * ECDH加密
     *
     * @param
     * @param msg
     * @return
     */
    public static byte[] ECDHEncrypt(byte[] publicKey, byte[] msg, KeyPair keyPair) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, NoSuchPaddingException, InvalidKeySpecException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey pk = keyFactory.generatePublic(x509EncodedKeySpec);
//        KeyPair keyPair = generatorKeyPair("EC", "secp256r1");//stdName:secp256k1
        PublicKey epk = keyPair.getPublic();
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(keyPair.getPrivate());
        keyAgreement.doPhase(pk, true);
        byte[] secret = keyAgreement.generateSecret();
        byte[] encKey = Arrays.copyOfRange(secret, 0, 16);
        String macKey = TypeUtils.bytesToHexString(Arrays.copyOfRange(secret, 16, 32));
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[16];//iv作为初始化向量
        secureRandom.nextBytes(iv);
        byte[] ct = AES256.AES_cbc_encrypt(msg, encKey, iv);//加密数据
        ByteString pct = ByteString.copyFrom(ct);
        ByteString pepk = ByteString.copyFrom(epk.getEncoded());
        ByteString piv = ByteString.copyFrom(iv);
        Encoding.ecies dataToMac = Encoding.ecies.newBuilder()
                .setCiphertext(pct)
                .setEphemPublicKey(pepk)
                .setIv(piv)
                .build();
        String mac = SHA256.sha256_HMAC(dataToMac.toString(), macKey);//hash（ct,epk,iv）
        ByteString pmac = ByteString.copyFrom(mac.getBytes());
        Encoding.ecies obj = Encoding.ecies.newBuilder().setIv(piv).setEphemPublicKey(pepk).setCiphertext(pct).setMac(pmac).build();
        return obj.toByteArray();
    }


    /**
     * ECDH解密
     *
     * @param
     * @param body
     * @return
     */
    public static byte[] ECDHDecrypt(byte[] privateKey, byte[] body) throws Exception {
        Encoding.ecies ecies = Encoding.ecies.parseFrom(body);
        byte[] ct = ecies.getCiphertext().toByteArray();
        byte[] iv = ecies.getIv().toByteArray();
        String bmac = ecies.getMac().toStringUtf8();
        byte[] pepk = ecies.getEphemPublicKey().toByteArray();
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pepk);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PublicKey epk = kf.generatePublic(x509EncodedKeySpec);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey sk = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(sk);
        keyAgreement.doPhase(epk, true);
        byte[] secret = keyAgreement.generateSecret();
        byte[] encKey = Arrays.copyOfRange(secret, 0, 16);
        String macKey = TypeUtils.bytesToHexString(Arrays.copyOfRange(secret, 16, 32));
        Encoding.ecies dataToMac = Encoding.ecies.newBuilder()
                .setCiphertext(ByteString.copyFrom(ct))
                .setEphemPublicKey(ByteString.copyFrom(pepk))
                .setIv(ByteString.copyFrom(iv))
                .build();
        String mac = SHA256.sha256_HMAC(dataToMac.toString(), macKey);
        if (!mac.equals(bmac)) {
            throw new Exception("Corrupted body - unmatched authentication code");
        }
        byte[] msg = AES256.AES_cbc_decrypt(ct, encKey, iv);
        return msg;
    }


    public static byte[] sign(byte[] privateKey, byte[] message) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey sk = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(sk);
        signature.update(message);
        return signature.sign();
    }

    public static boolean verify(byte[] publicKey, byte[] signed, byte[] message) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey pk = keyFactory.generatePublic(x509EncodedKeySpec);
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(pk);
        signature.update(message);
        return signature.verify(signed);
    }

    private static ASN1Primitive toAsn1Primitive(byte[] data) throws Exception {
        try(ByteArrayInputStream inStream = new ByteArrayInputStream(data);
            ASN1InputStream asnInputStream = new ASN1InputStream(inStream);) {
            return asnInputStream.readObject();
        }
    }

    public static List<String> generateRS(byte[] sign) throws Exception {
        List<String> l = new ArrayList<>();
        ASN1Primitive asn1 = toAsn1Primitive(sign);
        if (asn1 instanceof ASN1Sequence) {
            ASN1Sequence asn1Sequence = (ASN1Sequence) asn1;
            ASN1Encodable[] asn1Encodables = asn1Sequence.toArray();
            for(ASN1Encodable asn1Encodable : asn1Encodables) {
                ASN1Primitive asn1Primitive = asn1Encodable.toASN1Primitive();
                if (asn1Primitive instanceof ASN1Integer) {
                    ASN1Integer asn1Integer = (ASN1Integer) asn1Primitive;
                    BigInteger value = asn1Integer.getValue();
                    String s = value.toString(16);
                    if (s.length() % 2 == 0) {
                        l.add(s);
                    } else {
                        s = "0" + s;
                        l.add(s);
                    }
                }
            }
        }
        return l;
    }


    public static byte[] generateSign(String pr,String ps) throws IOException {
        BigInteger r = new BigInteger("4b4da36d6b22b4d0471c64bad49ee434280f07350463b58ef6a05856c2296a5c", 16);
        BigInteger s = new BigInteger("a26a184751c619dd5399ebe0eb72b843479a9cb304ad10d464a5184e3d2f1f6c", 16);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DERSequenceGenerator seq = new DERSequenceGenerator(b);
        seq.addObject(new ASN1Integer(r));
        seq.addObject(new ASN1Integer(s));
        seq.close();
        byte[] sign = b.toByteArray();
        b.close();
        return sign;
    }


    public static void main(String[] args) throws Exception {
        String msg = "a message for java-js interop purpose";


        //根据sign生成r s
//        for(int i = 0; i < 10; i++) {
//            BigInteger d = new BigInteger("628f142e96a2ba15f29f13ec85f3aeb9ec56fe0b3df30bea68870a19bbb7fba0", 16);
//            KeyPair pair = Secp256r1.generateKeyPair(d.toByteArray());
//            PrivateKey aPrivate = pair.getPrivate();
//            byte[] signature = sign(aPrivate.getEncoded(), msg.getBytes());
//            ASN1Primitive asn1 = toAsn1Primitive(signature);
//            if (asn1 instanceof ASN1Sequence) {
//                ASN1Sequence asn1Sequence = (ASN1Sequence) asn1;
//                ASN1Encodable[] asn1Encodables = asn1Sequence.toArray();
//                for(ASN1Encodable asn1Encodable : asn1Encodables) {
//                    ASN1Primitive asn1Primitive = asn1Encodable.toASN1Primitive();
//                    if (asn1Primitive instanceof ASN1Integer) {
//                        ASN1Integer asn1Integer = (ASN1Integer) asn1Primitive;
//                        BigInteger value = asn1Integer.getValue();
//                        String s = value.toString(16);
//                        if (s.length() % 2 == 0) {
//                            System.out.println(s);
//                        } else {
//                            s = "0" + s;
//                            System.out.println(s);
//                        }
//                    }
//                }
//            }
//        }


        //根据 r 和s 生成sign
//        BigInteger r = new BigInteger("4b4da36d6b22b4d0471c64bad49ee434280f07350463b58ef6a05856c2296a5c", 16);
//        BigInteger s = new BigInteger("a26a184751c619dd5399ebe0eb72b843479a9cb304ad10d464a5184e3d2f1f6c", 16);
//        ByteArrayOutputStream b = new ByteArrayOutputStream();
//        DERSequenceGenerator seq = new DERSequenceGenerator(b);
//        seq.addObject(new ASN1Integer(r));
//        seq.addObject(new ASN1Integer(s));
//        seq.close();
//        System.out.println(TypeUtils.bytesToHexString(b.toByteArray()));


        //设置x ,y点生成pk
//        BigInteger x = new BigInteger("61166327685726372633146956112430165093840350805695271212904644383394079735442");
//        BigInteger y = new BigInteger("58046343023627330736346952573191938034837742142120314188975834647179723108352");
//        ECPoint epk = new ECPoint(x, y);
//        X9ECParameters ecCurve = ECNamedCurveTable.getByName("secp256r1");
//        ECParameterSpec ecps = new ECNamedCurveSpec("secp256r1", ecCurve.getCurve(), ecCurve.getG(), ecCurve.getN(), ecCurve.getH(), ecCurve.getSeed());
//        ECPublicKeySpec ecpk = new ECPublicKeySpec(epk, ecps);
//        KeyFactory keyFactory = KeyFactory.getInstance("EC");
//        PublicKey publicKey = keyFactory.generatePublic(ecpk);


        //ECDH密钥交换
//        KeyPair akeyPair = generatorKeyPair("EC", "secp256k1");
//        KeyPair bkeyPair = generatorKeyPair("EC", "secp256k1");
//        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");//akeyagreement
//        keyAgreement.init(akeyPair.getPrivate());//ask
//        keyAgreement.doPhase(bkeyPair.getPublic(), true);//bpk
//        byte[] bytes = keyAgreement.generateSecret();
//        KeyAgreement bkeyAgreement = KeyAgreement.getInstance("ECDH");//bkeyagreement
//        bkeyAgreement.init(bkeyPair.getPrivate());//bpk
//        bkeyAgreement.doPhase(akeyPair.getPublic(), true);//ask
//        byte[] bbytes = keyAgreement.generateSecret();
//        System.out.println(TypeUtils.bytesToHexString(bbytes).equals(TypeUtils.bytesToHexString(bytes)));


//
        // security生成公私钥
//        Security.addProvider(new BouncyCastleProvider());
//        ECGenParameterSpec ecGenSpec = new ECGenParameterSpec("secp256k1");
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
//        keyPairGenerator.initialize(ecGenSpec, new SecureRandom());
//        java.security.KeyPair pair = keyPairGenerator.generateKeyPair();
//        ECPrivateKey privateKey = (ECPrivateKey) pair.getPrivate();
//        ECPublicKey publicKey = (ECPublicKey) pair.getPublic();
//        System.out.println("security gennerator:");
//        System.out.println(privateKey);
//        System.out.println(TypeUtils.bytesToHexString(publicKey.getW().getAffineX().toByteArray()));
//        System.out.println(TypeUtils.bytesToHexString(publicKey.getW().getAffineY().toByteArray()));


        //手动设置privateKey，生成publicKey
//        Security.addProvider(new BouncyCastleProvider());
//        KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "BC");
//        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
//        BigInteger k = new BigInteger("3", 16);
//        ECPoint Q = ecSpec.getG().multiply(k);
//        byte[] publicDerBytes = Q.getEncoded(false);
//        ECPoint point = ecSpec.getCurve().decodePoint(publicDerBytes);
//        ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);
//        ECPublicKey ecPublicKey = (ECPublicKey) keyFactory.generatePublic(pubSpec);
//        System.out.println(TypeUtils.bytesToHexString(ecPublicKey.getEncoded()));


    }


}
