package com.cmpt276.SFSS.Nexus.controller;

import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransitControllerTest {
    
    @Test
    void futureBusReturnsCorrectCountdown() {
        TransitController controller = new TransitController();
        LocalTime now = LocalTime.parse("13:00:00");
        // bus leaving at 13:10 is 10 minutes after now
        int minutes = controller.countDown("13:10:00", now);
        assertEquals(10, minutes);
    }

    @Test void pastBusReturnsZero() {
        TransitController controller = new TransitController();
        LocalTime now = LocalTime.parse("13:00:00");
        //a bus that already left (12:55) should show 0 instead of a negative number
        int minutes = controller.countDown("12:55:00", now);
        assertEquals(0, minutes);
    }
}
