package com.cmpt276.SFSS.Nexus.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
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
    
    //cache so TransitLand isn't called on every page load (rate limit)
    private Map<String, Object> cachedResult = null;
    private long cacheTime = 0;
    private static final long CACHE_MS = 60 * 1000; //reuse for 60 sec
    
    private String apiKey;

    private static final String SFU_STOP_ID = "s-c2b86ghk99-sfutransportationcentre~bay1";



    @GetMapping("/api/transit")
    public Map<String, Object> getTransit() {
        long now = System.currentTimeMillis();

        // if fetched recently, reuse instead of calling API again
        if(cachedResult != null && (now - cacheTime) > CACHE_MS) {
            return cachedResult;
        }

        try {
            String url = "https://transit.land/api/v2/rest/stops/" + SFU_STOP_ID + "/departures?apikey=" + apiKey;
        
            RestClient restClient = RestClient.create();
            Map<String, Object> response = restClient.get().uri(url).retrieve().body(Map.class);

            
            List<Map<String, Object>> buses = new ArrayList<>();
            
            //response stops is a list, take first stop
            List<Map<String, Object>> stops = (List<Map<String,Object>>) response.get("stops");
            Map<String, Object> stop = stops.get(0);

            // departure list, one entry per upcoming bus
            List<Map<String, Object>> departures = (List<Map<String, Object>>) stop.get("departures");
            //go through each departure and pull out relevant info
            for (Map<String, Object> dep : departures) {
                //route + destination
                Map<String, Object> trip = (Map<String, Object>) dep.get("trip");
                Map<String, Object> route = (Map<String, Object>) trip.get("route");

                String routeNum = (String) route.get("route_short_name"); // "145"
                String destination = (String) trip.get("trip_headsign"); // "145 Production way Station"
                String time = (String) dep.get("departure_time"); //time in 24 hour clock

                //bus entry and add it to list
                Map<String, Object> bus = new HashMap<>();
                bus.put("route", routeNum);
                bus.put("destination", destination);
                bus.put("minutes", countDown(time, LocalTime.now()));
                buses.add(bus);
                // Only next 5 buses
                if(buses.size() >= 5) {
                    break;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("stop", "SFU Exchange");
            result.put("buses", buses);
            return result;

        } catch (Exception e) {
            if (cachedResult != null) { //if call fails, reuse the last good result
                return cachedResult;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("stop", "SFU Excahnge");
            result.put("buses", new ArrayList<>());
            return result;
        }
    }


    
    // turn 24 hour time into count down
    int countDown(String clockTime, LocalTime now) {
        LocalTime busTime = LocalTime.parse(clockTime); // parse 24 hour time
    
        long minutes = Duration.between(now, busTime).toMinutes(); // Difference (in min)
        if (minutes < 0) {
            minutes = 0; //if bus already left, show 0 (instead of negative)
        }
        return (int) minutes;
    }
}
