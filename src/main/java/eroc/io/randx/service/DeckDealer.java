package eroc.io.randx.service;

import com.google.protobuf.ByteString;
import eroc.io.randx.exception.DataException;
import eroc.io.randx.pojo.Buffer;
import eroc.io.randx.utils.*;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.*;

public class DeckDealer {


    private static byte[] seed = {};

    private static byte[] dsk;

    private static byte[] dpk;

    private static KeyPair pair;

    private static final Integer CARD_INDEX = 7;

    private static BigInteger max256b;

    //存储未抽牌
    private static List<Short> cards = new ArrayList<>();
    //为每一位玩家存储最新盐
    private static Map<String, byte[]> salts = new HashMap<>();
    //所有抽牌证明
    private static Map<String, List<String>> proofs = new HashMap<>();
    //剩余牌数
    private static Integer count = 0;

    private static final BigInteger ZERO = new BigInteger("0");
    private static final BigInteger N = new BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16);


    /**
     * 重置游戏
     */
    public Object[] resetOrStart() {
        try {
            SecureRandom secureRandom = new SecureRandom();
            seed = new byte[32];
            secureRandom.nextBytes(seed);
            BigInteger n = new BigInteger(seed);
            if (n.compareTo(N) < 0 && n.compareTo(ZERO) > 0) {
                pair = Secp256r1.generateKeyPair(seed);
                dsk = pair.getPrivate().getEncoded();
                dpk = pair.getPublic().getEncoded();
                max256b = new BigInteger("10000000000000000000000000000000000000000000000000000000000000000", 16);
                salts.clear();
                proofs.clear();
                cards.clear();
                count = 0;
            } else {
                seed = SHA256.getSHA256Bytes(seed);
                resetOrStart();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object[] o = {dsk, TypeUtils.bufferPk(dpk)};
        return o;
    }


    /**
     * 开始游戏
     *
     * @param cardNum 卡牌数量
     * @param deckNum 几副
     * @param pks
     * @return dpk 牌局的公钥    s 初始盐
     */
    public Object[] openGame(Integer cardNum, Integer deckNum, List<byte[]> pks) throws Exception {
        for(Short i = 0; i < deckNum; i++) {
            for(Short j = 0; j < cardNum; j++) {
                cards.add(j);
            }
        }
        int destPos = seed.length;
        int l;
        for(byte[] pk : pks) {
            l = pk.length;
            seed = Arrays.copyOf(seed, destPos + l);
            System.arraycopy(pk, 0, seed, destPos, l);
            destPos += l;
        }
        seed = CryptoUtils.sign(dsk, SHA256.getSHA256Bytes(seed));
        byte[] s = SHA256.getSHA256Bytes(seed);

        for(byte[] pk : pks) {
            String pkk = Base64.getEncoder().encodeToString(pk);
            salts.put(pkk, s);
            proofs.put(pkk, new ArrayList<>());
        }

        count = cardNum * deckNum;
        Object[] o = {s, TypeUtils.bufferPk(dpk)};
//        Object[] o = {s, dpk};
        return o;
    }

    /**
     * 抽牌
     *
     * @param pk  公钥
     * @param sig 签名
     * @return object[0]给抽牌玩家的信息  object[1]给其他玩家的proof
     * @throws Exception
     */
    private int i = 1;

    public Object[] drawCard(byte[] pk, byte[] sig) throws Exception {
        if (count <= 0) {
            throw new Exception("No more card can be drawn");
        }

        String pkk = Base64.getEncoder().encodeToString(pk);

        byte[] s = salts.get(pkk);

        if (s.length <= 0) {
            throw new Exception(String.format("Unknown player: %s", pkk));
        }
        System.out.println("第" + i + "轮，盐： " + Base64.getEncoder().encodeToString(s));
        System.out.println("第" + i + "轮，签名： " + Base64.getEncoder().encodeToString(sig));
        i++;
        if (!CryptoUtils.verify(pk, sig, s)) {
            throw new Exception("pk owner have wrong signature!");
        }
        //重新生成盐和种子
        int l = seed.length;
        int sl = sig.length;
        seed = Arrays.copyOf(seed, l + sl);
        System.arraycopy(sig, 0, seed, l, sl);
        seed = CryptoUtils.sign(dsk, SHA256.getSHA256Bytes(seed));
        s = SHA256.getSHA256Bytes(seed);
        //抽取卡牌
        BigInteger i = new BigInteger(s).multiply(new BigInteger(count.toString())).divide(max256b);
        int index = Math.abs(i.intValue());
        Short card = cards.get(index);
        cards.set(index, cards.get(--count));
        cards.remove(count);//移除最后一张无用牌
        //将牌放入盐中并进行crc验证
        Arrays.fill(s, CARD_INDEX, CARD_INDEX + 1, TypeUtils.uint8ToByte(card));
        byte crc = CRC8Util.calcCrc8(s);
        Arrays.fill(s, s.length - 1, s.length, crc);
        //更新玩家的最新盐
        salts.put(pkk, s);
        List<String> p = proofs.get(pkk);
        byte[] sendProof = SHA256.getSHA256Bytes(s);
        String proof = Base64.getEncoder().encodeToString(SHA256.getSHA256Bytes(s));
        p.add(proof);
        Buffer.DrawResponse.Builder resp = Buffer.DrawResponse.newBuilder().setCardCipher(CryptoUtils.ECDHEncrypt(pk, s, pair));
        Buffer.DrawNotification.Builder notify = Buffer.DrawNotification.newBuilder().setPk(ByteString.copyFrom(TypeUtils.bufferPk(pk))).setProof(ByteString.copyFrom(sendProof));
        Object[] obj = {resp, notify, s, proof};
        return obj;
    }


    /**
     * 抓取剩余牌
     *
     * @param pks   玩家公钥集合
     * @param signs 签名集合
     * @return 抓取的牌
     */
    public Buffer.DrawLeftNotification.Builder drawLeftCards(byte[][] pks, byte[][] signs) throws Exception {

        //牌数小于等于零，抛出异常
        if (count <= 0) {
            throw new DataException("No more card can be drawn");
        }

        //salt的数量与公钥数量和sign的数量不同，抛出异常
        int pc = salts.size();
        if (pks.length != pc || signs.length != pc) {
            throw new DataException("Not a consented draw cards request");
        }

        //将所有的公钥合并
        byte[] initial = new byte[0];
        int length;
        for(byte[] pk : pks) {//pk顺序
            pk = TypeUtils.bufferPk(pk);
            length = initial.length;
            initial = Arrays.copyOf(initial, pk.length + length);
            System.arraycopy(pk, 0, initial, length, pk.length);
        }
        //哈希公钥数组，得到摘要
        byte[] h2s = SHA256.getSHA256Bytes(initial);

        //验证签名
        for(int i = 0; i < pks.length; i++) {
            byte[] salt = salts.get(Base64.getEncoder().encodeToString(pks[i]));
            if (salt == null || salt.length == 0) {
                throw new DataException("Unknown player: " + Base64.getEncoder().encodeToString(pks[i]));
            }
            boolean flag = ECDSA.verify(h2s, pks[i], "SHA256withECDSA", signs[i]);
            //验证签名失败，抛出异常
            if (flag == false) {
                throw new DataException("Signature verification failed");
            }
        }
        //验证成功，取出所有牌
        List<Short> lc = cards.subList(0, count);
        int size = lc.size();
        byte[] lcards = new byte[size];
        for(int i = 0; i < size; i++) {
            lcards[i] = TypeUtils.uint8ToByte(lc.get(i));
        }
        cards.clear();
        count = 0;
        //生成新盐
        seed = CryptoUtils.sign(dsk, SHA256.getSHA256Bytes(seed));
        byte[] s = SHA256.getSHA256Bytes(seed);
        for(byte[] pk : pks) {
            String pkk = Base64.getEncoder().encodeToString(pk);
            salts.put(pkk, s);
        }
        return Buffer.DrawLeftNotification.newBuilder().setCards(ByteString.copyFrom(lcards)).setSalt(ByteString.copyFrom(s));
    }


    /**
     * 返还牌
     *
     * @param pk  玩家公钥
     * @param sig 签名
     * @param cs  牌信息
     * @return 最新盐
     */

    public Object[] returnCards(byte[] pk, byte[] sig, List<byte[]> cs) throws Exception {
        //验证玩家身份
        String pkStr = Base64.getEncoder().encodeToString(pk);
        byte[] salt = salts.get(pkStr);
        if (salt == null || salt.length == 0) {
            throw new DataException("Unknown player: " + pkStr);
        }
        boolean flag = ECDSA.verify(salt, pk, "SHA256withECDSA", sig);
        //验证签名失败，抛出异常
        if (flag == false) {
            throw new DataException("Signature verification failed");
        }

        //验证欲返回牌库的牌是否合理
        for(byte[] c : cs) {
            if (proofs.get(pkStr).indexOf(Base64.getEncoder().encodeToString(SHA256.getSHA256Bytes(c))) < 0) {
                throw new DataException("Unproven card" + TypeUtils.bytesToHexString(c) + "to return");
            }
        }

        //还牌
        for(byte[] c : cs) {
            cards.add(TypeUtils.byteToUnit8(c[CARD_INDEX]));
            count++;
        }

        //生成新的seed
        int length = seed.length;
        seed = Arrays.copyOf(seed, length + sig.length);
        System.arraycopy(sig, 0, seed, length, sig.length);
        seed = SHA256.getSHA256Bytes(seed);
        seed = ECDSA.sign(seed, dsk, "SHA256withECDSA");
        byte[] s = SHA256.getSHA256Bytes(seed);
        salts.put(pkStr, s);
        Buffer.ReturnResponse resp = Buffer.ReturnResponse.newBuilder().setNumReturned(cs.size()).setSalt(ByteString.copyFrom(s)).build();
        Buffer.ReturnNotification notify = Buffer.ReturnNotification.newBuilder().setNumReturned(cs.size()).setPk(ByteString.copyFrom(TypeUtils.bufferPk(pk))).build();
        Object[] o = {resp, notify};
        return o;

    }


}


