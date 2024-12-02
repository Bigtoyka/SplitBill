package com.app.splitbill.service;

import com.app.splitbill.model.AppUser;
import com.app.splitbill.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser createUser(AppUser appUser) {
        return userRepository.save(appUser);
    }

    public AppUser getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
