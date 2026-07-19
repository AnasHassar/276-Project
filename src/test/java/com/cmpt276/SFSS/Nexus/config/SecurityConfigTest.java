package com.cmpt276.SFSS.Nexus.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RoleBasedAuthSuccessHandlerTest {

    private final RoleBasedAuthSuccessHandler handler = new RoleBasedAuthSuccessHandler();

    @Test
    void onAuthenticationSuccess_forAdmin_redirectsToAdminMenuPage() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "exec@sfu.ca", "n/a", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        handler.onAuthenticationSuccess(request, response, auth);

        verify(response).sendRedirect(eq("/admin-menu.html"));
    }

    @Test
    void onAuthenticationSuccess_forRegularUser_redirectsToHome() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "student@sfu.ca", "n/a", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        handler.onAuthenticationSuccess(request, response, auth);

        verify(response).sendRedirect(eq("/"));
    }
}