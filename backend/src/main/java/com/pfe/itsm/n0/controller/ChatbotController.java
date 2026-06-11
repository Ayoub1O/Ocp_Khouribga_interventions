package com.pfe.itsm.n0.controller;

import com.pfe.itsm.n0.dto.ChatbotAnswerResponse;
import com.pfe.itsm.n0.dto.ChatbotMessageResponse;
import com.pfe.itsm.n0.dto.ChatbotSessionResponse;
import com.pfe.itsm.n0.dto.SendChatbotMessageRequest;
import com.pfe.itsm.n0.dto.StartChatbotSessionRequest;
import com.pfe.itsm.n0.service.ChatbotService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@PreAuthorize("hasAnyRole('DEMANDEUR', 'ADMIN')")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatbotSessionResponse startSession(@Valid @RequestBody(required = false) StartChatbotSessionRequest request) {
        return chatbotService.startSession(request);
    }

    @PostMapping("/sessions/{id}/messages")
    public ChatbotAnswerResponse sendMessage(
            @PathVariable UUID id,
            @Valid @RequestBody SendChatbotMessageRequest request
    ) {
        return chatbotService.sendMessage(id, request);
    }

    @GetMapping("/sessions/{id}/messages")
    public List<ChatbotMessageResponse> messages(@PathVariable UUID id) {
        return chatbotService.messages(id);
    }

    @PostMapping("/sessions/{id}/confirm-resolution")
    public ChatbotSessionResponse confirmResolution(@PathVariable UUID id) {
        return chatbotService.confirmResolution(id);
    }

    @PostMapping("/sessions/{id}/escalate")
    public ChatbotAnswerResponse escalate(@PathVariable UUID id) {
        return chatbotService.escalate(id);
    }
}
