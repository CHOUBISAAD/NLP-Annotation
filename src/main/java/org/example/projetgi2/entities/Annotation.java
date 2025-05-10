package org.example.projetgi2.entities;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NamedEntityGraph
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Annotation {
    @Id @GeneratedValue
    private Long id;

    private String choixChoisi;

    @ManyToOne
    private Annotateur annotateur;

    @ManyToOne
    private CoupleTexte texte;


}

