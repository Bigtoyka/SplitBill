package com.app.splitbill.service;

import com.app.splitbill.dto.DebtDto;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.Bill;
import com.app.splitbill.model.BillItem;
import com.app.splitbill.model.BillParticipant;
import com.app.splitbill.repository.BillItemRepository;
import com.app.splitbill.repository.BillParticipantRepository;
import com.app.splitbill.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<DebtDto> calculateDebtBasedOnConsumption(Long billId) {
        Bill bill = getBillById(billId);
        if (bill == null) {
            throw new RuntimeException("Bill not found");
        }

        AppUser mainPayer = bill.getMainPayer();
        List<BillItem> billItems = billItemRepository.findByBillId(billId);
        Map<AppUser, BigDecimal> totalOwed = new HashMap<>();
        for (BillItem item : billItems) {
            List<BillParticipant> participants = billParticipantRepository.findByBillItemId(item.getId());
            for (BillParticipant participant : participants) {
                BigDecimal owed = participant.getAmountOwed();
                totalOwed.merge(participant.getAppUser(), owed, BigDecimal::add);
            }
        }

        List<DebtDto> debts = new ArrayList<>();
        for (Map.Entry<AppUser, BigDecimal> entry : totalOwed.entrySet()) {
            AppUser user = entry.getKey();
            BigDecimal owed = entry.getValue();
            BigDecimal paid = billParticipantRepository.findTotalPaidByUserAndBill(user.getId(), billId);
            BigDecimal debt = owed.subtract(paid);

            if (!user.equals(mainPayer) && debt.compareTo(BigDecimal.ZERO) > 0) {
                debts.add(new DebtDto(user.getUsername(), debt));
            }
        }
        return debts;
    }
}
