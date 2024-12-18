package com.app.splitbill.security.service;

import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.Role;
import com.app.splitbill.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(String username, String email, String password, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        AppUser user = new AppUser(null, username, email, passwordEncoder.encode(password), role);
        userRepository.save(user);
        return jwtService.generateToken(user.getEmail(), user.getRole());
    }

    public String authenticate(String email, String password) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return jwtService.generateToken(user.getEmail(), user.getRole());
    }
}
