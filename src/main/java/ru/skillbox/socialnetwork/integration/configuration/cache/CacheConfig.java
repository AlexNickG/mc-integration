package ru.skillbox.socialnetwork.integration.configuration.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@EnableConfigurationProperties(AppCacheProperties.class)
public class CacheConfig {

//    @Bean
//    @ConditionalOnProperty(prefix = "app.cache", name = "enable", havingValue = "true")
//    @ConditionalOnExpression("'${app.cache.cacheType}'.equals('inMemory')")//TODO: не использовать inMemory cache
//    public ConcurrentMapCacheManager inMemoryCacheManager(AppCacheProperties appCacheProperties) {//переопределяет кеш-менеджер по умолчанию
//        var cacheManager = new ConcurrentMapCacheManager() {
//            @Override
//            protected Cache createConcurrentMapCache(String name) {
//                return new ConcurrentMapCache(
//                        name,
//                        CacheBuilder.newBuilder()
//                                .expireAfterWrite(appCacheProperties.getCaches().get(name).getExpiry())
//                                .build()
//                                .asMap(),
//                        true
//                );
//            }
//        };
//        var cacheNames = appCacheProperties.getCacheNames();
//        if(!cacheNames.isEmpty()) {
//            cacheManager.setCacheNames(cacheNames);
//        }
//        return cacheManager;
//    }

    @Bean
    @ConditionalOnProperty(prefix = "app.cache", name = "enable", havingValue = "true")
    @ConditionalOnExpression("'${app.cache.cacheType}'.equals('redis')")
    public CacheManager redisCacheManager(AppCacheProperties appCacheProperties,
                                          LettuceConnectionFactory lettuceConnectionFactory) {
        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig();
        Map<String, RedisCacheConfiguration> redisCacheConfiguration = new HashMap<>();

        appCacheProperties.getCacheNames().forEach(cacheName -> {
            redisCacheConfiguration.put(cacheName, RedisCacheConfiguration.defaultCacheConfig().entryTtl(
                    appCacheProperties.getCaches().get(cacheName).getExpiry()
            ));
        });
        return RedisCacheManager.builder(lettuceConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(redisCacheConfiguration)
                .build();
    }
}
