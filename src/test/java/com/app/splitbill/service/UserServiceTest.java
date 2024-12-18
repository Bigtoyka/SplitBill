package com.app.splitbill.service;

import com.app.splitbill.exception.ResourceNotFoundException;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void CreateUser_Success() {
        AppUser appUser = new AppUser();
        appUser.setUsername("testUser");

        when(userRepository.save(any(AppUser.class))).thenReturn(appUser);

        AppUser createdUser = userService.createUser(appUser);

        assertNotNull(createdUser);
        assertEquals("testUser", createdUser.getUsername());
    }

    @Test
    void GetUserById_UserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void GetUserById_Success() {
        Long userId = 1L;
        AppUser appUser = new AppUser();
        appUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(appUser));

        AppUser result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
    }
}
