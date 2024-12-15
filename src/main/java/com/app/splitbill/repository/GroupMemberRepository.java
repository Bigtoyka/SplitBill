package com.app.splitbill.repository;

import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByAppGroup(AppGroup appGroup);
    boolean existsByAppGroupAndAppUser(AppGroup appGroup, AppUser appUser);

    @Query("SELECT g FROM AppGroup g JOIN GroupMember gm ON g.id = gm.appGroup.id WHERE gm.appUser.id = :userId")
    List<AppGroup> findGroupsByUserId(@Param("userId") Long userId);

}
