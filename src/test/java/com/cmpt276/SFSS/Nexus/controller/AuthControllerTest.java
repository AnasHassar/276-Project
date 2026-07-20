package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.config.PasswordConfig;
import com.cmpt276.SFSS.Nexus.config.SecurityConfig;
import com.cmpt276.SFSS.Nexus.model.User;
import com.cmpt276.SFSS.Nexus.service.UserService;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.cmpt276.SFSS.Nexus.config.PasswordConfig;
import com.cmpt276.SFSS.Nexus.config.RoleBasedAuthSuccessHandler;
import com.cmpt276.SFSS.Nexus.config.SecurityConfig;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, PasswordConfig.class, RoleBasedAuthSuccessHandler.class })
class AuthControllerTest {

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
    private UserService userService;

    @Test
    void register_withValidNewAccount_createsUserAndRedirectsToLogin() throws Exception {
        when(userService.usernameExists("newuser@sfu.ca")).thenReturn(false);

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("fullName", "New User")
                .param("username", "newuser@sfu.ca")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .isEqualTo("/login.html?registered"));

        verify(userService).registerUser("New User", "newuser@sfu.ca", "password123", "ROLE_USER");
    }

    @Test
    void register_withExistingUsername_redirectsBackToRegisterWithError() throws Exception {
        when(userService.usernameExists("taken@sfu.ca")).thenReturn(true);

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("fullName", "Some User")
                .param("username", "taken@sfu.ca")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .startsWith("/register.html?error="));

        verify(userService, never()).registerUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void register_withBlankFields_redirectsBackToRegisterWithError() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("fullName", "")
                .param("username", "")
                .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .startsWith("/register.html?error="));

        verify(userService, never()).registerUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void register_withoutCsrfToken_isForbidden() throws Exception {
        mockMvc.perform(post("/register")
                .param("fullName", "New User")
                .param("username", "newuser@sfu.ca")
                .param("password", "password123"))
                .andExpect(status().isForbidden());

        verify(userService, never()).registerUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void register_withValidExecCode_createsAdminAccount() throws Exception {
        when(userService.usernameExists("exec@sfu.ca")).thenReturn(false);

        // Deliberately built as a fresh (non-interned) String instance rather than
        // the literal "a" -- a regression guard against comparing codes with `==`
        // instead of `.equals()` (that bug let every registration silently fall
        // back to ROLE_USER regardless of the exec code entered).
        String submittedCode = new String("a".toCharArray());

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("fullName", "Exec User")
                .param("username", "exec@sfu.ca")
                .param("password", "password123")
                .param("execCode", submittedCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .isEqualTo("/login.html?registered"));

        verify(userService).registerUser("Exec User", "exec@sfu.ca", "password123", "ROLE_ADMIN");
    }

    @Test
    void register_withInvalidExecCode_redirectsBackToRegisterWithError() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("fullName", "Fake Exec")
                .param("username", "fakeexec@sfu.ca")
                .param("password", "password123")
                .param("execCode", "wrong-code"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl())
                        .startsWith("/register.html?error="));

        verify(userService, never()).registerUser(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "student@sfu.ca", roles = "USER")
    void me_whenAuthenticated_returnsUserInfo() throws Exception {
        User user = new User();
        user.setUsername("student@sfu.ca");
        user.setFullName("Student Person");
        user.setRole("ROLE_USER");
        when(userService.findByUsername("student@sfu.ca")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("student@sfu.ca"))
                .andExpect(jsonPath("$.fullName").value("Student Person"))
                .andExpect(jsonPath("$.isAdmin").value(false));
    }

    @Test
    @WithMockUser(username = "exec@sfu.ca", roles = "ADMIN")
    void me_whenAdmin_reportsIsAdminTrue() throws Exception {
        when(userService.findByUsername("exec@sfu.ca")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(true));
    }

    @Test
    void me_whenNotAuthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html"));
    }
}
