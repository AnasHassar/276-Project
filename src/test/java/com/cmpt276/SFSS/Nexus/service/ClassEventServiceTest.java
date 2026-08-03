package com.cmpt276.SFSS.Nexus.service;

import com.cmpt276.SFSS.Nexus.model.ClassEvent;
import com.cmpt276.SFSS.Nexus.model.User;
import com.cmpt276.SFSS.Nexus.repository.ClassEventRepository;
import com.cmpt276.SFSS.Nexus.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassEventServiceTest {

    @Mock
    private ClassEventRepository classEventRepository;

    @Mock
    private UserRepository userRepository;

    private ClassEventService classEventService;

    @BeforeEach
    void setUp() {
        classEventService = new ClassEventService(classEventRepository, userRepository);
    }

    @Test
    void createRecurringClass_defaultsRecurrenceRuleToWeeklyAndSavesOwnedEvent() {
        User owner = user(1L, "student@sfu.ca");
        when(userRepository.findByUsername("student@sfu.ca")).thenReturn(Optional.of(owner));
        when(classEventRepository.save(any(ClassEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClassEventRequest request = request("Algorithms", DayOfWeek.MONDAY, "09:30", "11:20", true, null, null);

        ClassEventResponse response = classEventService.createClassEvent("student@sfu.ca", request);

        ArgumentCaptor<ClassEvent> eventCaptor = ArgumentCaptor.forClass(ClassEvent.class);
        verify(classEventRepository).save(eventCaptor.capture());

        ClassEvent saved = eventCaptor.getValue();
        assertThat(saved.getUser()).isSameAs(owner);
        assertThat(saved.getTitle()).isEqualTo("Algorithms");
        assertThat(saved.getRecurring()).isTrue();
        assertThat(saved.getRecurrenceRule()).isEqualTo("WEEKLY");
        assertThat(response.getTitle()).isEqualTo("Algorithms");
        assertThat(response.getRecurring()).isTrue();
        assertThat(response.getRecurrenceRule()).isEqualTo("WEEKLY");
    }

    @Test
    void createClass_withInvalidTimeRangeRejectsRequest() {
        User owner = user(1L, "student@sfu.ca");
        when(userRepository.findByUsername("student@sfu.ca")).thenReturn(Optional.of(owner));

        ClassEventRequest request = request("Algorithms", DayOfWeek.MONDAY, "11:20", "09:30", false, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> classEventService.createClassEvent("student@sfu.ca", request));
    }

    @Test
    void listClasses_returnsOnlyOwnedClassesInDayAndTimeOrder() {
        User owner = user(1L, "student@sfu.ca");
        ClassEvent later = event(2L, owner, "Databases", DayOfWeek.WEDNESDAY, "13:00", "14:20", false, null, null);
        ClassEvent earlier = event(1L, owner, "Algorithms", DayOfWeek.MONDAY, "09:30", "11:20", true, "WEEKLY",
                LocalDate.of(2026, 12, 1));
        when(classEventRepository.findAllByUserUsername("student@sfu.ca")).thenReturn(List.of(later, earlier));

        List<ClassEventResponse> classes = classEventService.listClassEvents("student@sfu.ca");

        assertThat(classes).hasSize(2);
        assertThat(classes.get(0).getTitle()).isEqualTo("Algorithms");
        assertThat(classes.get(1).getTitle()).isEqualTo("Databases");
    }

    @Test
    void updateClass_whenOwnedEventExists_updatesTheSeries() {
        User owner = user(1L, "student@sfu.ca");
        ClassEvent existing = event(5L, owner, "Algorithms", DayOfWeek.MONDAY, "09:30", "11:20", true, "WEEKLY", null);
        when(classEventRepository.findByIdAndUserUsername(5L, "student@sfu.ca")).thenReturn(Optional.of(existing));
        when(classEventRepository.save(any(ClassEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClassEventRequest request = request("Algorithms II", DayOfWeek.TUESDAY, "10:30", "11:20", false, null, null);

        ClassEventResponse response = classEventService.updateClassEvent("student@sfu.ca", 5L, request);

        assertThat(response.getTitle()).isEqualTo("Algorithms II");
        assertThat(response.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(response.getRecurring()).isFalse();
        verify(classEventRepository).save(existing);
    }

    @Test
    void deleteClass_whenEventBelongsToAnotherUser_returnsNotFoundStyleFailure() {
        when(classEventRepository.findByIdAndUserUsername(9L, "student@sfu.ca")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> classEventService.deleteClassEvent("student@sfu.ca", 9L));
    }

    @Test
    void getClass_whenEventBelongsToAnotherUser_returnsNotFoundStyleFailure() {
        when(classEventRepository.findByIdAndUserUsername(9L, "student@sfu.ca")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> classEventService.getClassEvent("student@sfu.ca", 9L));
    }

    private User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole("ROLE_USER");
        return user;
    }

    private ClassEventRequest request(String title, DayOfWeek dayOfWeek, String start, String end,
            boolean recurring, String recurrenceRule, LocalDate recurrenceEndDate) {
        ClassEventRequest request = new ClassEventRequest();
        request.setTitle(title);
        request.setDayOfWeek(dayOfWeek);
        request.setStartTime(LocalTime.parse(start));
        request.setEndTime(LocalTime.parse(end));
        request.setRecurring(recurring);
        request.setRecurrenceRule(recurrenceRule);
        request.setRecurrenceEndDate(recurrenceEndDate);
        request.setLocation("SFU Burnaby");
        request.setNotes("Bring laptop");
        return request;
    }

    private ClassEvent event(Long id, User owner, String title, DayOfWeek dayOfWeek, String start, String end,
            boolean recurring, String recurrenceRule, LocalDate recurrenceEndDate) {
        ClassEvent event = new ClassEvent();
        event.setId(id);
        event.setUser(owner);
        event.setTitle(title);
        event.setDayOfWeek(dayOfWeek);
        event.setStartTime(LocalTime.parse(start));
        event.setEndTime(LocalTime.parse(end));
        event.setRecurring(recurring);
        event.setRecurrenceRule(recurrenceRule);
        event.setRecurrenceEndDate(recurrenceEndDate);
        return event;
    }
}