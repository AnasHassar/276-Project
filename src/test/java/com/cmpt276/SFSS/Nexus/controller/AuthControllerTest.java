package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.config.PasswordConfig;
import com.cmpt276.SFSS.Nexus.config.SecurityConfig;
import com.cmpt276.SFSS.Nexus.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, PasswordConfig.class })
class AuthControllerTest {

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
}
