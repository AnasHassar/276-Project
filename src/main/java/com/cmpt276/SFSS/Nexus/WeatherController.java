package com.cmpt276.SFSS.Nexus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WeatherController {
    
    // SFU Burnaby Coords
    private static final double LATITUDE = 49.246445;
    private static final double LONGITUDE = -122.994560;

    @GetMapping("/api/weather")
    public Map<String, Object> getWeather() {
        //Open-Meteo web address with SFU location
        //Current conditions and rain chance for students planning their day
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + LATITUDE
                + "&longitude=" + LONGITUDE
                + "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m"
                + "&daily=precipitation_probability_max"
                + "&timezone=auto"
                + "&forecast_days=1";


        RestClient restClient = RestClient.create();
        Map<String, Object> response = restClient.get().uri(url).retrieve().body(Map.class);

        //current weather
        Map<String, Object> current = (Map<String, Object>) response.get("current");
        double temperature = ((Number) current.get("temperature_2m")).doubleValue();
        double feelsLike = ((Number) current.get("apparent_temperature")).doubleValue();
        int weatherCode = ((Number) current.get("weather_code")).intValue();
        double windSpeed = ((Number) current.get("wind_speed_10m")).doubleValue();
        int humidity = ((Number) current.get("relative_humidity_2m")).intValue();
        //daily holds today's rain chance
        Map<String, Object> daily = (Map<String, Object>) response.get("daily");
        List<Object> rainChances = (List<Object>) daily.get("precipitation_probability_max");
        int rainChance = ((Number) rainChances.get(0)).intValue();

        // package values for the web page
        Map<String, Object> result = new HashMap<>();
        result.put("temperature", temperature);
        result.put("feelsLike", feelsLike);
        result.put("humidity", humidity);
        result.put("windSpeed", windSpeed);
        result.put("rainChance", rainChance);
        result.put("condition", describeWeather(weatherCode));

        return result;

    }

    //Turn Open-Meteo weather number code into string
    private String describeWeather(int code) {
        if (code == 0) return "Clear";
        if (code <= 3) return "Partly cloudy";
        if (code <= 48) return "Foggy";
        if (code <= 67) return "Rainy";
        if (code <= 77) return "Snowy";
        if (code <= 82) return "Rain showers";
        return "Thunderstorm";
    }

}