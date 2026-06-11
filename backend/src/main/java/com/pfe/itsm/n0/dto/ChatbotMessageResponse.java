package com.pfe.itsm.n0.dto;

import com.pfe.itsm.n0.domain.ChatbotMessage;
import com.pfe.itsm.n0.domain.ChatbotMessageAuthor;
import java.time.Instant;
import java.util.UUID;

public record ChatbotMessageResponse(
        UUID id,
        ChatbotMessageAuthor auteur,
        String contenu,
        String sourcesUtilisees,
        Double confidenceScore,
        Instant dateCreation
) {

    public static ChatbotMessageResponse from(ChatbotMessage message) {
        return new ChatbotMessageResponse(
                message.getId(),
                message.getAuteur(),
                message.getContenu(),
                message.getSourcesUtilisees(),
                message.getConfidenceScore(),
                message.getDateCreation()
        );
    }
}
