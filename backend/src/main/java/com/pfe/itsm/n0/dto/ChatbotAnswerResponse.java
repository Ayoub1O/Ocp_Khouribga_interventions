package com.pfe.itsm.n0.dto;

import com.pfe.itsm.tickets.dto.TicketResponse;
import java.util.List;

public record ChatbotAnswerResponse(
        ChatbotSessionResponse session,
        ChatbotMessageResponse reponse,
        double confidenceScore,
        boolean escaladeRecommandee,
        List<String> sources,
        TicketResponse ticket
) {
}
