package com.cmpt276.SFSS.Nexus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http,
                        RoleBasedAuthSuccessHandler successHandler) throws Exception {
                http
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                                                .ignoringRequestMatchers("/api/admin/**"))
                                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/login.html", "/register", "/register.html",
                                                                "/css/**", "/js/**", "/images/**",
                                                                "/api/weather", "/weather.html",
                                                                "/api/events", "/api/events/**",
                                                                "/events.html", "/event-detail.html",
                                                                "/api/clubs", "/api/clubs/**",
                                                                "/clubs.html")
                                                .permitAll()
                                                .requestMatchers("/admin/**", "/admin-events.html", "/admin-users.html",
                                                                "/admin-menu.html", "/admin-clubs.html",
                                                                "/api/admin/**")
                                                .hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login.html")
                                                .loginProcessingUrl("/login")
                                                .successHandler(successHandler)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login.html?logout")
                                                .permitAll())
                                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

                return http.build();
        }
}
