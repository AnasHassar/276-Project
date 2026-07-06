package com.cmpt276.SFSS.Nexus.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DotEnvPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile))
            return;
        try {
            Map<String, Object> props = new HashMap<>();
            Files.lines(envFile)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            props.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
                        }
                    });

            // Parse URL_DB (postgresql://user:password@host[:port]/db) into Spring
            // datasource props
            String urlDb = (String) props.get("URL_DB");
            if (urlDb != null && urlDb.startsWith("postgresql://")) {
                String rest = urlDb.substring("postgresql://".length()); // user:pass@host/db
                int atIdx = rest.indexOf('@');
                if (atIdx >= 0) {
                    String userInfo = rest.substring(0, atIdx);
                    String hostAndDb = rest.substring(atIdx + 1);
                    int colonIdx = userInfo.indexOf(':');
                    String username = colonIdx >= 0 ? userInfo.substring(0, colonIdx) : userInfo;
                    String password = colonIdx >= 0 ? userInfo.substring(colonIdx + 1) : "";
                    props.put("spring.datasource.url", "jdbc:postgresql://" + hostAndDb);
                    props.put("spring.datasource.username", username);
                    props.put("spring.datasource.password", password);
                }
            }

            environment.getPropertySources().addFirst(new MapPropertySource("dotenv", props));
        } catch (IOException e) {
        }
    }
}
