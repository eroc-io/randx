package eroc.io.randx.pojo;

public class GameType {

    private String name;
    private String code;
    private OtherParam otherParam;
    private BasicParam basicParam;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public OtherParam getOtherParam() {
        return otherParam;
    }

    public void setOtherParam(OtherParam otherParam) {
        this.otherParam = otherParam;
    }

    public BasicParam getBasicParam() {
        return basicParam;
    }

    public void setBasicParam(BasicParam basicParam) {
        this.basicParam = basicParam;
    }
}
