package com.app.splitbill.repository;

import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByAppGroup(AppGroup appGroup);
}
