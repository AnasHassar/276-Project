package com.cmpt276.SFSS.Nexus.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WeatherController {

    @Value("${OPENWEATHER_API_KEY:}")
    private String apiKey;

    // SFU Burnaby Coords
    private static final double LATITUDE = 49.246445;
    private static final double LONGITUDE = -122.994560;

    // cache
    private Map<String, Object> cachedResult = null; 
    private long cacheTime = 0;     
    private static final long CACHE_MS = 15 * 60 * 1000; // reuse for 15 min

    @GetMapping("/api/weather")
    public Map<String, Object> getWeather() {

         long now = System.currentTimeMillis();

         if (cachedResult != null && (now - cacheTime) < CACHE_MS) {
         }   

         try {
            String url = "https://api.openweathermap.org/data/2.5/forecast"
                    + "?lat=" + LATITUDE
                    + "&lon=" + LONGITUDE
                    + "&appid=" + apiKey
                    + "&units=metric"
                    + "&cnt=1";
        // String url = "https://api.open-meteo.com/v1/forecast"
        //         + "?latitude=" + LATITUDE
        //         + "&longitude=" + LONGITUDE
        //         + "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m"
        //         + "&daily=precipitation_probability_max"
        //         + "&timezone=auto"
        //         + "&forecast_days=1";

        RestClient restClient = RestClient.create();
        Map<String, Object> response = restClient.get().uri(url).retrieve().body(Map.class);

        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        Map<String, Object> entry = list.get(0);

        Map<String, Object> main = (Map<String, Object>) entry.get("main");
        double temperature = ((Number) main.get("temp")).doubleValue();
        double feelsLike = ((Number) main.get("feels_like")).doubleValue();
        int humidity = ((Number) main.get("humidity")).intValue();
        
        Map<String, Object> wind = (Map<String, Object>) entry.get("wind");
        double windSpeed = ((Number) wind.get("speed")).doubleValue() * 3.6; // m/s -> km/h

        List<Map<String, Object>> weather = (List<Map<String, Object>>) entry.get("weather");
        String owmCondition = (String) weather.get(0).get("main");

        double pop = ((Number) entry.get("pop")).doubleValue();   // rain-chance
        int rainChance = (int) Math.round(pop * 100); // percent

        // double temperature = ((Number) current.get("temperature_2m")).doubleValue();
        // double feelsLike = ((Number) current.get("apparent_temperature")).doubleValue();
        // int weatherCode = ((Number) current.get("weather_code")).intValue();
        // double windSpeed = ((Number) current.get("wind_speed_10m")).doubleValue();
        // int humidity = ((Number) current.get("relative_humidity_2m")).intValue();

        // Map<String, Object> daily = (Map<String, Object>) response.get("daily");
        // List<Object> rainChances = (List<Object>) daily.get("precipitation_probability_max");
        // int rainChance = ((Number) rainChances.get(0)).intValue();

        Map<String, Object> result = new HashMap<>();
        result.put("temperature", temperature);
        result.put("feelsLike", feelsLike);
        result.put("humidity", humidity);
        result.put("windSpeed", windSpeed);
        result.put("rainChance", rainChance);
        result.put("condition", describeWeather(owmCondition));
        
        cachedResult = result; //save to cache
        return result;
        
    }   catch (Exception e) {
        if (cachedResult != null) {
                return cachedResult;
            }
            Map<String, Object> result = new HashMap<>();
            result.put("error", "unavailable");
            return result;
        }
    }

    String describeWeather(String owmMain) {
        if (owmMain == null) {
            return "Clear";
        }
        switch (owmMain) {
            case "Clear":
                return "Clear";
            case "Clouds":
                return "Partly cloudy";
            case "Rain":
            case "Drizzle":
                return "Rainy";
            case "Snow":
                return "Snowy";
            case "Thunderstorm":
                return "Thunderstorm";
            case "Mist":
            case "Fog":
            case "Haze":
            case "Smoke":
            case "Dust":
            case "Sand":
            case "Ash":
                return "Foggy";
            default:
                return "Clear";
        }
//     private String describeWeather(int code) {
//         if (code == 0)
//             return "Clear";
//         if (code <= 3)
//             return "Partly cloudy";
//         if (code <= 48)
//             return "Foggy";
//         if (code <= 67)
//             return "Rainy";
//         if (code <= 77)
//             return "Snowy";
//         if (code <= 82)
//             return "Rain showers";
//         return "Thunderstorm";
//     }
    }
}
