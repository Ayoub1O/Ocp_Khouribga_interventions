package com.pfe.itsm.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class MessageDigestSupport {

    private MessageDigestSupport() {
    }

    public static boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static String sha256Base64Url(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de calculer le hash du token.", exception);
        }
    }
}
