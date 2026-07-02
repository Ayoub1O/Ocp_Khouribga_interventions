package com.pfe.itsm.n0.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.n0.ai")
public record N0AiProperties(
        String provider,
        boolean enabled,
        String apiKey,
        String baseUrl,
        String generationModel,
        String embeddingModel,
        int embeddingDimension,
        int maxOutputTokens,
        double temperature,
        int vectorSearchLimit
) {

    public boolean geminiEnabled() {
        return enabled && "gemini".equalsIgnoreCase(provider) && apiKey != null && !apiKey.isBlank();
    }
}
