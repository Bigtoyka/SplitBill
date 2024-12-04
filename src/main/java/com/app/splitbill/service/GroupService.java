package com.app.splitbill.service;

import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.GroupMember;
import com.app.splitbill.repository.GroupMemberRepository;
import com.app.splitbill.repository.GroupRepository;
import com.app.splitbill.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    public String createGroup(AppGroup appGroup) {
        boolean groupExists = groupRepository.existsByName(appGroup.getName());
        if (groupExists) {
            return "Group with this name already exists";
        }
        groupRepository.save(appGroup);
        return "You have created a new group";
    }

    public AppGroup getGroupById(Long id) {
        return groupRepository.findById(id).orElse(null);
    }

    public GroupMember addMemberToGroup(String groupName, String username) {
        AppGroup group = groupRepository.findByName(groupName)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        GroupMember groupMember = new GroupMember();
        groupMember.setAppGroup(group); // разобраться с тем, если такая запись с таким человеком и группой существует
        groupMember.setAppUser(user);
        return groupMemberRepository.save(groupMember);
    }
}
