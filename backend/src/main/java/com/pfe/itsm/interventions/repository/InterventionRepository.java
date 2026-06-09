package com.pfe.itsm.interventions.repository;

import com.pfe.itsm.interventions.domain.Intervention;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterventionRepository extends JpaRepository<Intervention, UUID> {

    List<Intervention> findByTicketIdOrderByDateDebutPrevueAsc(UUID ticketId);

    List<Intervention> findByTechnicienIdOrderByDateDebutPrevueAsc(UUID technicienId);
}

