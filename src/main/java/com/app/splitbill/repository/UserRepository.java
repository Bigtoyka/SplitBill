package com.app.splitbill.repository;

import com.app.splitbill.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {
}
