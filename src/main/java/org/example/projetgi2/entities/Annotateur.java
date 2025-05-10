package org.example.projetgi2.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;

import lombok.*;

import java.util.List;


@NamedEntityGraph
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Annotateur extends Utilisateur {

    @OneToMany(mappedBy = "annotateur")
    private List<Annotation> annotations;

    @OneToMany(mappedBy = "annotateur")
    private List<Tache> taches;

    public void addTache(Tache tache) {
        if (!this.taches.contains(tache)) {
            this.taches.add(tache);
            tache.setAnnotateur(this);
        }
    }

    public void removeTache(Tache tache) {
        this.taches.remove(tache);
        if (tache.getAnnotateur() == this) {
            tache.setAnnotateur(null);
        }
    }


    @Override
    public String toString() {
        return "Annotateur{id=" + getId() + ", nom=" + getNom() + "}";
    }

    @Column(nullable = false)
    private boolean actif = true;


}
