package com.ecole_primaire;

public class Enseignant {
    private final int idEnseignant;
    private final String nomEnseignant;
    private final String email;
    private final String matiere; // New field

    public Enseignant(int id, String nom, String email, String matiere) {
        this.idEnseignant = id;
        this.nomEnseignant = nom;
        this.email = email;
        this.matiere = matiere;
    }

    public int getIdEnseignant() {
        return idEnseignant;
    }

    public String getNomEnseignant() {
        return nomEnseignant;
    }

    public String getEmail() {
        return email;
    }

    public String getMatiere() { // Getter for matiere
        return matiere;
    }
}
