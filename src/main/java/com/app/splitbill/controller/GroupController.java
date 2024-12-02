package com.app.splitbill.controller;

import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.GroupMember;
import com.app.splitbill.service.GroupService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }
    @PostMapping("/create")
    public AppGroup createGroup(@RequestBody AppGroup appGroup) {
        return groupService.createGroup(appGroup);
    }

    @GetMapping("/{id}")
    public AppGroup getGroupById(@PathVariable Long id) {
        return groupService.getGroupById(id);
    }
    @PostMapping("/{groupId}/members/{userId}")
    public GroupMember addMemberToGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        return groupService.addMemberToGroup(groupId, userId);
    }
}
