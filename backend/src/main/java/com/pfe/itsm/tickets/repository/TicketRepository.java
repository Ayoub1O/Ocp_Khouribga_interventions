package com.pfe.itsm.tickets.repository;

import com.pfe.itsm.tickets.domain.SupportLevel;
import com.pfe.itsm.tickets.domain.Ticket;
import com.pfe.itsm.tickets.domain.TicketStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByReference(String reference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ticket> findLockedById(UUID id);

    List<Ticket> findByNiveauCourantAndTechnicienAssigneIsNullAndStatutIn(
            SupportLevel niveauCourant,
            List<TicketStatus> statuts
    );

    long countByStatut(TicketStatus statut);

    long countByDemandeurId(UUID demandeurId);

    long countByDemandeurIdAndStatut(UUID demandeurId, TicketStatus statut);

    long countByTechnicienAssigneId(UUID technicienId);

    long countByTechnicienAssigneIdAndStatut(UUID technicienId, TicketStatus statut);

    long countByNiveauCourantAndTechnicienAssigneIsNullAndStatutIn(
            SupportLevel niveauCourant,
            List<TicketStatus> statuts
    );

    @Query("select t.statut, count(t) from Ticket t group by t.statut")
    List<Object[]> countGroupedByStatut();

    @Query("select t.niveauCourant, count(t) from Ticket t group by t.niveauCourant")
    List<Object[]> countGroupedByNiveauCourant();

    @Query("select t.statut, count(t) from Ticket t where t.demandeur.id = :demandeurId group by t.statut")
    List<Object[]> countGroupedByStatutForDemandeur(UUID demandeurId);

    @Query("select t.statut, count(t) from Ticket t where t.technicienAssigne.id = :technicienId group by t.statut")
    List<Object[]> countGroupedByStatutForTechnicien(UUID technicienId);
}
