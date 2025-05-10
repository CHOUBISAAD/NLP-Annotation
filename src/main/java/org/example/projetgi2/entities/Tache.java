package org.example.projetgi2.entities;
import jakarta.persistence.*;

import java.time.LocalDate;
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
public class Tache {
    @Id @GeneratedValue
    private Long id;

    private LocalDate dateLimite;

    @ManyToOne
    private Dataset dataset;

    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL)
    private List<CoupleTexte> couples;

    @ManyToOne
    private Annotateur annotateur;

    public void setAnnotateur(Annotateur annotateur) {
        this.annotateur = annotateur;
        if (annotateur != null && !annotateur.getTaches().contains(this)) {
            annotateur.getTaches().add(this);
        }
    }

}
