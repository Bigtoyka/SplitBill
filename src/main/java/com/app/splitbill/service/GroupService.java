package com.app.splitbill.service;

import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.GroupMember;
import com.app.splitbill.repository.GroupMemberRepository;
import com.app.splitbill.repository.GroupRepository;
import org.springframework.stereotype.Service;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserService userService;
    private final GroupMemberRepository groupMemberRepository;

    public GroupService(GroupRepository groupRepository, UserService userService, GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.userService = userService;
        this.groupMemberRepository = groupMemberRepository;
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

    public GroupMember addMemberToGroup(Long groupId, Long userId) {
        AppGroup group = getGroupById(groupId);
        AppUser user = userService.getUserById(userId);
        GroupMember groupMember = new GroupMember();
        groupMember.setAppGroup(group); // разобраться с тем, если такая запись с таким человеком и группой существует
        groupMember.setAppUser(user);
        return groupMemberRepository.save(groupMember);
    }
}
