package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.config.PasswordConfig;
import com.cmpt276.SFSS.Nexus.config.RoleBasedAuthSuccessHandler;
import com.cmpt276.SFSS.Nexus.config.SecurityConfig;
import com.cmpt276.SFSS.Nexus.model.User;
import com.cmpt276.SFSS.Nexus.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import({ SecurityConfig.class, PasswordConfig.class, RoleBasedAuthSuccessHandler.class })
class AdminUserControllerTest {

    // Spring Boot 4.1's @WebMvcTest does not auto-apply Spring Security's MockMvc
    // test support, so it must be wired in explicitly for @WithMockUser to work.
    @TestConfiguration
    static class MockMvcSecurityConfig {
        @Bean
        MockMvcBuilderCustomizer securityMockMvcCustomizer() {
            return builder -> builder.apply(SecurityMockMvcConfigurers.springSecurity());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    private User user(Long id, String username, String fullName, String role) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setFullName(fullName);
        u.setPassword("$2a$10$shouldneverbeexposedhash");
        u.setRole(role);
        return u;
    }

    @Test
    @WithMockUser(username = "exec@sfu.ca", roles = "ADMIN")
    void getAllUsers_asAdmin_returnsUserSummariesWithoutPasswords() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(
                user(1L, "student@sfu.ca", "Student Person", "ROLE_USER"),
                user(2L, "exec@sfu.ca", "Exec Person", "ROLE_ADMIN")));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("student@sfu.ca"))
                .andExpect(jsonPath("$[0].fullName").value("Student Person"))
                .andExpect(jsonPath("$[0].role").value("ROLE_USER"))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].role").value("ROLE_ADMIN"));
    }

    @Test
    @WithMockUser(username = "student@sfu.ca", roles = "USER")
    void getAllUsers_asRegularUser_isForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_whenNotAuthenticated_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}
