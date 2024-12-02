package com.app.splitbill.controller;

import com.app.splitbill.model.Bill;
import com.app.splitbill.model.BillItem;
import com.app.splitbill.model.BillParticipant;
import com.app.splitbill.service.BillService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bills")
public class BillController {
    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }
    @PostMapping("/create")
    public Bill createBill(@RequestBody Bill bill) {
        return billService.createBill(bill);
    }

    @GetMapping("/{id}")
    public Bill getBillById(@PathVariable Long id) {
        return billService.getBillById(id);
    }
    @PostMapping("/{billId}/items")
    public BillItem addBillItem(@PathVariable Long billId, @RequestBody BillItem billItem) {
        return billService.addBillItem(billId, billItem);
    }

    @PostMapping("/items/{billItemId}/participants")
    public BillParticipant addBillParticipant(@PathVariable Long billItemId, @RequestBody BillParticipant billParticipant) {
        return billService.addBillParticipant(billItemId, billParticipant);
    }
}
