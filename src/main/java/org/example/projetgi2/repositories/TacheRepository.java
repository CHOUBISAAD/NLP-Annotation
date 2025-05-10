package org.example.projetgi2.repositories;

import org.example.projetgi2.entities.Annotateur;
import org.example.projetgi2.entities.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TacheRepository extends JpaRepository<Tache, Long> {
    List<Tache> findByDatasetIdAndAnnotateurId(Long datasetId, Long annotateurId);
    List<Tache> findByAnnotateur(Annotateur annotateur);
    @Query("SELECT COUNT(t) FROM Tache t WHERE t.dataset.id = :datasetId")
    long countAnnotateursByDatasetId(@Param("datasetId") Long datasetId);

}
