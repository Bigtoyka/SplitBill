package com.app.splitbill.controller;

import com.app.splitbill.dto.BillParticipantRequestDto;
import com.app.splitbill.dto.BillRequestDto;
import com.app.splitbill.dto.DebtDto;
import com.app.splitbill.dto.SharedBillItemRequestDto;
import com.app.splitbill.model.Bill;
import com.app.splitbill.model.BillItem;
import com.app.splitbill.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bills")
public class BillController {
    private final BillService billService;

    @PostMapping("/create")
    public ResponseEntity<String> createBill(@RequestBody BillRequestDto billRequestDto) {
        billService.createBillFromRequest(billRequestDto);
        return ResponseEntity.ok("Bill added successfully");
    }

    @GetMapping("/{id}")
    public Bill getBillById(@PathVariable Long id) {
        return billService.getBillById(id);
    }

    @PostMapping("/{billId}/items")
    public ResponseEntity<String> addBillItem(@PathVariable Long billId, @RequestBody BillItem billItem) {
        billService.addBillItem(billId, billItem);
        return ResponseEntity.ok("Item added successfully in bill " + billId);
    }

    @PostMapping("/items/participants")
    public ResponseEntity<String> addBillParticipant(@RequestBody BillParticipantRequestDto participantDto) {
        billService.addBillParticipantByDetails(participantDto);
        return ResponseEntity.ok(participantDto.getItemName() + " add in bill " + participantDto.getBillId() + " in group " + participantDto.getGroupName());
    }

    @GetMapping("/{billId}/debts")
    public List<DebtDto> calculateDebtsByConsumption(@PathVariable Long billId) {
        return billService.calculateDebtBasedOnConsumption(billId);
    }

    @PostMapping("/shared-item")
    public ResponseEntity<String> addSharedBillItem(@RequestBody SharedBillItemRequestDto requestDto) {
        billService.addSharedBillItem(requestDto.getBillId(), requestDto.getItemName(), requestDto.getItemPrice());
        return ResponseEntity.ok("Shared bill item added successfully");
    }

    @GetMapping("/group/{groupName}/debts")
    public List<DebtDto> getGroupDebts(@PathVariable String groupName) {
        return billService.calculateGroupDebts(groupName);
    }
}
