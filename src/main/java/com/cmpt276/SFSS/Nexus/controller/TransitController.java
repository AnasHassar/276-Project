package com.cmpt276.SFSS.Nexus.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class TransitController {
    @Value("${TRANSITLAND_API_KEY:}")
    private String apiKey;

    private static final String SFU_STOP_ID = "s-c2b86ghk99-sfutransportationcentre~bay1";
    private static final String SURR_STOP_ID = "s-c28xud8vyy-surreycentralstation~platform1";
    @GetMapping("/api/transit")
    public Map<String, Object> getTransit(@RequestParam(defaultValue = "sfu") String stop) {

        String stopId;
        String stopName;

        if (stop.equalsIgnoreCase("surrey")) {
            stopId = SURR_STOP_ID;
            stopName = "Surrey Central";
        } else {
            stopId = SFU_STOP_ID;
            stopName = "SFU Exchange";
        }

        String url = "https://transit.land/api/v2/rest/stops/" + stopId + "/departures?apikey=" + apiKey;

        RestClient restClient = RestClient.create();
        Map<String, Object> response = restClient.get().uri(url).retrieve().body(Map.class);

        List<Map<String, Object>> stops = (List<Map<String, Object>>) response.get("stops");
        if (stops.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("stop", "No transit data found");
            result.put("buses", new ArrayList<>());
            return result;
        }

        Map<String, Object> stopData = stops.get(0);
        List<Map<String, Object>> departures = (List<Map<String, Object>>) stopData.get("departures");

        Map<String, Object> result = new HashMap<>();
        result.put("stop", stopName);

        // Surrey Central Station
        if (stop.equalsIgnoreCase("surrey")) {

            List<Map<String, Object>> trains = new ArrayList<>();

            for (Map<String, Object> dep : departures) {
                Map<String, Object> trip = (Map<String, Object>) dep.get("trip");

                String destination = (String) trip.get("trip_headsign");
                String time = (String) dep.get("departure_time");

                Map<String, Object> train = new HashMap<>();
                train.put("destination", destination);
                train.put("minutes", countDown(time, LocalTime.now()));

                trains.add(train);

                if (trains.size() >= 5) {
                    break;
                }
            }

            result.put("type", "train");
            result.put("trains", trains);

        } else {
            //SFU Exchange
            List<Map<String, Object>> buses = new ArrayList<>();

            for (Map<String, Object> dep : departures) {
                Map<String, Object> trip = (Map<String, Object>) dep.get("trip");
                Map<String, Object> route = (Map<String, Object>) trip.get("route");

                String routeNum = (String) route.get("route_short_name");
                String destination = (String) trip.get("trip_headsign");
                String time = (String) dep.get("departure_time");

                Map<String, Object> bus = new HashMap<>();
                bus.put("route", routeNum);
                bus.put("destination", destination);
                bus.put("minutes", countDown(time, LocalTime.now()));

                buses.add(bus);

                if (buses.size() >= 5) {
                    break;
                }
            }

            result.put("type", "bus");
            result.put("buses", buses);
        }

        return result;
    }

    public int countDown(String clockTime, LocalTime now){
        LocalTime busTime = LocalTime.parse(clockTime);

        long minutes = Duration.between(now, busTime).toMinutes();

        if (minutes < 0) {
            minutes = 0;
        }

        return (int) minutes;
    }
}
