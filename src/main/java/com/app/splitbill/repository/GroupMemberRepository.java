package com.app.splitbill.repository;

import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.GroupMember;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByAppGroup(AppGroup appGroup);
    boolean existsByAppGroupAndAppUser(AppGroup appGroup, AppUser appUser);

    @Query("SELECT g FROM AppGroup g JOIN GroupMember gm ON g.id = gm.appGroup.id WHERE gm.appUser.id = :userId")
    List<AppGroup> findGroupsByUserId(@Param("userId") Long userId);

    void deleteAllByAppUser(AppUser appUser);

    @Transactional
    @Modifying
    @Query("DELETE FROM GroupMember gm WHERE gm.appUser.username = :username AND gm.appGroup.name = :groupName")
    void deleteByUsernameAndGroupName(@Param("username") String username, @Param("groupName") String groupName);
    @Query("SELECT gm.appGroup FROM GroupMember gm WHERE gm.appUser.username = :username")
    List<AppGroup> findGroupsByUsername(@Param("username") String username);

}
