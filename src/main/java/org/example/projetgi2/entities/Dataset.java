package org.example.projetgi2.entities;

import jakarta.persistence.*;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NamedEntityGraph
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor

@Entity
public class Dataset {
    @Id
    @GeneratedValue
    private Long id;

    private String nomDataset;
    private String description;

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CoupleTexte> textes;


    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL,orphanRemoval = true ,fetch = FetchType.LAZY)
    private List<Tache> taches;

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClassePossible> classes;

}


