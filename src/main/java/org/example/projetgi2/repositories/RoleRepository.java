package org.example.projetgi2.repositories;

import org.example.projetgi2.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNomRole(String nomRole);
}
