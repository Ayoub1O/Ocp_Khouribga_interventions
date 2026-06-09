package com.pfe.itsm.inventory.repository;

import com.pfe.itsm.inventory.domain.StockMovement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    List<StockMovement> findByPieceIdOrderByDateMouvementDesc(UUID pieceId);

    List<StockMovement> findByInterventionIdOrderByDateMouvementDesc(UUID interventionId);
}

