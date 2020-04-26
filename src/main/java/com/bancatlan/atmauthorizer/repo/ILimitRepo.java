package com.bancatlan.atmauthorizer.repo;

import com.bancatlan.atmauthorizer.model.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ILimitRepo extends JpaRepository<Limit,Long> {
}
