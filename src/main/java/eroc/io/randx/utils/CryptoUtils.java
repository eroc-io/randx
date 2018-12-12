package eroc.io.randx.utils;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import eroc.io.randx.pojo.Buffer;
import org.bouncycastle.asn1.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

//import org.bouncycastle.asn1.*;

public class CryptoUtils {

    private CryptoUtils() {
    }


    private static final String PK_SECP384R1 = "3076301006072A8648CE3D020106052B81040022036200";
    private static final String PK_SECP521R1 = "30819B301006072A8648CE3D020106052B8104002303818600";

    private static final byte[] PK_SECP256R1 = Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgA");


    /**
     * ECDH加密
     *
     * @param
     * @param msg
     * @return
     */
    public static Buffer.EciesBody ECDHEncrypt(byte[] publicKey, byte[] msg, KeyPair keyPair) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, NoSuchPaddingException, InvalidKeySpecException, UnsupportedEncodingException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey pk = keyFactory.generatePublic(x509EncodedKeySpec);
        PublicKey epk = keyPair.getPublic();
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(keyPair.getPrivate());
        keyAgreement.doPhase(pk, true);
        byte[] secret = keyAgreement.generateSecret();
        secret = SHA256.getSHA256Bytes(secret);
        byte[] encKey = Arrays.copyOfRange(secret, 0, 16);
        byte[] macKey = Arrays.copyOfRange(secret, 16, 32);
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[16];//iv作为初始化向量
        secureRandom.nextBytes(iv);
        byte[] ct = AES256.AES_cbc_encrypt(msg, encKey, iv);//加密数据
        byte[] pepk = TypeUtils.bufferPk(epk.getEncoded());
        byte[] dataToMac = TypeUtils.concatByteArrays(new byte[][]{iv, pepk, ct});
        byte[] mac = SHA256.sha256_HMAC(dataToMac, macKey);
        ByteString pmac = ByteString.copyFrom(mac);
        return Buffer.EciesBody.newBuilder().setIv(ByteString.copyFrom(iv)).setEpk(ByteString.copyFrom(pepk)).setCipher(ByteString.copyFrom(ct)).setMac(pmac).build();
    }

    /**
     * ECDH解密
     *
     * @param
     * @param
     * @return
     */
    public static byte[] ECDHDecrypt(byte[] privateKey, Buffer.EciesBody ecies) throws Exception {
        byte[] ct = ecies.getCipher().toByteArray();
        byte[] iv = ecies.getIv().toByteArray();
        byte[] bmac = ecies.getMac().toByteArray();
        byte[] pepk = TypeUtils.formatPK(ecies.getEpk().toByteArray());//formart pk
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
        secret = SHA256.getSHA256Bytes(secret);
        byte[] encKey = Arrays.copyOfRange(secret, 0, 16);
        byte[] macKey = Arrays.copyOfRange(secret, 16, 32);
        byte[] bpk = TypeUtils.bufferPk(pepk);//buffer pk
        byte[] dataToMac = TypeUtils.concatByteArrays(new byte[][]{iv, bpk, ct});
        byte[] mac = SHA256.sha256_HMAC(dataToMac, macKey);
        if (!Arrays.equals(mac, bmac)) {
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


    public static List<byte[]> generateRS(byte[] sign) throws Exception {
        List<byte[]> l = new ArrayList<>();
        ASN1Primitive asn1 = toAsn1Primitive(sign);
        if (asn1 instanceof ASN1Sequence) {
            ASN1Sequence asn1Sequence = (ASN1Sequence) asn1;
            ASN1Encodable[] asn1Encodables = asn1Sequence.toArray();
            for(ASN1Encodable asn1Encodable : asn1Encodables) {
                ASN1Primitive asn1Primitive = asn1Encodable.toASN1Primitive();
                if (asn1Primitive instanceof ASN1Integer) {
                    ASN1Integer asn1Integer = (ASN1Integer) asn1Primitive;
                    BigInteger value = asn1Integer.getValue();
                    byte[] bytes = value.toByteArray();
                    if (bytes.length > 32) {
                        bytes = TypeUtils.lastNBytes(bytes, 32);
                    }
                    l.add(bytes);
                }
            }
        }
        return l;
    }

    private static ASN1Primitive toAsn1Primitive(byte[] data) throws Exception {
        try(ByteArrayInputStream inStream = new ByteArrayInputStream(data);
            ASN1InputStream asnInputStream = new ASN1InputStream(inStream);) {
            return asnInputStream.readObject();
        }
    }


    public static byte[] rsGenSign(byte[] msg) throws IOException {

        byte[] r = Arrays.copyOfRange(msg, 0, 32);
        byte[] s = Arrays.copyOfRange(msg, 32, 64);
        if ((r[0] & 0x80) != 0) {
            r = TypeUtils.concatByteArrays(new byte[][]{new byte[]{0}, r});
        }
        if ((s[0] & 0x80) != 0) {
            s = TypeUtils.concatByteArrays(new byte[][]{new byte[]{0}, s});
        }
        return genSign(r, s);
    }


    public static byte[] encodeLength(int l) {
        if (l < 0x80) {
            return new byte[]{(byte) l};
        } else {
            List<Byte> t = new ArrayList<>();
            while (l > 0) {
                t.add((byte) (0x80 | (l & 0x7f)));
                l >>= 7;
            }
            Collections.reverse(t);
            return Bytes.toArray(t);
        }
    }


    private static byte[] toolsGenSign(byte[] rb, byte[] sb) throws IOException {
        BigInteger r = new BigInteger(rb);
        BigInteger s = new BigInteger(sb);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DERSequenceGenerator seq = new DERSequenceGenerator(b);
        seq.addObject(new ASN1Integer(r));
        seq.addObject(new ASN1Integer(s));
        seq.close();
        return b.toByteArray();
    }

    /**
     * r|s生成sign
     *
     * @param r
     * @param s
     * @return
     */
    private static byte[] genSign(byte[] r, byte[] s) {
        BigInteger rint = new BigInteger(r);
        BigInteger sint = new BigInteger(s);
        byte[] rbytes = rint.toByteArray();
        byte[] sbytes = sint.toByteArray();
        byte[] tag = {0x02};
        int rl = rbytes.length;
        int sl = sbytes.length;
        byte[] rEncodl = encodeLength(rl);
        byte[] sEncodl = encodeLength(sl);
        return TypeUtils.concatByteArrays(new byte[][]{new byte[]{0x30}, encodeLength(rl + sl + 2 + rEncodl.length + sEncodl.length), tag, rEncodl, rbytes, tag, sEncodl, sbytes});
    }
}