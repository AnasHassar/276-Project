package com.cmpt276.SFSS.Nexus.service;

import com.cmpt276.SFSS.Nexus.model.ClassEvent;
import com.cmpt276.SFSS.Nexus.model.User;
import com.cmpt276.SFSS.Nexus.repository.ClassEventRepository;
import com.cmpt276.SFSS.Nexus.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ClassEventService {

    private final ClassEventRepository classEventRepository;
    private final UserRepository userRepository;

    public ClassEventService(ClassEventRepository classEventRepository, UserRepository userRepository) {
        this.classEventRepository = classEventRepository;
        this.userRepository = userRepository;
    }

    public List<ClassEventResponse> listClassEvents(String username) {
        return classEventRepository.findAllByUserUsername(username).stream()
                .sorted(Comparator.comparing(ClassEvent::getDayOfWeek)
                        .thenComparing(ClassEvent::getStartTime))
                .map(this::toResponse)
                .toList();
    }

    public ClassEventResponse getClassEvent(String username, Long id) {
        return toResponse(loadOwnedEvent(username, id));
    }

    public ClassEventResponse createClassEvent(String username, ClassEventRequest request) {
        User owner = loadUser(username);
        ClassEvent classEvent = new ClassEvent();
        classEvent.setUser(owner);
        applyRequest(classEvent, request);
        return toResponse(classEventRepository.save(classEvent));
    }

    public ClassEventResponse updateClassEvent(String username, Long id, ClassEventRequest request) {
        ClassEvent classEvent = loadOwnedEvent(username, id);
        applyRequest(classEvent, request);
        return toResponse(classEventRepository.save(classEvent));
    }

    public void deleteClassEvent(String username, Long id) {
        ClassEvent classEvent = loadOwnedEvent(username, id);
        classEventRepository.delete(classEvent);
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    private ClassEvent loadOwnedEvent(String username, Long id) {
        return classEventRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new NoSuchElementException("Class event not found"));
    }

    private void applyRequest(ClassEvent classEvent, ClassEventRequest request) {
        validateRequest(request);

        classEvent.setTitle(request.getTitle().trim());
        classEvent.setDayOfWeek(request.getDayOfWeek());
        classEvent.setStartTime(request.getStartTime());
        classEvent.setEndTime(request.getEndTime());
        classEvent.setLocation(clean(request.getLocation()));
        classEvent.setNotes(clean(request.getNotes()));

        boolean recurring = Boolean.TRUE.equals(request.getRecurring());
        classEvent.setRecurring(recurring);
        if (recurring) {
            classEvent.setRecurrenceRule(resolveRecurrenceRule(request.getRecurrenceRule()));
            classEvent.setRecurrenceEndDate(request.getRecurrenceEndDate());
        } else {
            classEvent.setRecurrenceRule(null);
            classEvent.setRecurrenceEndDate(null);
        }
    }

    private void validateRequest(ClassEventRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (request.getDayOfWeek() == null) {
            throw new IllegalArgumentException("dayOfWeek is required");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("startTime and endTime are required");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        if (Boolean.TRUE.equals(request.getRecurring())
                && request.getRecurrenceRule() != null
                && !request.getRecurrenceRule().isBlank()
                && !"WEEKLY".equalsIgnoreCase(request.getRecurrenceRule().trim())) {
            throw new IllegalArgumentException("recurrenceRule must be WEEKLY");
        }
    }

    private String resolveRecurrenceRule(String recurrenceRule) {
        if (recurrenceRule == null || recurrenceRule.isBlank()) {
            return "WEEKLY";
        }
        if (!"WEEKLY".equalsIgnoreCase(recurrenceRule.trim())) {
            throw new IllegalArgumentException("recurrenceRule must be WEEKLY");
        }
        return "WEEKLY";
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ClassEventResponse toResponse(ClassEvent classEvent) {
        ClassEventResponse response = new ClassEventResponse();
        response.setId(classEvent.getId());
        response.setTitle(classEvent.getTitle());
        response.setDayOfWeek(classEvent.getDayOfWeek());
        response.setStartTime(classEvent.getStartTime());
        response.setEndTime(classEvent.getEndTime());
        response.setLocation(classEvent.getLocation());
        response.setNotes(classEvent.getNotes());
        response.setRecurring(classEvent.getRecurring());
        response.setRecurrenceRule(classEvent.getRecurrenceRule());
        response.setRecurrenceEndDate(classEvent.getRecurrenceEndDate());
        return response;
    }
}