package com.pfe.itsm.n0.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.n0.config.N0AiProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(prefix = "app.n0.ai", name = "enabled", havingValue = "true")
public class GeminiLlmClient implements LlmClient {

    private final N0AiProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiLlmClient(N0AiProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("x-goog-api-key", properties.apiKey())
                .build();
    }

    @Override
    public boolean isConfigured() {
        return properties.geminiEnabled();
    }

    @Override
    public List<Double> embedForDocument(String title, String content) {
        return embed("title: " + safeTitle(title) + " | text: " + content);
    }

    @Override
    public List<Double> embedForQuery(String question) {
        return embed("task: question answering | query: " + question);
    }

    @Override
    public GeneratedAnswer generateAnswer(String prompt) {
        if (!isConfigured()) {
            return new GeneratedAnswer("", true);
        }
        try {
            String responseBody = restClient.post()
                    .uri("/models/{model}:generateContent", properties.generationModel())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "contents", List.of(Map.of(
                                    "role", "user",
                                    "parts", List.of(Map.of("text", prompt)))),
                            "generationConfig", Map.of(
                                    "temperature", properties.temperature(),
                                    "maxOutputTokens", properties.maxOutputTokens())))
                    .retrieve()
                    .body(String.class);

            JsonNode response = readJson(responseBody, "generation Gemini");
            String text = response == null ? "" : response.at("/candidates/0/content/parts/0/text").asText("");
            boolean escalation = text.toLowerCase().contains("escalade recommandee: oui");
            return new GeneratedAnswer(cleanAnswer(text), escalation);
        } catch (RestClientException exception) {
            throw new BusinessException("Le service Gemini est indisponible pour le moment.");
        }
    }

    private List<Double> embed(String text) {
        if (!isConfigured()) {
            return List.of();
        }
        try {
            String responseBody = restClient.post()
                    .uri("/models/{model}:embedContent", properties.embeddingModel())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "content", Map.of("parts", List.of(Map.of("text", text))),
                            "outputDimensionality", properties.embeddingDimension()))
                    .retrieve()
                    .body(String.class);

            JsonNode response = readJson(responseBody, "embedding Gemini");
            JsonNode values = response == null ? null : response.at("/embedding/values");
            if (values == null || !values.isArray()) {
                values = response == null ? null : response.at("/embeddings/0/values");
            }
            if (values == null || !values.isArray()) {
                throw new BusinessException("Reponse d'embedding Gemini invalide.");
            }
            List<Double> embedding = new ArrayList<>();
            values.forEach(value -> embedding.add(value.asDouble()));
            return embedding;
        } catch (RestClientException exception) {
            throw new BusinessException("Le service d'embedding Gemini est indisponible pour le moment.");
        }
    }

    private JsonNode readJson(String responseBody, String operation) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new BusinessException("Reponse vide du service " + operation + ".");
        }
        try {
            return objectMapper.readTree(responseBody);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("Reponse JSON invalide du service " + operation + ".");
        }
    }

    private String safeTitle(String title) {
        return title == null || title.isBlank() ? "none" : title.trim();
    }

    private String cleanAnswer(String text) {
        return text
                .replace("Escalade recommandee: oui", "")
                .replace("Escalade recommandee: non", "")
                .trim();
    }
}
