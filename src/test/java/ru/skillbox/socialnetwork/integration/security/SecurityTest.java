package ru.skillbox.socialnetwork.integration.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.skillbox.socialnetwork.integration.api.HhApiClient;
import ru.skillbox.socialnetwork.integration.configuration.cache.AppCacheProperties;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.security.enabled=true",
        "app.cache.enable=false",
        "eureka.client.enabled=false",
        "management.health.redis.enabled=false",
        "hh.api.base-url=http://localhost:65535",
        "spring.cloud.aws.s3.bucket=test-bucket",
        "spring.cloud.aws.credentials.access-key=test-access-key",
        "spring.cloud.aws.credentials.secret-key=test-secret-key",
        "spring.cloud.aws.region.static=ru-central1",
        "spring.cloud.aws.endpoint=http://localhost:65535"
})
@AutoConfigureMockMvc
class SecurityTest {

    private static final KeyPair KEY_PAIR = generateRsaKeyPair();
    private static final KeyPair FOREIGN_KEY_PAIR = generateRsaKeyPair();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HhApiClient hhApiClient;

    @TestConfiguration
    static class SecurityTestConfig {

        @Bean
        JwtDecoder jwtDecoder() {
            return NimbusJwtDecoder.withPublicKey((RSAPublicKey) KEY_PAIR.getPublic()).build();
        }

        @Bean(name = "redisCacheManager")
        CacheManager redisCacheManager() {
            return new ConcurrentMapCacheManager(
                    AppCacheProperties.CacheNames.HH_COUNTRIES,
                    AppCacheProperties.CacheNames.HH_CITIES
            );
        }
    }

    @Test
    void protectedEndpoint_withoutToker_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/geo/country"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withTokenSignedWithForeignKey_returns401() throws Exception {
        String fakeToken = token(FOREIGN_KEY_PAIR, Instant.now().plusSeconds(300));

        mockMvc.perform(get("/api/va/geo/country")
                .header("Authorization", "Bearer " + fakeToken)).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withValidToken_returns200() throws Exception {
        when(hhApiClient.getAllCountries()).thenReturn(List.of());
        String validToken = token(KEY_PAIR, Instant.now().plusSeconds(300));

        mockMvc.perform(get("/api/v1/geo/country")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    void actuatorHealth_isAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void openApiDocs_areAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    private static String token(KeyPair keyPair, Instant expiresAt) {
        return Jwts.builder()
                .setSubject("test-user")
                .claim("roles", List.of("USER"))
                .setIssuedAt(Date.from(Instant.now().minusSeconds(600)))
                .setExpiration(Date.from(expiresAt))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    private static KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(("RSA"));
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA key pair generation failed", e);
        }
    }
}
