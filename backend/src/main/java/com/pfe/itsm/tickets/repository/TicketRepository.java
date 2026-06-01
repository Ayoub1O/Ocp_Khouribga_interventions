package com.pfe.itsm.tickets.repository;

import com.pfe.itsm.tickets.domain.SupportLevel;
import com.pfe.itsm.tickets.domain.Ticket;
import com.pfe.itsm.tickets.domain.TicketStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByReference(String reference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ticket> findLockedById(UUID id);

    List<Ticket> findByNiveauCourantAndTechnicienAssigneIsNullAndStatutIn(
            SupportLevel niveauCourant,
            List<TicketStatus> statuts
    );
}
