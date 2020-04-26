package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.model.TxnStatus;
import com.bancatlan.atmauthorizer.model.UseCase;
import com.bancatlan.atmauthorizer.repo.ITxnStatusRepo;
import com.bancatlan.atmauthorizer.service.ITxnStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TxnStatusServiceImpl implements ITxnStatusService {
    @Autowired
    ITxnStatusRepo repo;

    @Override
    public TxnStatus create(TxnStatus obj) {
        return null;
    }

    @Override
    public TxnStatus update(TxnStatus obj) {
        return null;
    }

    @Override
    public List<TxnStatus> getAll() {
        return null;
    }

    @Override
    public TxnStatus getById(Long id) {
        Optional<TxnStatus> status = repo.findById(id);
        return status.isPresent() ? status.get() : null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }
}
