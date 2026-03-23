package ru.skillbox.socialnetwork.integration.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "app.cache")
public class AppCacheProperties {

    private final List<String> cacheNames = new ArrayList<>();

    private final Map<String, CacheProperties> caches = new HashMap<>();

    @Data
    public static class CacheProperties {
        private Duration expiry = Duration.ZERO;
    }
    private CacheType cacheType;

    public interface CacheNames {
        String HH_COUNTRIES = "hh-countries";
        String HH_CITIES = "hh-cities";
    }

    public enum CacheType {
        IN_MEMORY,
        REDIS
    }
}
