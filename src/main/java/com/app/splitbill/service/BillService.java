package com.app.splitbill.service;

import com.app.splitbill.model.Bill;
import com.app.splitbill.model.BillItem;
import com.app.splitbill.model.BillParticipant;
import com.app.splitbill.repository.BillItemRepository;
import com.app.splitbill.repository.BillParticipantRepository;
import com.app.splitbill.repository.BillRepository;
import org.springframework.stereotype.Service;

@Service
public class BillService {
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final BillParticipantRepository billParticipantRepository;

    public BillService(BillRepository billRepository, BillItemRepository billItemRepository, BillParticipantRepository billParticipantRepository) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.billParticipantRepository = billParticipantRepository;
    }
    public Bill createBill(Bill bill) {
        return billRepository.save(bill);
    }
    public Bill getBillById(Long id) {
        return billRepository.findById(id).orElse(null);
    }
    public BillItem addBillItem(Long billId, BillItem billItem) {
        Bill bill = getBillById(billId);
        billItem.setBill(bill);
        return billItemRepository.save(billItem);
    }
    public BillParticipant addBillParticipant(Long billItemId, BillParticipant billParticipant) {
        BillItem billItem = billItemRepository.findById(billItemId).orElse(null);
        billParticipant.setBillItem(billItem);
        return billParticipantRepository.save(billParticipant);
    }
}
