package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.config.PasswordConfig;
import com.cmpt276.SFSS.Nexus.config.RoleBasedAuthSuccessHandler;
import com.cmpt276.SFSS.Nexus.config.SecurityConfig;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import({ SecurityConfig.class, PasswordConfig.class, RoleBasedAuthSuccessHandler.class })
class AdminMenuPageSecurityTest {

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

    @Test
    @WithMockUser(username = "exec@sfu.ca", roles = "ADMIN")
    void adminMenuPage_asAdmin_isServed() throws Exception {
        mockMvc.perform(get("/admin-menu.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registered Users")))
                .andExpect(content().string(containsString("Add Events")))
                .andExpect(content().string(containsString("Add Clubs")));
    }

    @Test
    @WithMockUser(username = "student@sfu.ca", roles = "USER")
    void adminMenuPage_asRegularUser_isForbidden() throws Exception {
        mockMvc.perform(get("/admin-menu.html"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminMenuPage_whenNotAuthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin-menu.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html"));
    }
}