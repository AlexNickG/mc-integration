package ru.skillbox.socialnetwork.integration.configuration.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;

import java.security.interfaces.RSAPublicKey;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.nio.file.Files;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

//    @Value("${app.jwt.secret}")
//    private String secret;

    @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}")
    private RSAPublicKey publicKey;

    @Bean
    public JwtDecoder jwtDecoder() {
        //SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return NimbusJwtDecoder
                .withPublicKey(publicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
    }

//    @Bean
//    public JwtDecoder jwtDecoder(
//            @Value("${jwt.public-key-path}") Resource publicKeyResource
//    ) throws Exception {
//
//        byte[] keyBytes = publicKeyResource.getInputStream().readAllBytes();
//
//        String key = new String(keyBytes)
//                .replace("-----BEGIN PUBLIC KEY-----", "")
//                .replace("-----END PUBLIC KEY-----", "")
//                .replaceAll("\\s", "");
//
//        byte[] decoded = java.util.Base64.getDecoder().decode(key);
//
//        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
//        KeyFactory kf = KeyFactory.getInstance("RSA");
//        RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(spec);
//
//        return NimbusJwtDecoder.withPublicKey(publicKey).build();
//    }
}
