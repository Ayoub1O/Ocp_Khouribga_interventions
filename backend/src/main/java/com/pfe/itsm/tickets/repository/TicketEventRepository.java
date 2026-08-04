package com.pfe.itsm.tickets.repository;

import com.pfe.itsm.tickets.domain.TicketEvent;
import com.pfe.itsm.tickets.domain.TicketEventType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketEventRepository extends JpaRepository<TicketEvent, UUID> {

    List<TicketEvent> findByTicketIdOrderByDateEvenementAsc(UUID ticketId);

    Optional<TicketEvent> findFirstByTicketIdAndTypeOrderByDateEvenementDesc(UUID ticketId, TicketEventType type);
}
