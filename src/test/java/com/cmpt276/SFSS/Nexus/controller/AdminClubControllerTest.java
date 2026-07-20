// package com.cmpt276.SFSS.Nexus.controller;

// import com.cmpt276.SFSS.Nexus.config.PasswordConfig;
// import com.cmpt276.SFSS.Nexus.config.RoleBasedAuthSuccessHandler;
// import com.cmpt276.SFSS.Nexus.config.SecurityConfig;
// import com.cmpt276.SFSS.Nexus.model.Club;
// import com.cmpt276.SFSS.Nexus.repository.ClubRepository;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.TestConfiguration;
// import
// org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
// import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Import;
// import org.springframework.http.MediaType;
// import org.springframework.security.test.context.support.WithMockUser;
// import
// org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.List;
// import java.util.Optional;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import static
// org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest(AdminClubController.class)
// @Import({ SecurityConfig.class, PasswordConfig.class,
// RoleBasedAuthSuccessHandler.class })
// class AdminClubControllerTest {

// // Spring Boot 4.1's @WebMvcTest does not auto-apply Spring Security's
// MockMvc
// // test support, so it must be wired in explicitly for @WithMockUser to work.
// @TestConfiguration
// static class MockMvcSecurityConfig {
// @Bean
// MockMvcBuilderCustomizer securityMockMvcCustomizer() {
// return builder -> builder.apply(SecurityMockMvcConfigurers.springSecurity());
// }
// }

// @Autowired
// private MockMvc mockMvc;

// @MockitoBean
// private ClubRepository clubRepository;

// private Club club(Long id, String name) {
// Club c = new Club();
// c.setId(id);
// c.setName(name);
// c.setCategory("Technology");
// c.setDescription("We build things");
// c.setContactEmail("club@sfu.ca");
// c.setLogoUrl("https://example.com/logo.png");
// return c;
// }

// @Test
// @WithMockUser(username = "exec@sfu.ca", roles = "ADMIN")
// void getAllClubs_asAdmin_returnsClubs() throws Exception {
// when(clubRepository.findAll())
// .thenReturn(List.of(club(1L, "Coding Club"), club(2L, "Chess Club")));

// mockMvc.perform(get("/api/admin/clubs"))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$.length()").value(2))
// .andExpect(jsonPath("$[0].name").value("Coding Club"));
// }

// @Test
// @WithMockUser(username = "student@sfu.ca", roles = "USER")
// void getAllClubs_asRegularUser_isForbidden() throws Exception {
// mockMvc.perform(get("/api/admin/clubs"))
// .andExpect(status().isForbidden());
// }

// @Test
// void getAllClubs_whenNotAuthenticated_redirectsToLogin() throws Exception {
// mockMvc.perform(get("/api/admin/clubs"))
// .andExpect(status().is3xxRedirection())
// .andExpect(redirectedUrl("/login.html"));
// }

// @Test
// @WithMockUser(username = "exec@sfu.ca", roles = "ADMIN")
// void createClub_asAdmin_withValidData_savesAndReturnsClub() throws Exception
// {
// when(clubRepository.save(any(Club.class))).thenAnswer(inv -> {
// Club saved = inv.getArgument(0);
// saved.setId(10L);
// return saved;
// });

// mockMvc.perform(post("/api/admin/clubs")
// .with(csrf())
// .contentType(MediaType.APPLICATION_JSON)
// .content("{\"name\":\"Coding Club\",\"category\":\"Technology\","
// + "\"description\":\"We code\",\"contactEmail\":\"coding@sfu.ca\","
// + "\"logoUrl\":\"https://example.com/logo.png\"}"))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$.id").value(10))
// .andExpect(jsonPath("$.name").value("Coding Club"))
// .andExpect(jsonPath("$.category").value("Technology"));
// }

// @Test
// @WithMockUser(username = "exec@sfu.ca", roles = "ADMIN")
// void createClub_asAdmin_withBlankName_returnsBadRequest() throws Exception {
// mockMvc.perform(post("/api/admin/clubs")
// .with(csrf())
// .contentType(MediaType.APPLICATION_JSON)
// .content("{\"name\":\"\"}"))
// .andExpect(status().isBadRequest());

// verify(clubRepository, never()).save(any(Club.class));
// }

// @Test
// @WithMockUser(username = "exec@sfu.ca", roles = "ADMIN")
// void createClub_withoutCsrfToken_isForbidden() throws Exception {
// mockMvc.perform(post("/api/admin/clubs")
// .contentType(MediaType.APPLICATION_JSON)
// .content("{\"name\":\"Coding Club\"}"))
// .andExpect(status().isForbidden());

// verify(clubRepository, never()).save(any(Club.class));
// }

// @Test
// @WithMockUser(username = "exec@sfu.ca", roles = "ADMIN")
// void updateClub_asAdmin_updatesExistingClub() throws Exception {
// Club existing = club(5L, "Old Name");
// when(clubRepository.findById(5L)).thenReturn(Optional.of(existing));
// when(clubRepository.save(any(Club.class))).thenAnswer(inv ->
// inv.getArgument(0));

// mockMvc.perform(put("/api/admin/clubs/5")
// .with(csrf())
// .contentType(MediaType.APPLICATION_JSON)
// .content("{\"name\":\"New Name\",\"category\":\"Sports\"}"))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$.name").value("New Name"))
// .andExpect(jsonPath("$.category").value("Sports"));
// }

// @Test
// @WithMockUser(username = "exec@sfu.ca", roles = "ADMIN")
// void updateClub_asAdmin_whenClubMissing_returnsNotFound() throws Exception {
// when(clubRepository.findById(999L)).thenReturn(Optional.empty());

// mockMvc.perform(put("/api/admin/clubs/999")
// .with(csrf())
// .contentType(MediaType.APPLICATION_JSON)
// .content("{\"name\":\"Whatever\"}"))
// .andExpect(status().isNotFound());
// }

// @Test
// @WithMockUser(username = "exec@sfu.ca", roles = "ADMIN")
// void deleteClub_asAdmin_deletesExistingClub() throws Exception {
// Club existing = club(7L, "Chess Club");
// when(clubRepository.findById(7L)).thenReturn(Optional.of(existing));

// mockMvc.perform(delete("/api/admin/clubs/7").with(csrf()))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$.deleted").value(7));

// verify(clubRepository).delete(existing);
// }

// @Test
// @WithMockUser(username = "exec@sfu.ca", roles = "ADMIN")
// void deleteClub_asAdmin_whenClubMissing_returnsNotFound() throws Exception {
// when(clubRepository.findById(999L)).thenReturn(Optional.empty());

// mockMvc.perform(delete("/api/admin/clubs/999").with(csrf()))
// .andExpect(status().isNotFound());
// }
// }