package com.app.splitbill.repository;

import com.app.splitbill.model.AppGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<AppGroup, Long> {
}
