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
        byte[] privateKey = TypeUtils.concatByteArrays(new byte[][]{Base64.getDecoder().decode(SK_SECP256R1), seed});
        PrivateKey sk = KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        byte[] publicKey = TypeUtils.concatByteArrays(new byte[][]{Base64.getDecoder().decode(PK_SECP256R1), new byte[]{4}, TypeUtils.lastNBytes(scalmult.getAffineX().toByteArray(), 32), TypeUtils.lastNBytes(scalmult.getAffineY().toByteArray(), 32)});
        PublicKey pk = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(publicKey));
        return new KeyPair(pk, sk);
    }


}
