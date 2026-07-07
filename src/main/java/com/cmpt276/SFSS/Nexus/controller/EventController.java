package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.model.Event;
import com.cmpt276.SFSS.Nexus.repository.EventRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;

    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public ResponseEntity<List<Event>> getEvents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date
    ) {
        Specification<Event> spec = buildSpec(q, source, location, tag, start_date, end_date);
        List<Event> events = eventRepository.findAll(spec,
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.ASC, "startDate"));
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private Specification<Event> buildSpec(String q, String source, String location,
                                            String tag, String startDateStr, String endDateStr) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }
            if (source != null && !source.isBlank()) {
                predicates.add(cb.equal(root.get("source"), source));
            }
            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("location")),
                        "%" + location.toLowerCase() + "%"));
            }
            if (tag != null && !tag.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("tags")),
                        "%" + tag.toLowerCase() + "%"));
            }
            if (startDateStr != null && !startDateStr.isBlank()) {
                try {
                    LocalDateTime start = LocalDate.parse(startDateStr).atStartOfDay();
                    predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), start));
                } catch (Exception ignored) {}
            }
            if (endDateStr != null && !endDateStr.isBlank()) {
                try {
                    LocalDateTime end = LocalDate.parse(endDateStr).atTime(23, 59, 59);
                    predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), end));
                } catch (Exception ignored) {}
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
