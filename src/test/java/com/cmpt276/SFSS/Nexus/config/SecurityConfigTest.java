// package com.cmpt276.SFSS.Nexus.config;

// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.junit.jupiter.api.Test;
// import
// org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import
// org.springframework.security.web.authentication.AuthenticationSuccessHandler;

// import java.util.List;

// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// class SecurityConfigTest {

// private final AuthenticationSuccessHandler handler = new
// SecurityConfig().roleBasedSuccessHandler();

// @Test
// void onAuthenticationSuccess_forAdmin_redirectsToAdminUsersPage() throws
// Exception {
// HttpServletRequest request = mock(HttpServletRequest.class);
// HttpServletResponse response = mock(HttpServletResponse.class);
// when(request.getContextPath()).thenReturn("");
// Authentication auth = new UsernamePasswordAuthenticationToken(
// "exec@sfu.ca", "n/a", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

// handler.onAuthenticationSuccess(request, response, auth);

// verify(response).sendRedirect(eq("/admin-users.html"));
// }

// @Test
// void onAuthenticationSuccess_forRegularUser_redirectsToHome() throws
// Exception {
// HttpServletRequest request = mock(HttpServletRequest.class);
// HttpServletResponse response = mock(HttpServletResponse.class);
// when(request.getContextPath()).thenReturn("");
// Authentication auth = new UsernamePasswordAuthenticationToken(
// "student@sfu.ca", "n/a", List.of(new SimpleGrantedAuthority("ROLE_USER")));

// handler.onAuthenticationSuccess(request, response, auth);

// verify(response).sendRedirect(eq("/"));
// }
// }
