package com.ecole_primaire;

public class Filiere {
    private int idFiliere;
    private String nomFiliere;

    // Constructor
    public Filiere(int idFiliere, String nomFiliere) {
        this.idFiliere = idFiliere;
        this.nomFiliere = nomFiliere;
    }

    // Getters and Setters
    public int getIdFiliere() {
        return idFiliere;
    }

    public void setIdFiliere(int idFiliere) {
        this.idFiliere = idFiliere;
    }

    public String getNomFiliere() {
        return nomFiliere;
    }

    public void setNomFiliere(String nomFiliere) {
        this.nomFiliere = nomFiliere;
    }
}

