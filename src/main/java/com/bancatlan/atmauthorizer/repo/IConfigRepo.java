package com.bancatlan.atmauthorizer.repo;

import com.bancatlan.atmauthorizer.model.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IConfigRepo extends JpaRepository<Config, Long> {
    Config getConfigByPropertyName(String propertyName);
}
