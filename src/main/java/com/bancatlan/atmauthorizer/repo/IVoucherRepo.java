package com.bancatlan.atmauthorizer.repo;

import com.bancatlan.atmauthorizer.model.Customer;
import com.bancatlan.atmauthorizer.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface IVoucherRepo extends JpaRepository<Voucher, Long> {
    Voucher findByPickupCodeAndSecretCode(String pickupCode, String secretCode);
    Voucher findByPickupCodeAndSecretCodeAndCustomer(String pickupCode, String secretCode, Customer customer);
    @Query(value = "select * from voucher where id_txn_created_by in (select id from txn where id_payer = (select id from customer where ocb_user =:ocbUser) and id_use_case = 174) and is_active = 1", nativeQuery = true)
    List<Voucher> findAllActiveByOcbUser(String ocbUser);
}
