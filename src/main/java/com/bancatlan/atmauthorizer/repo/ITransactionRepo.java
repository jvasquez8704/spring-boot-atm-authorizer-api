package com.bancatlan.atmauthorizer.repo;

import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.UseCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ITransactionRepo extends JpaRepository<Transaction, Long> {
    Transaction getTransactionByAtmReference(String atmReference);
    List<Transaction> getTransactionsByPayeeAndUseCaseAndCreationDateBetween(Customer payee, UseCase useCase, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> getTransactionsByPayerAndUseCaseAndCreationDateBetween(Customer payer,UseCase useCase, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> getTransactionsByPayerOrPayeeAndUseCaseAndCreationDateBetween(Customer payer, Customer payee,UseCase useCase, LocalDateTime startDate, LocalDateTime endDate);
}