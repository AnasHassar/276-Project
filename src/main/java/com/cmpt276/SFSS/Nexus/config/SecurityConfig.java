package com.cmpt276.SFSS.Nexus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/login.html", "/register", "/register.html",
                                                                "/css/**", "/js/**", "/images/**",
                                                                "/api/weather", "/weather.html",
                                                                "/api/events", "/api/events/**",
                                                                "/events.html", "/event-detail.html")
                                                .permitAll()
                                                .requestMatchers("/admin/**", "/admin-events.html", "/admin-users.html",
                                                                "/api/admin/events/**", "/api/admin/users/**")
                                                .hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .defaultAuthenticationEntryPointFor(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                                                PathPatternRequestMatcher.pathPattern("/api/**")))
                                .formLogin(form -> form
                                                .loginPage("/login.html")
                                                .loginProcessingUrl("/login")
                                                .successHandler(roleBasedSuccessHandler())
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login.html?logout")
                                                .permitAll())
                                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

                return http.build();
        }

        @Bean
        public AuthenticationSuccessHandler roleBasedSuccessHandler() {
                return (request, response, authentication) -> {
                        boolean isAdmin = authentication.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                        String target = request.getContextPath() + (isAdmin ? "/admin-users.html" : "/");
                        response.sendRedirect(target);
                };
        }
}
