package com.cmpt276.SFSS.Nexus.service;

import com.cmpt276.SFSS.Nexus.model.Event;
import com.cmpt276.SFSS.Nexus.repository.EventRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EventbriteScraperService {

    private static final Logger log = LoggerFactory.getLogger(EventbriteScraperService.class);

    private static final String DISCOVERY_URL =
            "https://www.eventbrite.ca/d/canada--vancouver/simon-fraser-university/";

    private static final Pattern EVENT_ID_PATTERN = Pattern.compile("-(\\d+)/?$");

    private final EventRepository eventRepository;

    public EventbriteScraperService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void scrapeAndUpsert() {
        log.info("Starting Eventbrite scrape for SFU events");
        List<Event> scraped = scrapeEvents();
        int upserted = 0;
        for (Event e : scraped) {
            try {
                upsertEvent(e);
                upserted++;
            } catch (Exception ex) {
                log.warn("Failed to upsert event '{}': {}", e.getTitle(), ex.getMessage());
            }
        }
        log.info("Eventbrite scrape complete — {} events upserted", upserted);
    }

    private List<Event> scrapeEvents() {
        List<Event> events = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(DISCOVERY_URL)
                    .userAgent("Mozilla/5.0 (compatible; SFSSNexus/1.0)")
                    .timeout(15_000)
                    .get();

            Elements cards = doc.select("div[data-testid=event-card], article.eds-event-card-content, " +
                    "div.search-event-card-wrapper, article[data-event-id]");

            if (cards.isEmpty()) {
                cards = doc.select("article, div.eds-event-card");
            }

            for (Element card : cards) {
                try {
                    Event event = parseCard(card);
                    if (event != null && isRelevant(event)) {
                        events.add(event);
                    }
                } catch (Exception e) {
                    log.debug("Skipping card parse error: {}", e.getMessage());
                }
            }

            if (events.isEmpty()) {
                log.warn("Eventbrite scrape returned 0 relevant events — page structure may have changed");
            }
        } catch (Exception e) {
            log.error("Eventbrite scrape HTTP error: {}", e.getMessage());
        }
        return events;
    }

    private Event parseCard(Element card) {
        String url = extractUrl(card);
        if (url == null || url.isBlank()) return null;

        String externalId = extractEventId(url);
        if (externalId == null) return null;

        String title = extractText(card, "h2, h3, .eds-event-card-content__title, " +
                "[data-testid=event-card-title], .event-card__title");
        if (title == null || title.isBlank()) return null;

        Event event = new Event();
        event.setSource("eventbrite");
        event.setExternalId(externalId);
        event.setTitle(title.trim());
        event.setUrl(url);

        String description = extractText(card, ".eds-event-card-content__sub-title, " +
                "[data-testid=event-card-subtitle], .event-card__description, p");
        event.setDescription(description != null ? description.trim() : "");

        String dateTimeText = extractText(card, "time, .eds-event-card-content__sub-title, " +
                "[data-testid=event-card-datetime], .event-time");
        if (dateTimeText != null) {
            event.setStartDate(parseDateTime(dateTimeText));
        }

        String location = extractText(card, ".eds-event-card-content__primary-address, " +
                "[data-testid=event-card-venue], .event-card__venue, address");
        event.setLocation(location != null ? location.trim() : "");

        String organizer = extractText(card, ".eds-event-card-content__organizer, " +
                "[data-testid=event-card-organizer], .event-card__organizer");
        event.setOrganizer(organizer != null ? organizer.trim() : "");

        String imageUrl = extractImageUrl(card);
        event.setImageUrl(imageUrl);

        return event;
    }

    private String extractUrl(Element card) {
        Element link = card.selectFirst("a[href*='/e/']");
        if (link != null) return link.absUrl("href");
        link = card.selectFirst("a[href]");
        if (link != null) {
            String href = link.absUrl("href");
            if (href.contains("eventbrite")) return href;
        }
        return null;
    }

    private String extractEventId(String url) {
        Matcher m = EVENT_ID_PATTERN.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    private String extractText(Element parent, String cssQuery) {
        for (String selector : cssQuery.split(",\\s*")) {
            Element el = parent.selectFirst(selector.trim());
            if (el != null) {
                String text = el.text();
                if (!text.isBlank()) return text;
            }
        }
        return null;
    }

    private String extractImageUrl(Element card) {
        Element img = card.selectFirst("img[src]");
        if (img != null) return img.attr("src");
        Element picture = card.selectFirst("picture source[srcset]");
        if (picture != null) {
            String srcset = picture.attr("srcset");
            if (!srcset.isBlank()) return srcset.split("\\s+")[0];
        }
        return null;
    }

    private LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) return null;
        String[] patterns = {
                "EEE, MMM d, yyyy h:mm a",
                "EEE, MMM d h:mm a",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm"
        };
        for (String pattern : patterns) {
            try {
                return LocalDateTime.parse(text.trim(),
                        DateTimeFormatter.ofPattern(pattern, java.util.Locale.ENGLISH));
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    private boolean isRelevant(Event event) {
        String venue = event.getLocation() != null ? event.getLocation().toLowerCase() : "";
        String organizer = event.getOrganizer() != null ? event.getOrganizer().toLowerCase() : "";
        String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";
        String combined = venue + " " + organizer + " " + title;
        return combined.contains("simon fraser") || combined.contains("sfu");
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
            eventRepository.save(ev);
        } else {
            eventRepository.save(incoming);
        }
    }
}
