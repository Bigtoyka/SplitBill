package com.app.splitbill.repository;

import com.app.splitbill.model.BillParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillParticipantRepository extends JpaRepository<BillParticipant, Long> {
}
