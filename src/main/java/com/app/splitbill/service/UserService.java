package com.app.splitbill.service;

import com.app.splitbill.exception.ResourceNotFoundException;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.repository.BillParticipantRepository;
import com.app.splitbill.repository.GroupMemberRepository;
import com.app.splitbill.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BillParticipantRepository billParticipantRepository;
    private final GroupMemberRepository groupMemberRepository;


    public AppUser createUser(AppUser appUser) {
        log.info("Attempting to create user with username: {}", appUser.getUsername());
        AppUser createdUser = userRepository.save(appUser);
        log.info("User '{}' created successfully with ID: {}", appUser.getUsername(), createdUser.getId());
        return createdUser;
    }

    public AppUser getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public void deleteUserByUsername(String username) {
        log.info("Attempting to delete user with username: {}", username);
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        billParticipantRepository.deleteAllByAppUser(user);
        groupMemberRepository.deleteAllByAppUser(user);
        userRepository.delete(user);
        log.info("User '{}' deleted successfully with ID: {}", user.getUsername(), user.getId());
    }
}
