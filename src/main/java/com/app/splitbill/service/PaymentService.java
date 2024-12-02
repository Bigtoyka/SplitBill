package com.app.splitbill.service;

import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.Payment;
import com.app.splitbill.repository.PaymentRepository;
import com.app.splitbill.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private PaymentRepository paymentRepository;
    private UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }
    public Payment updatePayment(Long id, Payment payment) {
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        AppUser payer = userRepository.findById(payment.getPayer().getId())
                .orElseThrow(() -> new RuntimeException("Payer not found"));
        AppUser payee = userRepository.findById(payment.getPayee().getId())
                .orElseThrow(() -> new RuntimeException("Payee not found"));

        existingPayment.setPayer(payer);
        existingPayment.setPayee(payee);
        existingPayment.setAmount(payment.getAmount());

        return paymentRepository.save(existingPayment);
    }
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}
