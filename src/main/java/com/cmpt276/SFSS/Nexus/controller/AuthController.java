package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String password) {
        if (fullName == null || fullName.isBlank()
                || username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return "redirect:/register.html?error=" + encode("All fields are required.");
        }
        if (userService.usernameExists(username)) {
            return "redirect:/register.html?error="
                    + encode("An account with that email or student ID already exists.");
        }
        userService.registerUser(fullName.trim(), username.trim(), password, "ROLE_USER");
        return "redirect:/login.html?registered";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
