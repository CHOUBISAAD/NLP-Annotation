package org.example.projetgi2.repositories;

import org.example.projetgi2.entities.CoupleTexte;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CoupleTexteRepository extends CrudRepository<CoupleTexte, Long> {
    @Query("SELECT COUNT(c) FROM CoupleTexte c WHERE c.dataset.id = :datasetId")
    long countCouplesByDatasetId(@Param("datasetId") Long datasetId);

}
