package com.app.splitbill.controller;

import com.app.splitbill.dto.BillParticipantRequestDto;
import com.app.splitbill.dto.BillRequestDto;
import com.app.splitbill.dto.DebtDto;
import com.app.splitbill.dto.SharedBillItemRequestDto;
import com.app.splitbill.model.Bill;
import com.app.splitbill.model.BillItem;
import com.app.splitbill.model.BillParticipant;
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
    public Bill createBill(@RequestBody BillRequestDto billRequestDto) {
        return billService.createBillFromRequest(billRequestDto);
    }

    @GetMapping("/{id}")
    public Bill getBillById(@PathVariable Long id) {
        return billService.getBillById(id);
    }

    @PostMapping("/{billId}/items")
    public BillItem addBillItem(@PathVariable Long billId, @RequestBody BillItem billItem) {
        return billService.addBillItem(billId, billItem);
    }

    @PostMapping("/items/participants")
    public BillParticipant addBillParticipant(@RequestBody BillParticipantRequestDto participantDto) {
        return billService.addBillParticipantByDetails(participantDto);
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
}
