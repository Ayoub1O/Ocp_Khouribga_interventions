package com.pfe.itsm.auth.security;

import com.pfe.itsm.auth.config.JwtProperties;
import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.domain.UserRole;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class JwtService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String EXPECTED_JWT_ALGORITHM = "HS256";
    private static final String EXPECTED_JWT_TYPE = "JWT";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.clock = Clock.systemUTC();
        validateSecret(properties.secret());
    }

    public TokenResult generateAccessToken(UserAccount user) {
        Instant issuedAt = Instant.now(clock);
        Instant expiresAt = issuedAt.plus(properties.expirationMinutes(), ChronoUnit.MINUTES);

        Map<String, Object> header = Map.of(
                "alg", EXPECTED_JWT_ALGORITHM,
                "typ", EXPECTED_JWT_TYPE
        );
        Map<String, Object> payload = Map.of(
                "iss", properties.issuer(),
                "sub", user.getEmail(),
                "uid", user.getId().toString(),
                "role", user.getRole().name(),
                "iat", issuedAt.getEpochSecond(),
                "exp", expiresAt.getEpochSecond()
        );

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        String signature = sign(unsignedToken);
        return new TokenResult(unsignedToken + "." + signature, expiresAt);
    }

    public JwtPrincipal validate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new JwtAuthenticationException("Token JWT invalide.");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        JsonNode header = decodeJson(parts[0], "Header JWT invalide.");
        requireText(header, "alg", EXPECTED_JWT_ALGORITHM, "Algorithme JWT invalide.");
        requireText(header, "typ", EXPECTED_JWT_TYPE, "Type JWT invalide.");

        String expectedSignature = sign(unsignedToken);
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            throw new JwtAuthenticationException("Signature JWT invalide.");
        }

        JsonNode payload = decodeJson(parts[1], "Payload JWT invalide.");
        requireText(payload, "iss", properties.issuer(), "Emetteur JWT invalide.");

        Instant expiresAt = Instant.ofEpochSecond(requiredLong(payload, "exp"));
        if (!expiresAt.isAfter(Instant.now(clock))) {
            throw new JwtAuthenticationException("Token JWT expire.");
        }

        String userId = requiredText(payload, "uid");
        UUID.fromString(userId);

        return new JwtPrincipal(
                requiredText(payload, "sub"),
                userId,
                UserRole.valueOf(requiredText(payload, "role"))
        );
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de generer le JWT.", exception);
        }
    }

    private JsonNode decodeJson(String encodedValue, String errorMessage) {
        try {
            byte[] decoded = BASE64_URL_DECODER.decode(encodedValue);
            return objectMapper.readTree(decoded);
        } catch (Exception exception) {
            throw new JwtAuthenticationException(errorMessage);
        }
    }

    private String requiredText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.asString() == null || value.asString().isBlank()) {
            throw new JwtAuthenticationException("Claim JWT obligatoire manquant.");
        }
        return value.asString();
    }

    private void requireText(JsonNode node, String field, String expected, String errorMessage) {
        if (!expected.equals(requiredText(node, field))) {
            throw new JwtAuthenticationException(errorMessage);
        }
    }

    private long requiredLong(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isNumber()) {
            throw new JwtAuthenticationException("Claim JWT obligatoire manquant.");
        }
        return value.asLong();
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de signer le JWT.", exception);
        }
    }

    private void validateSecret(String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET doit contenir au moins 32 octets.");
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigestSupport.constantTimeEquals(left, right);
    }

    public record TokenResult(String token, Instant expiresAt) {
    }

    public record JwtPrincipal(String email, String userId, UserRole role) {
    }
}
