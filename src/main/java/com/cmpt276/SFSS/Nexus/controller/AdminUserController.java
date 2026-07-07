package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.model.User;
import com.cmpt276.SFSS.Nexus.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserSummary>> getAllUsers() {
        List<UserSummary> users = userRepository.findAll().stream()
                .map(UserSummary::from)
                .toList();
        return ResponseEntity.ok(users);
    }

    // Deliberately excludes the password hash — never expose it over the API.
    public record UserSummary(Long id, String username, String fullName, String role) {
        static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getUsername(), user.getFullName(), user.getRole());
        }
    }
}
