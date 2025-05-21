package org.example.projetgi2.entities;
import jakarta.persistence.*;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NamedEntityGraph

@Setter
@Getter
@NoArgsConstructor
@Entity

public class CoupleTexte {
    @Id @GeneratedValue
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String texte1;

    @Column(columnDefinition = "TEXT")
    private String texte2;


    @ManyToOne
    private Tache tache;

    @ManyToOne
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;
    // ✅ lien vers le dataset auquel ce texte appartient

    @OneToMany(mappedBy = "texte", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Annotation> annotations;

    public CoupleTexte(String texte1, String texte2) {
        this.texte1 = texte1;
        this.texte2 = texte2;
    }

    public void setTache(Tache tache) {
        this.tache = tache;
        if (tache != null && !tache.getCouples().contains(this)) {
            tache.getCouples().add(this);
        }
    }

}

