package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.model.User;
import com.cmpt276.SFSS.Nexus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Value("${app.security.exec-signup-code:}")
    private String execSignupCode;

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String execCode) {
        if (fullName == null || fullName.isBlank()
                || username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return "redirect:/register.html?error=" + encode("All fields are required.");
        }
        if (userService.usernameExists(username)) {
            return "redirect:/register.html?error="
                    + encode("An account with that username already exists.");
        }

        String role = "ROLE_USER";
        if (execCode != null && !execCode.isBlank()) {
            if (execSignupCode == null || execSignupCode.isBlank() || !execSignupCode.equals(execCode.trim())) {
                return "redirect:/register.html?error=" + encode("Invalid executive access code.");
            }
            role = "ROLE_ADMIN";
        }

        userService.registerUser(fullName.trim(), username.trim(), password, role);
        return "redirect:/login.html?registered";
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<?> me(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String fullName = userService.findByUsername(authentication.getName())
                .map(User::getFullName)
                .orElse(authentication.getName());
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "fullName", fullName,
                "isAdmin", isAdmin));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
