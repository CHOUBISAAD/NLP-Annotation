package org.example.projetgi2.entities;

import jakarta.persistence.Entity;

@Entity
public class Administrateur extends Utilisateur {

    // Constructeur vide requis par Hibernate
    public Administrateur() {
        super();
    }

    // Constructeur avec tous les champs (hérités de Utilisateur)
    public Administrateur(Long id, String nom, String prenom, String login, String password, Role role) {
        super(id, nom, prenom, login, password, role);
    }

    // Getters et setters pour les champs hérités (redondants si déjà dans Utilisateur)
    // Facultatifs ici si les méthodes de la classe parent sont public

    @Override
    public Long getId() {
        return super.getId();
    }

    @Override
    public void setId(Long id) {
        super.setId(id);
    }

    @Override
    public String getNom() {
        return super.getNom();
    }

    @Override
    public void setNom(String nom) {
        super.setNom(nom);
    }

    @Override
    public String getPrenom() {
        return super.getPrenom();
    }

    @Override
    public void setPrenom(String prenom) {
        super.setPrenom(prenom);
    }

    @Override
    public String getLogin() {
        return super.getLogin();
    }

    @Override
    public void setLogin(String login) {
        super.setLogin(login);
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public void setPassword(String password) {
        super.setPassword(password);
    }

    @Override
    public Role getRole() {
        return super.getRole();
    }

    @Override
    public void setRole(Role role) {
        super.setRole(role);
    }
}



