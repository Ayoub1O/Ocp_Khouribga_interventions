package com.pfe.itsm.n0.ai;

import com.pfe.itsm.users.domain.UserAccount;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SensitiveDataSanitizer {

    private static final Pattern EMAIL = Pattern.compile("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE = Pattern.compile("\\b(?:\\+?\\d[\\d .-]{7,}\\d)\\b");
    private static final Pattern PRIVATE_IP = Pattern.compile("\\b(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3}|172\\.(1[6-9]|2\\d|3[01])\\.\\d{1,3}\\.\\d{1,3})\\b");
    private static final Pattern SECRET_ASSIGNMENT = Pattern.compile("(?i)\\b(password|mot de passe|token|api[-_ ]?key|secret)\\s*[:=]\\s*\\S+");
    private static final Pattern LONG_IDENTIFIER = Pattern.compile("\\b[A-Z0-9][A-Z0-9_-]{11,}\\b");

    public String sanitize(String value, UserAccount user) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String sanitized = EMAIL.matcher(value).replaceAll("[EMAIL]");
        sanitized = PHONE.matcher(sanitized).replaceAll("[TELEPHONE]");
        sanitized = PRIVATE_IP.matcher(sanitized).replaceAll("[IP_PRIVEE]");
        sanitized = SECRET_ASSIGNMENT.matcher(sanitized).replaceAll("$1: [SECRET]");
        sanitized = LONG_IDENTIFIER.matcher(sanitized).replaceAll("[IDENTIFIANT]");

        if (user != null) {
            sanitized = maskWord(sanitized, user.getNom(), "[NOM]");
            sanitized = maskWord(sanitized, user.getPrenom(), "[PRENOM]");
        }
        return sanitized.trim();
    }

    private String maskWord(String value, String word, String replacement) {
        if (word == null || word.isBlank() || word.length() < 2) {
            return value;
        }
        String escaped = Pattern.quote(word.toLowerCase(Locale.ROOT));
        return Pattern.compile("\\b" + escaped + "\\b", Pattern.CASE_INSENSITIVE)
                .matcher(value)
                .replaceAll(replacement);
    }
}
