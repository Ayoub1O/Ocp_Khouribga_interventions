package com.pfe.itsm.n0.repository;

import com.pfe.itsm.n0.domain.ChatbotMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, UUID> {

    List<ChatbotMessage> findBySessionIdOrderByDateCreationAsc(UUID sessionId);
}
