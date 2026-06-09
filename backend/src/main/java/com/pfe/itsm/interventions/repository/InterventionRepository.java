package com.pfe.itsm.interventions.repository;

import com.pfe.itsm.interventions.domain.Intervention;
import com.pfe.itsm.interventions.domain.InterventionStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InterventionRepository extends JpaRepository<Intervention, UUID> {

    List<Intervention> findByTicketIdOrderByDateDebutPrevueAsc(UUID ticketId);

    List<Intervention> findByTechnicienIdOrderByDateDebutPrevueAsc(UUID technicienId);

    long countByTechnicienId(UUID technicienId);

    long countByTechnicienIdAndStatut(UUID technicienId, InterventionStatus statut);

    @Query("select i.statut, count(i) from Intervention i group by i.statut")
    List<Object[]> countGroupedByStatut();

    @Query("select i.statut, count(i) from Intervention i where i.technicien.id = :technicienId group by i.statut")
    List<Object[]> countGroupedByStatutForTechnicien(UUID technicienId);
}

