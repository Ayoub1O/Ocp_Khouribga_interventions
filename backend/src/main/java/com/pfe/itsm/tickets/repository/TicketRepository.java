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

    List<Ticket> findAllByOrderByDateDerniereModificationDesc();

    List<Ticket> findByDemandeurIdOrderByDateDerniereModificationDesc(UUID demandeurId);

    List<Ticket> findByTechnicienAssigneIdOrderByDateDerniereModificationDesc(UUID technicienId);

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

    @Query(
            value = """
                    select cast(date_creation at time zone 'UTC' as date) as jour, count(*) as total
                    from tickets
                    where date_creation >= now() - interval '6 days'
                    group by jour
                    order by jour
                    """,
            nativeQuery = true
    )
    List<Object[]> countDailyVolumeLastSevenDays();

    @Query(
            value = """
                    select cast(date_creation at time zone 'UTC' as date) as jour, count(*) as total
                    from tickets
                    where demandeur_id = :demandeurId
                      and date_creation >= now() - interval '6 days'
                    group by jour
                    order by jour
                    """,
            nativeQuery = true
    )
    List<Object[]> countDailyVolumeLastSevenDaysForDemandeur(UUID demandeurId);
}
