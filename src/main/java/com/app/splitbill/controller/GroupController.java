package com.app.splitbill.controller;

import com.app.splitbill.dto.AddMembersRequestDto;
import com.app.splitbill.dto.RemoveUserFromGroupRequest;
import com.app.splitbill.model.AppGroup;
import com.app.splitbill.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/groups")
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/create")
    public String createGroup(@RequestBody AppGroup appGroup) {
        return groupService.createGroup(appGroup) + ": " + appGroup.getName();
    }

    @GetMapping("/{id}")
    public AppGroup getGroupById(@PathVariable Long id) {
        return groupService.getGroupById(id);
    }

    @PostMapping("/members")
    public ResponseEntity<String> addMembersToGroup(@RequestBody AddMembersRequestDto request) {
        for (String username : request.getUsernames()) {
            groupService.addMemberToGroup(request.getGroupName(), username);
        }
        return ResponseEntity.ok("Users added to the group successfully");
    }

    @DeleteMapping("/members")
    public ResponseEntity<String> removeUserFromGroup(@RequestBody RemoveUserFromGroupRequest request) {
        groupService.removeUserFromGroup(request.getUsername(), request.getGroupName());
        return ResponseEntity.ok("User " + request.getUsername() + " removed from group " + request.getGroupName());
    }
}
