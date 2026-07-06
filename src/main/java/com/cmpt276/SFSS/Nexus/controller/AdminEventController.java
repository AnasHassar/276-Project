package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.model.Event;
import com.cmpt276.SFSS.Nexus.repository.EventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/events")
public class AdminEventController {

    private final EventRepository eventRepository;

    public AdminEventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @PostMapping
    public ResponseEntity<?> createClubEvent(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "title is required"));
        }

        Event event = new Event();
        event.setSource("club");
        event.setExternalId(UUID.randomUUID().toString());
        event.setTitle(title.trim());
        event.setDescription(body.getOrDefault("description", ""));
        event.setLocation(body.getOrDefault("location", ""));
        event.setUrl(body.getOrDefault("url", ""));
        event.setImageUrl(body.getOrDefault("imageUrl", ""));
        event.setOrganizer(body.getOrDefault("organizer", ""));
        event.setTags(body.getOrDefault("tags", ""));

        String startStr = body.get("startDate");
        if (startStr != null && !startStr.isBlank()) {
            try {
                event.setStartDate(LocalDateTime.parse(startStr.length() == 16
                        ? startStr + ":00" : startStr));
            } catch (DateTimeParseException ignored) {}
        }
        if (event.getStartDate() == null) {
            event.setStartDate(LocalDateTime.now().plusDays(1));
        }

        String endStr = body.get("endDate");
        if (endStr != null && !endStr.isBlank()) {
            try {
                event.setEndDate(LocalDateTime.parse(endStr.length() == 16
                        ? endStr + ":00" : endStr));
            } catch (DateTimeParseException ignored) {}
        }

        Event saved = eventRepository.save(event);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClubEvent(@PathVariable Long id) {
        return eventRepository.findById(id).map(event -> {
            if (!"club".equals(event.getSource())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Can only delete club-sourced events"));
            }
            eventRepository.delete(event);
            return ResponseEntity.ok(Map.of("deleted", id));
        }).orElse(ResponseEntity.notFound().build());
    }
}
