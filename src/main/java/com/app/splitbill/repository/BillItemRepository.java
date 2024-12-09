package com.app.splitbill.repository;

import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {
    List<BillItem> findByBillId(Long billId);
    Optional<BillItem> findByItemNameAndBill_AppGroup(String itemName, AppGroup appGroup);

}
