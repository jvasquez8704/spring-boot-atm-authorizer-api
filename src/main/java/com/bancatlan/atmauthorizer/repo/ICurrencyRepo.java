package com.bancatlan.atmauthorizer.repo;

import com.bancatlan.atmauthorizer.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICurrencyRepo extends JpaRepository<Currency, Long> {
}
