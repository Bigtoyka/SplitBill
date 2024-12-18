package com.app.splitbill.repository;

import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.BillParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BillParticipantRepository extends JpaRepository<BillParticipant, Long> {
    @Query("SELECT SUM(bp.amountPaid) FROM BillParticipant bp WHERE bp.appUser.id = :userId AND bp.billItem.bill.id = :billId")
    BigDecimal findTotalPaidByUserAndBill(@Param("userId") Long userId, @Param("billId") Long billId);
    List<BillParticipant> findByBillItemId(Long billItemId);
    void deleteAllByAppUser(AppUser appUser);

}
