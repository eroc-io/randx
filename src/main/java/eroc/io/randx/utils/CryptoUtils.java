package eroc.io.randx.utils;

import com.google.protobuf.ByteString;
import eroc.io.randx.pojo.Buffer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

//import org.bouncycastle.asn1.*;

public class CryptoUtils {

    private CryptoUtils() {
    }

    private static final String PK_SECP256R1 = "3059301306072A8648CE3D020106082A8648CE3D030107034200";
    private static final String PK_SECP384R1 = "3076301006072A8648CE3D020106052B81040022036200";
    private static final String PK_SECP521R1 = "30819B301006072A8648CE3D020106052B8104002303818600";


    /**
     * 给pk加上前缀
     *
     * @param publicKey PK.raw
     * @return formatPK
     */
    public static byte[] formatPK(byte[] publicKey) {
        byte[] prefix = TypeUtils.hexStringToByte(PK_SECP256R1);
        int fl = prefix.length;
        int pl = publicKey.length;
        int l = pl + fl;
        byte[] pk = new byte[l];
        System.arraycopy(prefix, 0, pk, 0, fl);
        System.arraycopy(publicKey, 0, pk, fl, pl);
        return pk;
    }


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
//        KeyPair keyPair = generatorKeyPair("EC", "secp256r1");//stdName:secp256k1
        PublicKey epk = keyPair.getPublic();
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(keyPair.getPrivate());
        keyAgreement.doPhase(pk, true);
        byte[] secret = keyAgreement.generateSecret();
        byte[] encKey = Arrays.copyOfRange(secret, 0, 16);
        byte[] macKey = Arrays.copyOfRange(secret, 16, 32);
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[16];//iv作为初始化向量
        secureRandom.nextBytes(iv);
        byte[] ct = AES256.AES_cbc_encrypt(msg, encKey, iv);//加密数据
        ByteString pct = ByteString.copyFrom(ct);
        ByteString pepk = ByteString.copyFrom(epk.getEncoded());
        ByteString piv = ByteString.copyFrom(iv);
        Buffer.EciesBody dataToMac = Buffer.EciesBody.newBuilder()
                .setCipher(pct)
                .setEpk(pepk)
                .setIv(piv)
                .build();
        String mac = SHA256.sha256_HMAC(dataToMac.toString(), macKey);//hash（ct,epk,iv）
        ByteString pmac = ByteString.copyFrom(mac, "utf-8");
        return Buffer.EciesBody.newBuilder().setIv(piv).setEpk(pepk).setCipher(pct).setMac(pmac).build();
    }


    /**
     * ECDH解密
     *
     * @param
     * @param
     * @return
     */
    public static byte[] ECDHDecrypt(byte[] privateKey, Buffer.EciesBody ecies) throws Exception {
//        Buffer.EciesBody ecies = Buffer.EciesBody.parseFrom(body);
        byte[] ct = ecies.getCipher().toByteArray();
        byte[] iv = ecies.getIv().toByteArray();
        String bmac = ecies.getMac().toStringUtf8();
        byte[] pepk = ecies.getEpk().toByteArray();
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
        byte[] macKey = Arrays.copyOfRange(secret, 16, 32);
        Buffer.EciesBody dataToMac = Buffer.EciesBody.newBuilder()
                .setCipher(ByteString.copyFrom(ct))
                .setEpk(ByteString.copyFrom(pepk))
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

    public static byte[] rsGnSign(byte[] msg) {
        Integer l = msg.length / 2;
        byte[] rb = new byte[l];
        byte[] sb = new byte[l];
        System.arraycopy(msg, 0, rb, 0, l);
        System.arraycopy(msg, l, sb, 0, l);
        return gnSign(rb, sb);
    }


//    public static List<String> generateRS(byte[] sign) throws Exception {
//        List<String> l = new ArrayList<>();
//        ASN1Primitive asn1 = toAsn1Primitive(sign);
//        if (asn1 instanceof ASN1Sequence) {
//            ASN1Sequence asn1Sequence = (ASN1Sequence) asn1;
//            ASN1Encodable[] asn1Encodables = asn1Sequence.toArray();
//            for(ASN1Encodable asn1Encodable : asn1Encodables) {
//                ASN1Primitive asn1Primitive = asn1Encodable.toASN1Primitive();
//                if (asn1Primitive instanceof ASN1Integer) {
//                    ASN1Integer asn1Integer = (ASN1Integer) asn1Primitive;
//                    BigInteger value = asn1Integer.getValue();
//                    String s = value.toString(16);
//                    if (s.length() % 2 == 0) {
//                        l.add(s);
//                    } else {
//                        s = "0" + s;
//                        l.add(s);
//                    }
//                }
//            }
//        }
//        return l;
//    }

//    private static ASN1Primitive toAsn1Primitive(byte[] data) throws Exception {
//        try(ByteArrayInputStream inStream = new ByteArrayInputStream(data);
//            ASN1InputStream asnInputStream = new ASN1InputStream(inStream);) {
//            return asnInputStream.readObject();
//        }
//    }

    /**
     * r|s生成sign
     *
     * @param r
     * @param s
     * @return
     */
    private static byte[] gnSign(byte[] r, byte[] s) {
        byte[] sign = {0x30};
        byte tag = 0x02;
        short i = TypeUtils.byteToUnit8((r[0]));
        short j = TypeUtils.byteToUnit8((s[0]));
        StringBuffer b = new StringBuffer();
        byte[] rb = {0x00};
        if ((i & 0x80) != 0) {
            rb = Arrays.copyOf(rb, 1 + r.length);
            System.arraycopy(r, 0, rb, 1, r.length);
            b.append("r");
        }

        byte[] sb = {0x00};
        if ((j & 0x80) != 0) {
            sb = Arrays.copyOf(sb, 1 + s.length);
            System.arraycopy(s, 0, sb, 1, s.length);
            b.append("s");
        }
        byte rl, sl;
        short bl, rlUint8, slUint8;
        switch (b.toString()) {
            case "r":
                //计算r和s的长度
                rl = (byte) rb.length;
                sl = (byte) s.length;
                rlUint8 = TypeUtils.byteToUnit8(rl);
                slUint8 = TypeUtils.byteToUnit8(sl);
                bl = (short) (rlUint8 + slUint8 + 4);
                sign = Arrays.copyOf(sign, 2 + bl);
                sign[1] = TypeUtils.uint8ToByte(bl);
                sign[2] = tag;
                sign[3] = rl;
                System.arraycopy(rb, 0, sign, 4, rlUint8);
                sign[4 + rlUint8] = tag;
                sign[5 + rlUint8] = sl;
                System.arraycopy(s, 0, sign, 6 + rlUint8, slUint8);
                break;
            case "s":
                //计算r和s的长度
                rl = (byte) r.length;
                sl = (byte) sb.length;
                rlUint8 = TypeUtils.byteToUnit8(rl);
                slUint8 = TypeUtils.byteToUnit8(sl);
                bl = (short) (rlUint8 + slUint8 + 4);
                sign = Arrays.copyOf(sign, 2 + bl);
                sign[1] = TypeUtils.uint8ToByte(bl);
                sign[2] = tag;
                sign[3] = rl;
                System.arraycopy(r, 0, sign, 4, rlUint8);
                sign[4 + rlUint8] = tag;
                sign[5 + rlUint8] = sl;
                System.arraycopy(sb, 0, sign, 6 + rlUint8, slUint8);
                break;
            case "rs":
                //计算r和s的长度
                rl = (byte) rb.length;
                sl = (byte) sb.length;
                rlUint8 = TypeUtils.byteToUnit8(rl);
                slUint8 = TypeUtils.byteToUnit8(sl);
                bl = (short) (rlUint8 + slUint8 + 4);
                sign = Arrays.copyOf(sign, 2 + bl);
                sign[1] = TypeUtils.uint8ToByte(bl);
                sign[2] = tag;
                sign[3] = rl;
                System.arraycopy(rb, 0, sign, 4, rlUint8);
                sign[4 + rlUint8] = tag;
                sign[5 + rlUint8] = sl;
                System.arraycopy(sb, 0, sign, 6 + rlUint8, slUint8);
                break;
            default:
                //计算r和s的长度
                rl = (byte) r.length;
                sl = (byte) s.length;
                rlUint8 = TypeUtils.byteToUnit8(rl);
                slUint8 = TypeUtils.byteToUnit8(sl);
                bl = (short) (rlUint8 + slUint8 + 4);
                sign = Arrays.copyOf(sign, 2 + bl);
                sign[1] = TypeUtils.uint8ToByte(bl);
                sign[2] = tag;
                sign[3] = rl;
                System.arraycopy(r, 0, sign, 4, rlUint8);
                sign[4 + rlUint8] = tag;
                sign[5 + rlUint8] = sl;
                System.arraycopy(s, 0, sign, 6 + rlUint8, slUint8);
                break;
        }
        return sign;
    }


    public static void main(String[] args) throws Exception {
        //签名
        String m = "hello world";
        String s = "b395b42d4a4976fe25ca3830a08b3f5f0261b565f0cf70ff834e7af96cc1e638138d15801158f36dd23bdc0c6228b9f76f1fb6583cf6036a3fea5841d7c58b0e";
        String pk = "0442b1c8e7daa30848f59e2d8d764882f088a8cfed054d0c91395f8cdb47b1dbecbf28ed6666f7aa7e19f14cc3725894c04ba1f805dcaecdc05e85d4fd1e9db56f";
        byte[] pk1 = formatPK(TypeUtils.hexStringToByte(pk));
        byte[] sign = rsGnSign(TypeUtils.hexStringToByte(s));
        boolean verify = verify(pk1, sign, m.getBytes());
        System.out.println(verify);

        //解密
        byte[] epk = TypeUtils.hexStringToByte("01f4aa3d0176decc4eb560e780b36c8291a8505ca24b96cdbef6e74a1e4d781f");
        byte[] iv = TypeUtils.hexStringToByte("40b0e2968f19d9a11e21a7412ecae926");
        byte[] c = TypeUtils.hexStringToByte("f5c8221de0f50ce8d232f08673d1d298d3d29295d0fd3142ed47ef22bdd0f4b044ef518a7ec73563aa825ff851bf4a79da7ced916eb88483bc3fdb8e95297d4953a97c40e38d7abafe034f022e0b9f67b433f7de4f7eb84e312cd52249672ac15f06666193f3fbc9129a4d62df9782a974de1582faa491349fe54c12dbf7b4584afb11e72b0e2eb428cba00afb00239f2ebf1f2bde99177e90a7a40f3621860e4f0ee60913cba943f3a0f8f198b8dddc4366297f64e51726114a770c8e07c63b1720bdb35ef907fc394dda8e1b28589a875649cbf582bdc68746f4d2803883a3f2baf4a004fe2ad54edca6babf9d0be856224287fd303e1634e083a85ce7414b7f568379ba9b39a4ca0e57a80b85216abb261254dbfc49a3304c85c200f8a7f23291b8339329f4c2766eda2b1abf5be432ec01bf8a4ad1dffd985bce3ed3d2de24ab370f089a6243f01a26e21b71baa6d17d6c8e4647141f4a22cc8f31a5261b260035e8ded2fa3c1f82fedac7476ed3998210d1c46141b050b2e2c2727b483fd7a3c720318eb7a1b01bfa40f4b5964d4d5162752895773fd01b6c3df250df3601322a1b1372a9c24d8ec03a25d26ee19e7030eefa2cd82c99fdd9d1f169aa730e283aabb64fd7b2c310ac42d9d904f69d10810443116a7a7fafbcdbd9337b360cfbc7c9ff13f1ea9261fb0ce5d785938ca61a2cb84ba542d517e2c14ba6b5f9ec2eb351eb53a56c617232ea4278ba189bd5c2cc6dcb3d583105bf1dea43c558ad33b651e6d759f78868f4da2413d4d9a2153cdddd8d85593c02b91690f62cd44175b029c432425fb17818800413bb09c0af273af7bad6631e1bb6767d4fadf8c311185259bdf228fd3955c268006e154298396fe0040f0bfc17da0d68625785882d815d995bc3ac4b8e1d8589a06ef09f7c7f45244d978799a0ef8af0332649be3cc92155c4f1ce5843c7807398208fce2cd3126a8ab1b523260fa3a7371d7cb8e820893173b6826908da3e2ce876cf1b1f63affbe0ae7da0d76dd3f345b496327a5c6fe30a25092d798a365e2d5dcba4751b59f26142182c39bfa9aad6bf2fd69632811b0ef2086e1ab80ffe5bbbba338d209d553ac91696138a4fe0592480c27d381b9dd218dea62b387a845dab75a68a25d46e37242ce06b5a9ea109acb7557a4e80256314e6bb4273561717142c19216622c298db632638e76b052514042603e100bd7c50d36922f9f51023b8f084f862eec8c00f631d0375212663e719d54a69890253161f7db9728d0fd1da929e784804802d2740f0db35a8f730f79d5c77b4785a83fb6604ddaae23d4afcc619dd2af53a57017fc3538535f1b5b6196b89e6913815c5fcc9d8e00b0c2a14dd6ed8e69b666baa9e612019e949c4ae17dd752031c335fdec600519d17405770e6ccbb2407969d5eb6ddbb103c9ebdced70faa6efb6a6e718dbb51c5971b899497a5dc9765e60dc3088219c4a53373fc8b43e5e5525ed7d9370adf1e55b1a0c0059199bab9eeb24cd9a5941d5b991e02eb7d95a4ac5c9f7bf15f907b6f8a248afa16e7ae344751c877aaa2c20c1b7430dc6a0b4c9ba99acbbd6e34dd3361f50afe574ad6db1b643be1b26c7db29771639b312eb5ea1ee2b5e8b4a8419308251afd8553f230a0c92f5d0ddcb3a79524d10429b597232799cd3df0c72c03a4fcc4ee480a30ecce28ddea4ec29ccb0ceb8a607b734bbab44a84c3829ee02dc099945b8f83701f5a80116db87ce6024718ea3e32aab70d0bb1bc4ccda8b5dca179469823d1e3751c5c23948cd02054f6d0c4c7e85eb6c17280f3ae9ffd284da4b5ed5d3ca8568e7d34e434608b425c4518dbb9c63971a4bbc9e2e52c992a44fbe4a8fa8828bc16e88a310a3660861d23b419c7b90efa76df5670ba13a2039a4bfa52a445bbdf9d3d7461d8f3a71d761a3a1d9d37f52da439a194ded85a0552e747e95d5f3154d665f9a1206eae64fb1c746098fdcb3f40fe6ff9a3a57736f76d68d82c2d0f427e1afcb067d4c8d61204a6d37f055d47001f399a97d6bdb2c5a94c4d1042675ce6d0bc1b8c05f3f6f3dd0514c4f044fa22f9ebb107c9bdf28936fb83b1ab1b285033da14980658e4804a8a55f4c90f3f8a7a966bd5f148eb1e598fd9ccb2eebc88ff1d6a93e2919c8ff8c233336b14ceb6edec9ecea6805b3efe190afd46808ddedcfaa972838980263349c734ecf4d2022312229c2307fed090e4c326228b722586cb12edaa095095e25c684c30bf90b5fc9aa58ca7c881891498dd90651feb41ea10596b57c8d2d15dbab9920fe75d678bc2faba449546ad88ba1f11a839c612577a4fc62266ea03fba36c063bc7f2ef8dc79ecd4362aaab76d3dc5ed7f82a23a3acdaf5cd0dbdd817798a8e3c6117927fb80e7a77a45e83ae7dc1733ada47514991d5adc96fe7c45d20d32231c41e82e8febffc3ffab6736a051583c723cc3c24151c182942de7b2e7d51c");
        byte[] mk = TypeUtils.hexStringToByte("4235b7279c885beb3747e4d01f33e38049611d0892364acdef189230ae88c2e8f9f63ab5a1b8a0505309e6ad90e1f4314d5b7c5336b5e20617671f57b132f6f7");
        byte[] bytes = AES256.AES_cbc_decrypt(c, epk, iv);
        System.out.println(new String(bytes));
        String mac = SHA256.sha256_HMAC(new String(bytes), mk);
        System.out.println(mac);


        //根据sign生成r s
//        for(int i = 0; i < 10; i++) {
//        BigInteger d = new BigInteger("628f142e96a2ba15f29f13ec85f3aeb9ec56fe0b3df30bea68870a19bbb7fba0", 16);
//        KeyPair pair = Secp256r1.generateKeyPair(d.toByteArray());
//        System.out.println(TypeUtils.bytesToHexString(pair.getPublic().getEncoded()));
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
