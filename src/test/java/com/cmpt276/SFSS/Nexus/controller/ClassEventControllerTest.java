package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.config.PasswordConfig;
import com.cmpt276.SFSS.Nexus.config.RoleBasedAuthSuccessHandler;
import com.cmpt276.SFSS.Nexus.config.SecurityConfig;
import com.cmpt276.SFSS.Nexus.service.ClassEventRequest;
import com.cmpt276.SFSS.Nexus.service.ClassEventResponse;
import com.cmpt276.SFSS.Nexus.service.ClassEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClassEventController.class)
@Import({ SecurityConfig.class, PasswordConfig.class, RoleBasedAuthSuccessHandler.class })
class ClassEventControllerTest {

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
    private ClassEventService classEventService;

    @Test
    @WithMockUser(username = "student@sfu.ca", roles = "USER")
    void getMyClasses_returnsOnlyOwnedClasses() throws Exception {
        when(classEventService.listClassEvents("student@sfu.ca")).thenReturn(List.of(
                response(1L, "Algorithms", DayOfWeek.MONDAY, "09:30", "11:20", true, "WEEKLY", null),
                response(2L, "Databases", DayOfWeek.WEDNESDAY, "13:00", "14:20", false, null, null)));

        mockMvc.perform(get("/api/class-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Algorithms"));
    }

    @Test
    @WithMockUser(username = "student@sfu.ca", roles = "USER")
    void createMyClass_withValidData_returnsSavedClass() throws Exception {
        when(classEventService.createClassEvent(eq("student@sfu.ca"), any(ClassEventRequest.class)))
                .thenReturn(response(10L, "Algorithms", DayOfWeek.MONDAY, "09:30", "11:20", true, "WEEKLY",
                        LocalDate.of(2026, 12, 1)));

        mockMvc.perform(post("/api/class-events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"title\":\"Algorithms\"," +
                        "\"dayOfWeek\":\"MONDAY\"," +
                        "\"startTime\":\"09:30\"," +
                        "\"endTime\":\"11:20\"," +
                        "\"location\":\"ASB 120\"," +
                        "\"notes\":\"Bring laptop\"," +
                        "\"recurring\":true" +
                        "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.recurrenceRule").value("WEEKLY"));
    }

    @Test
    @WithMockUser(username = "student@sfu.ca", roles = "USER")
    void createMyClass_withInvalidTime_returnsBadRequest() throws Exception {
        when(classEventService.createClassEvent(eq("student@sfu.ca"), any(ClassEventRequest.class)))
                .thenThrow(new IllegalArgumentException("endTime must be after startTime"));

        mockMvc.perform(post("/api/class-events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"title\":\"Algorithms\"," +
                        "\"dayOfWeek\":\"MONDAY\"," +
                        "\"startTime\":\"11:20\"," +
                        "\"endTime\":\"09:30\"" +
                        "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "student@sfu.ca", roles = "USER")
    void getMyClass_whenOwned_returnsClass() throws Exception {
        when(classEventService.getClassEvent("student@sfu.ca", 5L))
                .thenReturn(response(5L, "Algorithms", DayOfWeek.MONDAY, "09:30", "11:20", true, "WEEKLY", null));

        mockMvc.perform(get("/api/class-events/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Algorithms"));
    }

    @Test
    @WithMockUser(username = "student@sfu.ca", roles = "USER")
    void getMyClass_whenNotOwned_returnsNotFound() throws Exception {
        when(classEventService.getClassEvent("student@sfu.ca", 999L))
                .thenThrow(new NoSuchElementException("Class event not found"));

        mockMvc.perform(get("/api/class-events/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "student@sfu.ca", roles = "USER")
    void updateMyClass_updatesOwnedClass() throws Exception {
        when(classEventService.updateClassEvent(eq("student@sfu.ca"), eq(5L), any(ClassEventRequest.class)))
                .thenReturn(response(5L, "Algorithms II", DayOfWeek.TUESDAY, "10:30", "11:20", false, null, null));

        mockMvc.perform(put("/api/class-events/5")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"title\":\"Algorithms II\"," +
                        "\"dayOfWeek\":\"TUESDAY\"," +
                        "\"startTime\":\"10:30\"," +
                        "\"endTime\":\"11:20\"," +
                        "\"recurring\":false" +
                        "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Algorithms II"));
    }

    @Test
    @WithMockUser(username = "student@sfu.ca", roles = "USER")
    void deleteMyClass_deletesOwnedClass() throws Exception {
        mockMvc.perform(delete("/api/class-events/5").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(5));
    }

    @Test
    void getMyClasses_whenNotAuthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/class-events"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html"));
    }

    private ClassEventResponse response(Long id, String title, DayOfWeek dayOfWeek, String start,
            String end, boolean recurring, String recurrenceRule,
            LocalDate recurrenceEndDate) {
        ClassEventResponse response = new ClassEventResponse();
        response.setId(id);
        response.setTitle(title);
        response.setDayOfWeek(dayOfWeek);
        response.setStartTime(LocalTime.parse(start));
        response.setEndTime(LocalTime.parse(end));
        response.setRecurring(recurring);
        response.setRecurrenceRule(recurrenceRule);
        response.setRecurrenceEndDate(recurrenceEndDate);
        response.setLocation("SFU Burnaby");
        response.setNotes("Bring laptop");
        return response;
    }
}