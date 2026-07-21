package com.cmpt276.SFSS.Nexus.service;

import com.cmpt276.SFSS.Nexus.model.Event;
import com.cmpt276.SFSS.Nexus.repository.EventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SfuEventsService {

    private static final Logger log = LoggerFactory.getLogger(SfuEventsService.class);

    private static final String BASE_URL = "https://events.sfu.ca/live/json/events/";
    private static final String[] EXTRA_TAG_PATHS = {
            "tag/Computer%20Science",
            "tag/Engineering",
            "tag/Business",
            "tag/Arts",
            "tag/Science",
            "tag/Health%20Sciences"
    };

    private static final DateTimeFormatter[] DT_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"),
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    };

    private final EventRepository eventRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public SfuEventsService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/json")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public void cleanupDuplicates() {
        log.info("Cleaning up duplicate events in database");
        List<Event> all = eventRepository.findAll();
        Map<String, List<Event>> grouped = all.stream()
                .collect(Collectors.groupingBy(this::duplicateKey));

        int removed = 0;
        for (List<Event> group : grouped.values()) {
            if (group.size() > 1) {
                group.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
                for (int i = 1; i < group.size(); i++) {
                    eventRepository.delete(group.get(i));
                    removed++;
                }
            }
        }
        log.info("Removed {} duplicate events", removed);
    }

    private String duplicateKey(Event e) {
        String title = e.getTitle() == null ? "" : e.getTitle().toLowerCase().trim().replaceAll("\\s+", " ");
        String location = e.getLocation() == null ? "" : e.getLocation().toLowerCase().trim().replaceAll("\\s+", " ");
        String start = e.getStartDate() == null ? "" : e.getStartDate().toString();
        return title + "::" + location + "::" + start;
    }

    public void fetchAndUpsert() {
        log.info("Starting SFU Events fetch");
        List<Event> allEvents = new ArrayList<>();

        allEvents.addAll(fetchFromPath(""));
        for (String tag : EXTRA_TAG_PATHS) {
            allEvents.addAll(fetchFromPath(tag));
        }

        Map<String, Event> dedupedMap = new LinkedHashMap<>();
        for (Event e : allEvents) {
            dedupedMap.putIfAbsent(duplicateKey(e), e);
        }

        int upserted = 0;
        for (Event e : dedupedMap.values()) {
            try {
                upsertEvent(e);
                upserted++;
            } catch (Exception ex) {
                log.warn("Failed to upsert SFU event '{}': {}", e.getTitle(), ex.getMessage());
            }
        }
        cleanupDuplicates();
        log.info("SFU Events fetch complete — {} events upserted", upserted);
    }

    private List<Event> fetchFromPath(String path) {
        List<Event> events = new ArrayList<>();
        try {
            String json = restClient.get()
                    .uri(path.isBlank() ? "/" : "/" + path)
                    .header("Cache-Control", "no-cache, no-store")
                    .header("Pragma", "no-cache")
                    .retrieve()
                    .body(String.class);

            if (json == null || json.isBlank()) return events;

            JsonNode root = objectMapper.readTree(json);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    Event ev = parseNode(node);
                    if (ev != null) events.add(ev);
                }
            }
        } catch (RestClientException e) {
            log.warn("SFU Events HTTP error for path '{}': {}", path, e.getMessage());
        } catch (Exception e) {
            log.error("SFU Events parse error for path '{}': {}", path, e.getMessage());
        }
        return events;
    }

    private Event parseNode(JsonNode node) {
        String externalId = getStr(node, "id");
        if (externalId == null || externalId.isBlank()) return null;

        String title = getStr(node, "title");
        if (title == null || title.isBlank()) return null;

        Event event = new Event();
        event.setSource("sfu_events");
        event.setExternalId(externalId);
        event.setTitle(title.trim());

        String description = getStr(node, "description");
        if (description == null) description = getStr(node, "summary");
        event.setDescription(description != null ? description.trim() : "");

        event.setStartDate(parseDateTime(getStr(node, "date")));
        event.setEndDate(parseDateTime(getStr(node, "date_end")));

        String location = getStr(node, "location");
        if (location == null) location = getStr(node, "location_name");
        event.setLocation(location != null ? location.trim() : "");

        String url = getStr(node, "url");
        if (url == null) url = getStr(node, "permalink");
        event.setUrl(url);

        String imageUrl = getStr(node, "thumbnail");
        if (imageUrl == null) imageUrl = getStr(node, "image");
        event.setImageUrl(imageUrl);

        String organizer = getStr(node, "group");
        if (organizer == null) organizer = getStr(node, "department");
        event.setOrganizer(organizer != null ? organizer.trim() : "");

        StringBuilder tagBuilder = new StringBuilder();
        JsonNode tagsNode = node.get("tags");
        if (tagsNode != null && tagsNode.isArray()) {
            for (JsonNode t : tagsNode) {
                if (tagBuilder.length() > 0) tagBuilder.append(",");
                tagBuilder.append(t.asText().trim());
            }
        }
        event.setTags(tagBuilder.toString());

        return event;
    }

    private String getStr(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || n.isNull() || n.asText().isBlank()) return null;
        return n.asText();
    }

    private LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) return null;
        String t = text.trim();
        for (DateTimeFormatter fmt : DT_FORMATS) {
            try {
                return LocalDateTime.parse(t, fmt);
            } catch (DateTimeParseException ignored) {}
            try {
                return ZonedDateTime.parse(t, fmt).toLocalDateTime();
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    private void upsertEvent(Event incoming) {
        Optional<Event> existing = eventRepository.findBySourceAndExternalId(
                incoming.getSource(), incoming.getExternalId());

        if (existing.isPresent()) {
            Event ev = existing.get();
            ev.setTitle(incoming.getTitle());
            ev.setDescription(incoming.getDescription());
            ev.setStartDate(incoming.getStartDate());
            ev.setEndDate(incoming.getEndDate());
            ev.setLocation(incoming.getLocation());
            ev.setUrl(incoming.getUrl());
            ev.setImageUrl(incoming.getImageUrl());
            ev.setOrganizer(incoming.getOrganizer());
            ev.setTags(incoming.getTags());
            eventRepository.save(ev);
        } else {
            eventRepository.save(incoming);
        }
    }
}
