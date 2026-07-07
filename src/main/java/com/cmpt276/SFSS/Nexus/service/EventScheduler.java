package com.cmpt276.SFSS.Nexus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EventScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventScheduler.class);

    private final EventbriteScraperService eventbriteScraper;
    private final SfuEventsService sfuEventsService;

    public EventScheduler(EventbriteScraperService eventbriteScraper,
                          SfuEventsService sfuEventsService) {
        this.eventbriteScraper = eventbriteScraper;
        this.sfuEventsService = sfuEventsService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void runHourly() {
        log.info("Event ingestion job started");
        try {
            sfuEventsService.fetchAndUpsert();
        } catch (Exception e) {
            log.error("SFU Events ingestion failed: {}", e.getMessage());
        }
        try {
            eventbriteScraper.scrapeAndUpsert();
        } catch (Exception e) {
            log.error("Eventbrite ingestion failed: {}", e.getMessage());
        }
        log.info("Event ingestion job finished");
    }

    @Scheduled(initialDelay = 5_000, fixedDelay = Long.MAX_VALUE)
    public void runOnStartup() {
        log.info("Running initial event ingestion on startup");
        runHourly();
    }
}
