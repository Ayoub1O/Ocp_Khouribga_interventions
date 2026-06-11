package com.pfe.itsm.n0.repository;

import com.pfe.itsm.n0.domain.ChatbotSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotSessionRepository extends JpaRepository<ChatbotSession, UUID> {

    Optional<ChatbotSession> findByIdAndDemandeurId(UUID id, UUID demandeurId);
}
