package eroc.io.randx.service.impl;

import eroc.io.randx.dao.ProofsDao;
import eroc.io.randx.pojo.Proofs;
import eroc.io.randx.service.ProofsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProofsServiceImpl implements ProofsService {

    @Autowired
    private ProofsDao proofsDao;

    @Override
    public Integer insertProof(Proofs proofs) {
        int i = this.proofsDao.insertSelective(proofs);
        System.out.println(i);
        int insert = this.proofsDao.insert(proofs);
        System.out.println(insert);
        return i + insert;
    }
}
