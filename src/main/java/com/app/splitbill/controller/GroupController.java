package com.app.splitbill.controller;

import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.GroupMember;
import com.app.splitbill.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups")
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/create")
    public String createGroup(@RequestBody AppGroup appGroup) {
        return groupService.createGroup(appGroup);
    }

    @GetMapping("/{id}")
    public AppGroup getGroupById(@PathVariable Long id) {
        return groupService.getGroupById(id);
    }

    @PostMapping("/members")
    public GroupMember addMemberToGroup(@RequestParam String groupName, @RequestParam String username) {
        return groupService.addMemberToGroup(groupName, username);
    }
}
