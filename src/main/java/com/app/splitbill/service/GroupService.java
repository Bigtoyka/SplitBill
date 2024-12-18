package com.app.splitbill.service;

import com.app.splitbill.exception.ResourceNotFoundException;
import com.app.splitbill.exception.ValidationException;
import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.GroupMember;
import com.app.splitbill.repository.GroupMemberRepository;
import com.app.splitbill.repository.GroupRepository;
import com.app.splitbill.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;


    public String createGroup(AppGroup appGroup) {
        log.info("Attempting to create group with name: {}", appGroup.getName());
        boolean groupExists = groupRepository.existsByName(appGroup.getName());
        if (groupExists) {
            log.warn("Group with name '{}' already exists", appGroup.getName());
            throw new ValidationException("Group with this name already exists");
        }
        groupRepository.save(appGroup);
        log.info("Group '{}' created successfully", appGroup.getName());
        return "You have created a new group";
    }

    public AppGroup getGroupById(Long id) {
        log.info("Fetching group with id: {}", id);
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
    }

    public GroupMember addMemberToGroup(String groupName, String username) {
        log.info("Attempting to add user '{}' to group '{}'", username, groupName);

        AppGroup group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with name: " + groupName));

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        boolean isAlreadyMember = groupMemberRepository.existsByAppGroupAndAppUser(group, user);
        if (isAlreadyMember) {
            log.warn("User '{}' is already a member of group '{}'", username, groupName);
            throw new ValidationException("User is already a member of this group");
        }
        GroupMember groupMember = new GroupMember();
        groupMember.setAppGroup(group);
        groupMember.setAppUser(user);
        log.info("User '{}' added to group '{}'", username, groupName);
        return groupMemberRepository.save(groupMember);
    }

    public void removeUserFromGroup(String username, String groupName) {
        log.info("Attempting to remove user '{}' from group '{}'", username, groupName);
        groupMemberRepository.deleteByUsernameAndGroupName(username, groupName);
        log.info("User '{}' removed from group '{}'", username, groupName);
    }

}
