package com.bancatlan.atmauthorizer.repo;

import com.bancatlan.atmauthorizer.model.TxnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITxnStatusRepo extends JpaRepository<TxnStatus, Long> {
}
