package org.example.projetgi2.repositories;

import org.example.projetgi2.entities.Annotateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnotateurRepository extends JpaRepository<Annotateur, Long> {
    List<Annotateur> findByActifTrue();

}
