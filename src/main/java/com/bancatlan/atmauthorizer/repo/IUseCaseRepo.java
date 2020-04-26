package com.bancatlan.atmauthorizer.repo;

import com.bancatlan.atmauthorizer.model.UseCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUseCaseRepo extends JpaRepository<UseCase, Long> {
}
