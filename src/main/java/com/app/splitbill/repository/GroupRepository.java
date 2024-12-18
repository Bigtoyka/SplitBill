package com.app.splitbill.repository;

import com.app.splitbill.model.AppGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<AppGroup, Long> {
    boolean existsByName(String name);
    Optional<AppGroup> findByName(String name);

}
