package com.cmpt276.SFSS.Nexus.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherControllerTest {
    @Test
    void clearCondition() {
        WeatherController controller = new WeatherController();
        assertEquals("Clear", controller.describeWeather("Clear"));
    }

    @Test
    void rainToRainy() {
        WeatherController controller = new WeatherController();
        assertEquals("Rainy", controller.describeWeather("Rain"));

    }

    @Test
    void unknownConditionToClear() {
        WeatherController controller = new WeatherController();
        assertEquals("Clear", controller.describeWeather("somethingWeird"));
    }

}
