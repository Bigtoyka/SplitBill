package com.app.splitbill.service;

import com.app.splitbill.exception.ResourceNotFoundException;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser createUser(AppUser appUser) {
        log.info("Attempting to create user with username: {}", appUser.getUsername());
        AppUser createdUser = userRepository.save(appUser);
        log.info("User '{}' created successfully with ID: {}", appUser.getUsername(), createdUser.getId());
        return createdUser;    }

    public AppUser getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
