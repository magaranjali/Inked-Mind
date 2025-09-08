package com.virinchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.virinchi.model.Payment;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // find all payments by user email
    List<Payment> findByEmail(String email);

    // find a payment by transactionId
    Payment findByTransactionId(String transactionId);
}
