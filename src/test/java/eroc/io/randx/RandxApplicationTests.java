package eroc.io.randx;

import eroc.io.randx.pojo.Proofs;
import eroc.io.randx.service.ProofsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = RandxApplication.class)
public class RandxApplicationTests {

    @Autowired
    private ProofsService proofsService;

    @Test
    public void contextLoads() {
        Proofs proofs = new Proofs();
        proofs.setPk("1234");
        proofs.setProof("321");
        Integer integer = this.proofsService.insertProof(proofs);
        System.out.println(integer);

    }

}
