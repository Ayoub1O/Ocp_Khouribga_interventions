package com.pfe.itsm.inventory.repository;

import com.pfe.itsm.inventory.domain.SparePart;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SparePartRepository extends JpaRepository<SparePart, UUID> {

    Optional<SparePart> findByReference(String reference);

    boolean existsByReference(String reference);

    @Query("select p from SparePart p where p.actif = true and p.quantiteDisponible <= p.seuilAlerte")
    List<SparePart> findLowStockParts();

    @Query("select count(p) from SparePart p where p.actif = true and p.quantiteDisponible <= p.seuilAlerte")
    long countLowStockParts();
}
