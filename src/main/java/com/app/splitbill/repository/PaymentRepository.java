package com.app.splitbill.repository;

import com.app.splitbill.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPayer_Id(Long payerId);

    List<Payment> findByPayee_Id(Long payeeId);
}
