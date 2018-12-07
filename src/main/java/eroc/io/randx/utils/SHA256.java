package eroc.io.randx.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 {


    /**
     * 利用java原生的类实现SHA256加密
     *
     * @param str 加密后的报文
     * @return
     */
    public static String getSHA256(String str) {
        MessageDigest messageDigest;
        String encodestr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodestr = TypeUtils.bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    public static String getSHA256(byte[] bytes) {
        String encodestr = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes);
            encodestr = TypeUtils.bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    public static byte[] getSHA256Bytes(byte[] bytes) {
        byte[] digest = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes);
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return digest;
    }


    /**
     * sha256_HMAC加密
     *
     * @param message 消息
     * @param mk      秘钥
     * @return 加密后字符串
     */
    public static byte[] sha256_HMAC(byte[] message, byte[] mk) {
        String hash = "";
        byte[] bytes = {};
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(mk, "HmacSHA256");
            sha256_HMAC.init(secret_key);
            bytes = sha256_HMAC.doFinal(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

}
