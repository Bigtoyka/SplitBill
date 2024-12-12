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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;

@Slf4j
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
        log.info("Creating bill from request: {}", billRequestDto);
        AppUser createdBy = userRepository.findByUsername(billRequestDto.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + billRequestDto.getCreatedBy()));
        log.debug("Found user for createdBy: {}", createdBy.getUsername());

        AppUser mainPayer = userRepository.findByUsername(billRequestDto.getMainPayer())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + billRequestDto.getMainPayer()));
        log.debug("Found user for mainPayer: {}", mainPayer.getUsername());

        AppGroup appGroup = groupRepository.findByName(billRequestDto.getAppGroup())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + billRequestDto.getAppGroup()));
        log.debug("Found group: {}", appGroup.getName());

        Bill bill = new Bill();
        bill.setTotalAmount(billRequestDto.getTotalAmount());
        bill.setDescription(billRequestDto.getDescription());
        bill.setDate(billRequestDto.getDate());
        bill.setCreatedBy(createdBy);
        bill.setMainPayer(mainPayer);
        bill.setAppGroup(appGroup);

        Bill savedBill = billRepository.save(bill);
        log.info("Bill created successfully with ID: {}", savedBill.getId());
        return savedBill;
    }

    public Bill getBillById(Long id) {
        log.info("Fetching bill with ID: {}", id);
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }

    public BillItem addBillItem(Long billId, BillItem billItem) {
        log.info("Adding bill item to bill ID: {}", billId);
        Bill bill = getBillById(billId);
        billItem.setBill(bill);
        BillItem savedItem = billItemRepository.save(billItem);
        log.info("Bill item added successfully with ID: {}", savedItem.getId());
        return savedItem;
    }


    public BillParticipant addBillParticipantByDetails(BillParticipantRequestDto participantDto) {
        log.info("Adding participant to bill: {}", participantDto);
        AppGroup appGroup = groupRepository.findByName(participantDto.getGroupName())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        log.debug("Found group: {}", appGroup.getName());

        Bill bill = billRepository.findById(participantDto.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        log.debug("Found bill with ID: {}", bill.getId());


        if (!bill.getAppGroup().equals(appGroup)) {
            throw new ValidationException("Bill does not belong to the specified group");
        }

        BillItem billItem = billItemRepository.findByItemNameAndBill_AppGroup(participantDto.getItemName(), appGroup)
                .orElseThrow(() -> new ResourceNotFoundException("Bill item not found in the specified group"));
        log.debug("Found bill item: {}", billItem.getItemName());

        AppUser appUser = userRepository.findByUsername(participantDto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        log.debug("Found user: {}", appUser.getUsername());

        BillParticipant billParticipant = new BillParticipant();
        billParticipant.setBillItem(billItem);
        billParticipant.setAppUser(appUser);
        billParticipant.setAmountOwed(participantDto.getAmountOwed());
        billParticipant.setAmountPaid(participantDto.getAmountPaid());
        BillParticipant savedParticipant = billParticipantRepository.save(billParticipant);
        log.info("Participant added successfully with ID: {}", savedParticipant.getId());

        return savedParticipant;
    }


    public List<DebtDto> calculateDebtBasedOnConsumption(Long billId) {
        log.info("Calculating debts for bill ID: {}", billId);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        log.debug("Found bill with ID: {}", bill.getId());

        AppUser mainPayer = bill.getMainPayer();
        List<BillItem> billItems = billItemRepository.findByBillId(billId);
        log.debug("Found {} bill items for bill ID: {}", billItems.size(), billId);

        Map<AppUser, BigDecimal> totalOwed = new HashMap<>();

        for (BillItem item : billItems) {
            List<BillParticipant> participants = billParticipantRepository.findByBillItemId(item.getId());
            log.debug("Found {} participants for bill item ID: {}", participants.size(), item.getId());
            for (BillParticipant participant : participants) {
                BigDecimal owed = participant.getAmountOwed();
                totalOwed.merge(participant.getAppUser(), owed, BigDecimal::add);
            }
        }

        List<DebtDto> debts = new ArrayList<>();
        for (Map.Entry<AppUser, BigDecimal> entry : totalOwed.entrySet()) {
            AppUser debtor = entry.getKey();
            BigDecimal owed = entry.getValue();
            BigDecimal paid = billParticipantRepository.findTotalPaidByUserAndBill(debtor.getId(), billId);
            BigDecimal debt = owed.subtract(paid);

            if (!debtor.equals(mainPayer) && debt.compareTo(BigDecimal.ZERO) > 0) {
                debts.add(new DebtDto(debtor.getUsername(), mainPayer.getUsername(), debt));
            }
        }
        log.info("Calculated debts: {}", debts);
        return debts;
    }

    // общее блюдо
    public void addSharedBillItem(Long billId, String itemName, BigDecimal itemPrice) {
        log.info("Adding shared bill item: {}, price: {} to bill ID: {}", itemName, itemPrice, billId);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        log.debug("Found bill with ID: {}", bill.getId());

        AppGroup group = bill.getAppGroup();
        if (group == null) {
            throw new ValidationException("Group not associated with this bill");
        }
        log.debug("Found group: {}", group.getName());

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
        log.debug("Calculated individual share: {} for {} members", individualShare, members.size());

        for (GroupMember member : members) {
            BillParticipant participant = new BillParticipant();
            participant.setBillItem(sharedItem);
            participant.setAppUser(member.getAppUser());
            participant.setAmountOwed(individualShare);
            participant.setAmountPaid(BigDecimal.ZERO);


            billParticipantRepository.save(participant);
            log.info("Shared bill item added successfully");

        }
    }

    public List<DebtDto> calculateGroupDebts(String groupName) {
        log.info("Calculating group debts for group: {}", groupName);
        AppGroup group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        log.debug("Group found: {}", group);

        List<Bill> bills = billRepository.findByAppGroup(group);
        log.debug("Number of bills found: {}", bills.size());

        Map<AppUser, Map<AppUser, BigDecimal>> debtMap = new HashMap<>();

        bills.forEach(bill -> {
            AppUser mainPayer = bill.getMainPayer();
            log.debug("Processing bill with ID: {}, main payer: {}", bill.getId(), mainPayer.getUsername());

            billItemRepository.findByBillId(bill.getId()).forEach(item -> {
                billParticipantRepository.findByBillItemId(item.getId()).forEach(participant -> {
                    BigDecimal debt = participant.getAmountOwed().subtract(participant.getAmountPaid());
                    if (debt.compareTo(BigDecimal.ZERO) > 0) {
                        debtMap
                                .computeIfAbsent(participant.getAppUser(), k -> new HashMap<>())
                                .merge(mainPayer, debt, BigDecimal::add);
                    }
                });
            });
        });
        log.debug("Debt map before simplification: {}", debtMap);

        Map<AppUser, Map<AppUser, BigDecimal>> simplifiedDebts = simplifyDebts(debtMap);
        log.debug("Simplified debt map: {}", simplifiedDebts);

        List<DebtDto> debts = new ArrayList<>();
        simplifiedDebts.forEach((debtor, creditors) -> creditors.forEach((creditor, amount) -> {
            debts.add(new DebtDto(debtor.getUsername(), creditor.getUsername(), amount));
        }));
        log.info("Calculated debts for group: {}", groupName);

        return debts;
    }

    private Map<AppUser, Map<AppUser, BigDecimal>> simplifyDebts(Map<AppUser, Map<AppUser, BigDecimal>> debtMap) {
        log.debug("Simplifying debts beginer");

        Map<AppUser, Map<AppUser, BigDecimal>> simplified = new HashMap<>();

        debtMap.forEach((debtor, creditors) -> creditors.forEach((creditor, amount) -> {
            BigDecimal reverseDebt = debtMap.getOrDefault(creditor, Collections.emptyMap()).getOrDefault(debtor, BigDecimal.ZERO);
            if (reverseDebt.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal netDebt = amount.subtract(reverseDebt);
                if (netDebt.compareTo(BigDecimal.ZERO) > 0) {
                    simplified.computeIfAbsent(debtor, k -> new HashMap<>()).put(creditor, netDebt);
                } else if (netDebt.compareTo(BigDecimal.ZERO) < 0) {
                    simplified.computeIfAbsent(creditor, k -> new HashMap<>()).put(debtor, netDebt.negate());
                }
            } else {
                simplified.computeIfAbsent(debtor, k -> new HashMap<>()).put(creditor, amount);
            }
        }));
        log.debug("Simplified debts: {}", simplified);
        return simplified;
    }
}
