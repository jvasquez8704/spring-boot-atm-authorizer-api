package com.bancatlan.atmauthorizer.service.impl;

import com.bancatlan.atmauthorizer.model.UseCase;
import com.bancatlan.atmauthorizer.repo.IUseCaseRepo;
import com.bancatlan.atmauthorizer.service.IUserCaseSevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UseCaseServiceImpl implements IUserCaseSevice {

    @Autowired
    IUseCaseRepo repo;

    @Override
    public UseCase create(UseCase obj) {
        return null;
    }

    @Override
    public UseCase update(UseCase obj) {
        return null;
    }

    @Override
    public List<UseCase> getAll() {
        return null;
    }

    @Override
    public UseCase getById(Long id) {
        Optional<UseCase> useCase = repo.findById(id);
        return useCase.isPresent() ? useCase.get() : null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }
}
