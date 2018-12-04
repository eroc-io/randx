package eroc.io.randx.pojo;

public class Player {

    //玩家公钥
    private byte[] pk;

    //wss id
    private String uid;

    //玩家签名
    private byte[] sign;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public byte[] getPk() {
        return pk;
    }

    public void setPk(byte[] pk) {
        this.pk = pk;
    }

    public byte[] getSign() {
        return sign;
    }

    public void setSign(byte[] sign) {
        this.sign = sign;
    }
}
