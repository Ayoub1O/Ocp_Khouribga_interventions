package com.pfe.itsm.n0.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendChatbotMessageRequest(
        @NotBlank @Size(max = 2000) String message
) {
}
