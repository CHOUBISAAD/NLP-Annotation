package org.example.projetgi2.entities;

import jakarta.persistence.*;


@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomRole;

    public Role() {}

    public Role(String nomRole) {
        this.nomRole = nomRole;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNomRole() { return nomRole; }

    public void setNomRole(String nomRole) { this.nomRole = nomRole; }
}

