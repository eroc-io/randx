package eroc.io.randx.pojo;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "proofs")
public class Proofs {

    @Id
    private Integer id;
    private String proof;
    private String pk;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProof() {
        return proof;
    }

    public void setProof(String proof) {
        this.proof = proof;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }
}
