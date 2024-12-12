// Возможно убрать не опнадобился
//package com.app.splitbill.service;
//
//import com.app.splitbill.dto.PaymentRequestDto;
//import com.app.splitbill.exception.ResourceNotFoundException;
//import com.app.splitbill.model.AppUser;
//import com.app.splitbill.model.Payment;
//import com.app.splitbill.repository.PaymentRepository;
//import com.app.splitbill.repository.UserRepository;
//import org.springframework.stereotype.Service;
//
//
//@Service
//public class PaymentService {
//    private PaymentRepository paymentRepository;
//    private UserRepository userRepository;
//
//    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
//        this.paymentRepository = paymentRepository;
//        this.userRepository = userRepository;
//    }
//
//    public Payment createPayment(PaymentRequestDto paymentRequestDto) {
//        AppUser payer = userRepository.findByUsername(paymentRequestDto.getPayerUsername())
//                .orElseThrow(() -> new ResourceNotFoundException("Payer not found with username: " + paymentRequestDto.getPayerUsername()));
//        AppUser payee = userRepository.findByUsername(paymentRequestDto.getPayeeUsername())
//                .orElseThrow(() -> new ResourceNotFoundException("Payee not found with username: " + paymentRequestDto.getPayeeUsername()));
//
//        Payment payment = new Payment();
//        payment.setPayer(payer);
//        payment.setPayee(payee);
//        payment.setAmount(paymentRequestDto.getAmount());
//
//        return paymentRepository.save(payment);
//    }
//
//    public Payment getPaymentById(Long id) {
//        return paymentRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
//    }
//
//    public Payment updatePayment(Long id, PaymentRequestDto paymentRequestDto) {
//        Payment existingPayment = paymentRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
//
//        AppUser payer = userRepository.findByUsername(paymentRequestDto.getPayerUsername())
//                .orElseThrow(() -> new ResourceNotFoundException("Payer not found with username: " + paymentRequestDto.getPayerUsername()));
//
//        AppUser payee = userRepository.findByUsername(paymentRequestDto.getPayeeUsername())
//                .orElseThrow(() -> new ResourceNotFoundException("Payee not found with username: " + paymentRequestDto.getPayeeUsername()));
//
//
//        existingPayment.setPayer(payer);
//        existingPayment.setPayee(payee);
//        existingPayment.setAmount(paymentRequestDto.getAmount());
//
//        return paymentRepository.save(existingPayment);
//    }
//
//
//    public void deletePayment(Long id) {
//        if (!paymentRepository.existsById(id)) {
//            throw new ResourceNotFoundException("Payment not found with id: " + id);
//        }
//        paymentRepository.deleteById(id);
//    }
//
//}
