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
public class ClassePossible {
    @Id @GeneratedValue
    private Long id;

    private String nomClasse;

    @ManyToOne
    private Dataset dataset; // ou bien une Tâche selon ton design
}

