package com.app.splitbill.service;

import com.app.splitbill.exception.ResourceNotFoundException;
import com.app.splitbill.exception.ValidationException;
import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.GroupMember;
import com.app.splitbill.repository.GroupMemberRepository;
import com.app.splitbill.repository.GroupRepository;
import com.app.splitbill.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupMemberRepository groupMemberRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private GroupService groupService;

    @Test
    void CreateGroup_ExceptionGroupAlreadyExists() {
        AppGroup appGroup = new AppGroup();
        appGroup.setName("Test Group");

        when(groupRepository.existsByName("Test Group")).thenReturn(true);

        assertThrows(ValidationException.class, () -> groupService.createGroup(appGroup));
    }

    @Test
    void CreateGroup_SuccessCreateGroup() {
        AppGroup appGroup = new AppGroup();
        appGroup.setName("Test Group");

        when(groupRepository.existsByName("Test Group")).thenReturn(false);
        when(groupRepository.save(any(AppGroup.class))).thenReturn(appGroup);

        String result = groupService.createGroup(appGroup);

        assertEquals("You have created a new group", result);
    }

    @Test
    void GetGroupById_ExceptionGroupNotFound() {
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.getGroupById(1L));
    }

    @Test
    void GetGroupById_SuccessGetGroupById() {
        AppGroup appGroup = new AppGroup();
        appGroup.setId(1L);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(appGroup));

        AppGroup result = groupService.getGroupById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void AddMemberToGroup_ExceptionUserAlreadyMember() {
        AppGroup appGroup = new AppGroup();
        appGroup.setName("Test Group");
        AppUser appUser = new AppUser();
        appUser.setUsername("testUser");

        when(groupRepository.findByName("Test Group")).thenReturn(Optional.of(appGroup));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(appUser));
        when(groupMemberRepository.existsByAppGroupAndAppUser(appGroup, appUser)).thenReturn(true);

        assertThrows(ValidationException.class, () -> groupService.addMemberToGroup("Test Group", "testUser"));
    }

    @Test
    void AddMemberToGroup_SuccessAddMembetToGroup() {
        AppGroup appGroup = new AppGroup();
        appGroup.setName("Test Group");
        AppUser appUser = new AppUser();
        appUser.setUsername("testUser");
        GroupMember groupMember = new GroupMember();

        when(groupRepository.findByName("Test Group")).thenReturn(Optional.of(appGroup));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(appUser));
        when(groupMemberRepository.existsByAppGroupAndAppUser(appGroup, appUser)).thenReturn(false);
        when(groupMemberRepository.save(any(GroupMember.class))).thenReturn(groupMember);

        GroupMember result = groupService.addMemberToGroup("Test Group", "testUser");

        assertNotNull(result);
    }
}
