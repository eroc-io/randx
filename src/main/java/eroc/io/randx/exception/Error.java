package eroc.io.randx.exception;

public enum Error {

    BufferConvert(90000, "PBF数据转换异常"), OpenConvert(10000, "游戏无法正常开局"), JoinConvert(10001, "牌局玩家人数不足，请耐心等待"),ReturnConvert(10002, "还牌信息有误，请检查后重新操作");


    private Error(Integer code, String msg) {
        this.msg = msg;
        this.code = code;
    }

    private String msg;

    private Integer code;


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public static String getMsg(int code) {
        for(Error error : Error.values()) {
            if (error.code == code) {
                return error.msg;
            }
        }
        return null;
    }


}
