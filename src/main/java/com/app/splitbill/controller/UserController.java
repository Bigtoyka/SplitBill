package com.app.splitbill.controller;

import com.app.splitbill.model.AppUser;
import com.app.splitbill.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public AppUser createUser(@RequestBody AppUser appUser) {
        return userService.createUser(appUser);
    }

    @GetMapping("/{id}")
    public AppUser getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

}
