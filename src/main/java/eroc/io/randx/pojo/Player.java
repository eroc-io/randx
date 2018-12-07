package eroc.io.randx.pojo;

import java.util.ArrayList;
import java.util.List;

public class Player {

    //玩家公钥
    private byte[] pk;//格式化后pk

    //wss id
    private String uid;

    //玩家签名
    private byte[] sign;

    //玩家座位号
    private Integer seat;

    private List<byte[]> salt = new ArrayList<>();

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

    public List<byte[]> getSalt() {
        return salt;
    }

    public void setSalt(List<byte[]> salt) {
        this.salt = salt;
    }

    public Integer getSeat() {
        return seat;
    }

    public void setSeat(Integer seat) {
        this.seat = seat;
    }
}
