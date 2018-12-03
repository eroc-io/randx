package eroc.io.randx.pojo;

public class Player {

    //玩家公钥
    private byte[] pk;

    //玩家签名
//    private byte[] sign;

    private String uid;//wss id


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
}
