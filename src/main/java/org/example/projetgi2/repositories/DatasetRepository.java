package org.example.projetgi2.repositories;

import org.example.projetgi2.entities.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetRepository extends JpaRepository<Dataset, Long> {
}
