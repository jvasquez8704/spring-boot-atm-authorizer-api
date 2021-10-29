package com.bancatlan.atmauthorizer.repo;

import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.Transaction;
import com.bancatlan.atmauthorizer.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface IVoucherRepo extends JpaRepository<Voucher, Long> {
    Voucher findByPickupCodeAndSecretCode(String pickupCode, String secretCode);
    Voucher getVoucherByTxnCreatedByIs(Transaction transaction);
    Voucher findByPickupCodeAndSecretCodeAndCustomerAndIsActive(String pickupCode, String secretCode, Customer customer, Boolean active);
    @Query(value = "select * from voucher where id_txn_created_by in (select id from txn where id_payer = (select id from customer where username =:username) and id_use_case = 174) and is_active = 1", nativeQuery = true)
    List<Voucher> findAllActiveByOcbUser(String username);
    List<Voucher> getVouchersByIsActiveAndExpirationDateGreaterThanEqual(Boolean isActive, LocalDateTime now);
    List<Voucher> getVouchersByIsActiveAndExpirationDateIsBefore(Boolean isActive, LocalDateTime now);
    List<Voucher> getVouchersByIsActiveAndIsCanceled(Boolean isActive, Boolean isCanceled);
    /*@Query(value = "SELECT * FROM voucher \n" +
            "WHERE is_active =:isActive AND is_canceled =: isCancelled AND is_deleted:isDeleted AND is_expired:isExpired \n" +
            "AND id_txn_created_by in (SELECT id FROM txn WHERE id_use_case =:useCaseId AND id_status =:statusTxnId)", nativeQuery = true)
    List<Voucher> findAllByFlagsAndCreatorTxnTypeAndCreatorTxnStatus(Boolean isActive, Boolean isCancelled, Boolean isDeleted, Boolean isExpired, Integer useCaseId, Integer statusTxnId);*/
}
