package eroc.io.randx.service;

import com.google.protobuf.ByteString;
import eroc.io.randx.exception.DataException;
import eroc.io.randx.pojo.Buffer;
import eroc.io.randx.utils.*;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.*;

/**
 * Game board
 */
public class DeckDealer {


    private static byte[] seed = {};

    private static byte[] dsk;

    private static byte[] dpk;

    private static KeyPair pair;

    private static final Integer CARD_INDEX = 7;

    private static BigInteger max256b;

    // Storage not drawn
    private static List<Short> cards = new ArrayList<>();
    // Store the latest salt for each player
    private static Map<String, byte[]> salts = new HashMap<>();
    // All draw proofs
    private static Map<String, List<String>> proofs = new HashMap<>();
    // Remaining cards
    private static Integer count = 0;

    private static final BigInteger ZERO = new BigInteger("0");
    private static final BigInteger N = new BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16);


    /**
     * Open or reset the game
     *
     * @return obj[0] CardDealer privateKey; obj[1] CardDealer publicKey
     */
    public Object[] resetOrStart() {
        try {
            // Randomly generate 256-bit seeds
            SecureRandom secureRandom = new SecureRandom();
            seed = new byte[32];
            secureRandom.nextBytes(seed);
            BigInteger n = new BigInteger(seed);

            // Seed must meet 0<seed<N ,Otherwise hash the seed;
            // Initialize the table information
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
        return new Object[]{dsk, TypeUtils.bufferPk(dpk)};
    }


    /**
     * openGame
     *
     * @param cardNum cardNum
     * @param deckNum deckNum
     * @param pks     Public key of all players
     * @return dpk: CardDealer publicKey    s: Initial salt
     */
    public Object[] openGame(Integer cardNum, Integer deckNum, byte[][] pks) throws Exception {
        // Initialize all cards
        for(Short i = 0; i < deckNum; i++) {
            for(Short j = 0; j < cardNum; j++) {
                cards.add(j);
            }
        }

        // Generate new seeds and salt
        for(byte[] pk : pks) {
            seed = TypeUtils.concatByteArrays(new byte[][]{seed, pk});
        }
        seed = CryptoUtils.sign(dsk, SHA256.getSHA256Bytes(seed));
        byte[] s = SHA256.getSHA256Bytes(seed);

        // Cache the latest salt for each player
        for(byte[] pk : pks) {
            String pkk = Base64.getEncoder().encodeToString(pk);
            salts.put(pkk, s);
            proofs.put(pkk, new ArrayList<>());
        }

        count = cardNum * deckNum;
        return new Object[]{s, TypeUtils.bufferPk(dpk)};
    }

    /**
     * drawCard
     *
     * @param pk  Player public key
     * @param sig Player sign
     * @return object[0]Information for the draw player;  object[1]Proof to other players; object[2]Player latest salt; object[3] Proof of player draw;
     * @throws Exception
     */
    public Object[] drawCard(byte[] pk, byte[] sig) throws Exception {
        if (count <= 0) {
            throw new Exception("No more card can be drawn");
        }

        String pkk = Base64.getEncoder().encodeToString(pk);

        byte[] s = salts.get(pkk);

        if (s.length <= 0) {
            throw new Exception(String.format("Unknown player: %s", pkk));
        }

        // Verify player information
        if (!CryptoUtils.verify(pk, sig, s)) {
            throw new Exception("pk owner have wrong signature!");
        }

        // Regenerate salt and seeds
        seed = CryptoUtils.sign(dsk, SHA256.getSHA256Bytes(TypeUtils.concatByteArrays(new byte[][]{seed, sig})));
        s = SHA256.getSHA256Bytes(seed);

        // Draw a card at random and Fisher–Yates shuffle
        BigInteger i = new BigInteger(s).multiply(new BigInteger(count.toString())).divide(max256b);
        int index = Math.abs(i.intValue());
        Short card = cards.get(index);
        cards.set(index, cards.get(--count));
        cards.remove(count);

        // Place the card in salt and verify it by CRC
        s[CARD_INDEX] = TypeUtils.uint8ToByte(card);
        s[s.length - 1] = CRC8Util.calcCrc8(s);

        // Update player's latest salt and proof
        salts.put(pkk, s);
        List<String> p = proofs.get(pkk);
        byte[] sendProof = SHA256.getSHA256Bytes(s);
        String proof = Base64.getEncoder().encodeToString(SHA256.getSHA256Bytes(s));
        p.add(proof);
        Buffer.DrawResponse.Builder resp = Buffer.DrawResponse.newBuilder().setCardCipher(CryptoUtils.ECDHEncrypt(pk, s, pair));
        Buffer.DrawNotification.Builder notify = Buffer.DrawNotification.newBuilder().setPk(ByteString.copyFrom(TypeUtils.bufferPk(pk))).setProof(ByteString.copyFrom(sendProof));
        return new Object[]{resp, notify, s, proof};
    }


    /**
     * drawLeftCards
     *
     * @param pks   All players public key
     * @param signs All Players Signature Collection
     * @param n     To grab the remaining number of cards, null is the total number
     * @return Removal of card information
     */
    public Buffer.DrawLeftNotification.Builder drawLeftCards(byte[][] pks, byte[][] signs, Integer n) throws Exception {

        // If the number of cards is less than or equal to zero, throw an exception
        if (count <= 0) {
            throw new DataException("No more card can be drawn");
        }

        // The number of salts is different from the number of public keys and signs, throwing exceptions
        int pc = salts.size();
        if (pks.length != pc || signs.length != pc) {
            throw new DataException("Not a consented draw cards request");
        }

        // Hash public key array, get Abstract
        byte[] h2s = SHA256.getSHA256Bytes(TypeUtils.concatByteArrays(pks));

        // Verifying signature
        for(int i = 0; i < pks.length; i++) {
            byte[] salt = salts.get(Base64.getEncoder().encodeToString(pks[i]));
            if (salt == null || salt.length == 0) {
                throw new DataException("Unknown player: " + Base64.getEncoder().encodeToString(pks[i]));
            }
            boolean flag = ECDSA.verify(h2s, pks[i], "SHA256withECDSA", signs[i]);
            // Verification signature failed, exception thrown
            if (flag == false) {
                throw new DataException("Signature verification failed");
            }
        }

        // If n is empty, take out all cards, if n < count, take out n cards
        List<Short> lc = null;
        if (n == null || n > count) {
            lc = cards.subList(0, count);
            cards.clear();
            count = 0;
        } else {
            lc = cards.subList(count - n, count);
            cards = cards.subList(0, count - n);
            count -= n;
        }
        int size = lc.size();
        byte[] lcards = new byte[size];
        for(int i = 0; i < size; i++) {
            lcards[i] = TypeUtils.uint8ToByte(lc.get(i));
        }

        // Generating new salt
        seed = CryptoUtils.sign(dsk, SHA256.getSHA256Bytes(seed));
        byte[] s = SHA256.getSHA256Bytes(seed);
        for(byte[] pk : pks) {
            String pkk = Base64.getEncoder().encodeToString(pk);
            salts.put(pkk, s);
        }
        return Buffer.DrawLeftNotification.newBuilder().setCards(ByteString.copyFrom(lcards)).setSalt(ByteString.copyFrom(s));
    }


    /**
     * returnCards
     *
     * @param pk  player publicKey
     * @param sig player sign
     * @param cs  All salts containing card information
     * @return obj[0] Buffer.returnResponse return number, salt  obj[1] Buffer.returnNotification return card number, return card player publicKey
     */

    public Object[] returnCards(byte[] pk, byte[] sig, List<byte[]> cs) throws Exception {
        // Verify player sign
        String pkStr = Base64.getEncoder().encodeToString(pk);
        byte[] salt = salts.get(pkStr);
        if (salt == null || salt.length == 0) {
            throw new DataException("Unknown player: " + pkStr);
        }
        boolean flag = ECDSA.verify(salt, pk, "SHA256withECDSA", sig);

        // Verify signature failed, throw exception
        if (flag == false) {
            throw new DataException("Signature verification failed");
        }

        // Verify that the card to be returned to the library is reasonable
        for(byte[] c : cs) {
            if (proofs.get(pkStr).indexOf(Base64.getEncoder().encodeToString(SHA256.getSHA256Bytes(c))) < 0) {
                throw new DataException("Unproven card" + TypeUtils.bytesToHexString(c) + "to return");
            }
        }

        // return cards
        for(byte[] c : cs) {
            cards.add(TypeUtils.byteToUnit8(c[CARD_INDEX]));
            count++;
        }

        // Generate a new seed
        int length = seed.length;
        seed = Arrays.copyOf(seed, length + sig.length);
        System.arraycopy(sig, 0, seed, length, sig.length);
        seed = SHA256.getSHA256Bytes(seed);
        seed = ECDSA.sign(seed, dsk, "SHA256withECDSA");
        byte[] s = SHA256.getSHA256Bytes(seed);
        salts.put(pkStr, s);

        Buffer.ReturnResponse resp = Buffer.ReturnResponse.newBuilder().setNumReturned(cs.size()).setSalt(ByteString.copyFrom(s)).build();
        Buffer.ReturnNotification notify = Buffer.ReturnNotification.newBuilder().setNumReturned(cs.size()).setPk(ByteString.copyFrom(TypeUtils.bufferPk(pk))).build();
        return new Object[]{resp, notify};

    }


}


