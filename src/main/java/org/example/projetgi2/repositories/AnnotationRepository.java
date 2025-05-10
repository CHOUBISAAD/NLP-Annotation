package org.example.projetgi2.repositories;

import org.example.projetgi2.entities.Annotateur;
import org.example.projetgi2.entities.Annotation;
import org.example.projetgi2.entities.CoupleTexte;
import org.example.projetgi2.entities.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    boolean existsByAnnotateurAndTexte(Annotateur annotateur, CoupleTexte texte);
    Optional<Annotation> findByAnnotateurAndTexte(Annotateur annotateur, CoupleTexte texte);

    @Query("SELECT t.id, COUNT(a) FROM CoupleTexte c " +
            "JOIN c.tache t " +
            "LEFT JOIN Annotation a ON a.texte = c AND a.annotateur = :annotateur " +
            "WHERE t IN :taches " +
            "GROUP BY t.id")
    List<Object[]> countAnnotationsByTache(@Param("annotateur") Annotateur annotateur,
                                           @Param("taches") List<Tache> taches);

    long countByAnnotateur(Annotateur annotateur);

    long countByAnnotateurAndTexte_Tache(Annotateur annotateur, Tache tache);

    // Pour compter combien d'annotations ont été faites sur un couple de textes
    long countByTexte(CoupleTexte texte);

    @Query("SELECT COUNT(a) FROM Annotation a WHERE a.texte.dataset.id = :datasetId")
    long countAnnotationsByDatasetId(@Param("datasetId") Long datasetId);
}
