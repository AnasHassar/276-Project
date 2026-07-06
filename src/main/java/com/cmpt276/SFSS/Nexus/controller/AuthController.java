package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            Model model) {
        if (username.isBlank() || password.isBlank()) {
            model.addAttribute("error", "Username and password are required.");
            return "register";
        }
        if (userService.usernameExists(username)) {
            model.addAttribute("error", "Username is already taken.");
            return "register";
        }
        String grantedRole = "ADMIN".equalsIgnoreCase(role) ? "ROLE_ADMIN" : "ROLE_USER";
        userService.registerUser(username, password, grantedRole);
        return "redirect:/login?registered";
    }

    @GetMapping("/home")
    public String home(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        return "home";
    }
}
