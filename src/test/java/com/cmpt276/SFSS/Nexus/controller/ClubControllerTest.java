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
// import
// org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.List;
// import java.util.Optional;

// import static org.mockito.Mockito.when;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest(ClubController.class)
// @Import({ SecurityConfig.class, PasswordConfig.class,
// RoleBasedAuthSuccessHandler.class })
// class ClubControllerTest {

// // Spring Boot 4.1's @WebMvcTest does not auto-apply Spring Security's
// MockMvc
// // test support, so it must be wired in explicitly.
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

// private Club sampleClub() {
// Club club = new Club();
// club.setName("Coding Club");
// club.setDescription("Learn to code together");
// club.setCategory("Academic");
// club.setMemberCount(50);
// club.setActive(true);
// return club;
// }

// @Test
// void getClubs_returnsActiveClubs() throws Exception {
// when(clubRepository.findByActiveTrue()).thenReturn(List.of(sampleClub()));

// mockMvc.perform(get("/api/clubs"))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$[0].name").value("Coding Club"))
// .andExpect(jsonPath("$[0].category").value("Academic"));
// }

// @Test
// void getClubs_withSearchQuery_filtersByName() throws Exception {
// when(clubRepository.findByActiveTrue()).thenReturn(List.of(sampleClub()));

// mockMvc.perform(get("/api/clubs").param("q", "coding"))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$[0].name").value("Coding Club"));
// }

// @Test
// void getClubs_withNonMatchingSearch_returnsEmptyList() throws Exception {
// when(clubRepository.findByActiveTrue()).thenReturn(List.of(sampleClub()));

// mockMvc.perform(get("/api/clubs").param("q", "zzz-no-match"))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$").isEmpty());
// }

// @Test
// void getClubs_withCategoryFilter_filtersByCategory() throws Exception {
// when(clubRepository.findByActiveTrue()).thenReturn(List.of(sampleClub()));

// mockMvc.perform(get("/api/clubs").param("category", "Sports"))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$").isEmpty());
// }

// @Test
// void getClub_withUnknownId_returnsNotFound() throws Exception {
// when(clubRepository.findById(999L)).thenReturn(Optional.empty());

// mockMvc.perform(get("/api/clubs/999"))
// .andExpect(status().isNotFound());
// }

// @Test
// void getCategories_returnsCategoryList() throws Exception {
// mockMvc.perform(get("/api/clubs/categories/list"))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$[0]").value("Academic"));
// }
// }
