package com.app.splitbill.repository;

import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByAppGroup(AppGroup appGroup);
    boolean existsByAppGroupAndAppUser(AppGroup appGroup, AppUser appUser);

}
