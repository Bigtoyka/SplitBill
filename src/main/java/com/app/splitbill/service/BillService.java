package com.app.splitbill.service;

import com.app.splitbill.dto.BillParticipantRequestDto;
import com.app.splitbill.dto.BillRequestDto;
import com.app.splitbill.dto.DebtDto;
import com.app.splitbill.exception.ResourceNotFoundException;
import com.app.splitbill.exception.ValidationException;
import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.Bill;
import com.app.splitbill.model.BillItem;
import com.app.splitbill.model.BillParticipant;
import com.app.splitbill.model.GroupMember;

import com.app.splitbill.repository.BillItemRepository;
import com.app.splitbill.repository.BillRepository;
import com.app.splitbill.repository.BillParticipantRepository;
import com.app.splitbill.repository.UserRepository;
import com.app.splitbill.repository.GroupRepository;
import com.app.splitbill.repository.GroupMemberRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BillService {
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final BillParticipantRepository billParticipantRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public BillService(BillRepository billRepository, BillItemRepository billItemRepository, BillParticipantRepository billParticipantRepository, UserRepository userRepository, GroupRepository groupRepository, GroupMemberRepository groupMemberRepository) {
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.billParticipantRepository = billParticipantRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    public Bill createBillFromRequest(BillRequestDto billRequestDto) {
        AppUser createdBy = userRepository.findByUsername(billRequestDto.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + billRequestDto.getCreatedBy()));

        AppUser mainPayer = userRepository.findByUsername(billRequestDto.getMainPayer())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + billRequestDto.getMainPayer()));

        AppGroup appGroup = groupRepository.findByName(billRequestDto.getAppGroup())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + billRequestDto.getAppGroup()));

        Bill bill = new Bill();
        bill.setTotalAmount(billRequestDto.getTotalAmount());
        bill.setDescription(billRequestDto.getDescription());
        bill.setDate(billRequestDto.getDate());
        bill.setCreatedBy(createdBy);
        bill.setMainPayer(mainPayer);
        bill.setAppGroup(appGroup);

        return billRepository.save(bill);
    }

    public Bill getBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }

    public BillItem addBillItem(Long billId, BillItem billItem) {
        Bill bill = getBillById(billId);
        billItem.setBill(bill);
        return billItemRepository.save(billItem);
    }


    public BillParticipant addBillParticipantByDetails(BillParticipantRequestDto participantDto) {
        AppGroup appGroup = groupRepository.findByName(participantDto.getGroupName())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        BillItem billItem = billItemRepository.findByItemNameAndBill_AppGroup(participantDto.getItemName(), appGroup)
                .orElseThrow(() -> new ResourceNotFoundException("Bill item not found in the specified group"));

        AppUser appUser = userRepository.findByUsername(participantDto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BillParticipant billParticipant = new BillParticipant();
        billParticipant.setBillItem(billItem);
        billParticipant.setAppUser(appUser);
        billParticipant.setAmountOwed(participantDto.getAmountOwed());
        billParticipant.setAmountPaid(participantDto.getAmountPaid());

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

    // общее блюдо
    public void addSharedBillItem(Long billId, String itemName, BigDecimal itemPrice) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        AppGroup group = bill.getAppGroup();
        if (group == null) {
            throw new ValidationException("Group not associated with this bill");
        }

        List<GroupMember> members = groupMemberRepository.findByAppGroup(group);
        if (members.isEmpty()) {
            throw new ValidationException("Group has no members");
        }

        BillItem sharedItem = new BillItem();
        sharedItem.setItemName(itemName);
        sharedItem.setItemPrice(itemPrice);
        sharedItem.setBill(bill);

        billItemRepository.save(sharedItem);

        BigDecimal individualShare = itemPrice.divide(new BigDecimal(members.size()), 2, RoundingMode.HALF_UP);
        for (GroupMember member : members) {
            BillParticipant participant = new BillParticipant();
            participant.setBillItem(sharedItem);
            participant.setAppUser(member.getAppUser());
            participant.setAmountOwed(individualShare);
            participant.setAmountPaid(BigDecimal.ZERO);


            billParticipantRepository.save(participant);
        }
    }
}
