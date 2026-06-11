package com.pfe.itsm.n0.dto;

import jakarta.validation.constraints.Size;

public record StartChatbotSessionRequest(
        @Size(max = 2000) String messageInitial
) {
}
