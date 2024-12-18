package com.app.splitbill.controller;

import com.app.splitbill.dto.AddMembersRequestDto;
import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.GroupMember;
import com.app.splitbill.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(GroupController.class)
public class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @Test
    public void testCreateGroup() throws Exception {
        AppGroup appGroup = new AppGroup();
        appGroup.setName("TestGroup");

        when(groupService.createGroup(any(AppGroup.class))).thenReturn("Group created successfully");

        mockMvc.perform(post("/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(appGroup)))
                .andExpect(status().isOk())
                .andExpect(content().string("Group created successfully: TestGroup"));
    }

    @Test
    public void testGetGroupById() throws Exception {
        Long groupId = 1L;
        AppGroup appGroup = new AppGroup();
        appGroup.setId(groupId);
        appGroup.setName("TestGroup");

        when(groupService.getGroupById(groupId)).thenReturn(appGroup);

        mockMvc.perform(get("/groups/{id}", groupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(groupId))
                .andExpect(jsonPath("$.name").value("TestGroup"));
    }

    @Test
    public void testAddMembersToGroup() throws Exception {
        AddMembersRequestDto requestDto = new AddMembersRequestDto();
        requestDto.setGroupName("TestGroup");
        requestDto.setUsernames(Arrays.asList("User1", "User2"));

        GroupMember mockGroupMember = new GroupMember();
        AppGroup mockGroup = new AppGroup();
        mockGroup.setName("TestGroup");
        AppUser mockUser = new AppUser();
        mockUser.setUsername("User1");
        mockGroupMember.setAppGroup(mockGroup);
        mockGroupMember.setAppUser(mockUser);

        when(groupService.addMemberToGroup(eq("TestGroup"), anyString())).thenReturn(mockGroupMember);

        mockMvc.perform(post("/groups/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Users added to the group successfully"));
    }
}
