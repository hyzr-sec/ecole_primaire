package com.ecole_primaire;

public class Matiere {
    private int matiereId;
    private String nomMatiere;
    private Integer enseignantId;  // Can be null if no enseignant is assigned

    // Constructor
    public Matiere(int matiereId, String nomMatiere) {
        this.matiereId = matiereId;
        this.nomMatiere = nomMatiere;
    }
    public Matiere(int matiereId, String nomMatiere, Integer enseignantId) {
        this.matiereId = matiereId;
        this.nomMatiere = nomMatiere;
        this.enseignantId = enseignantId;
    }

    // Getters
    public int getId() {
        return matiereId;
    }

    public String getName() {
        return nomMatiere;
    }

    public Integer getEnseignantId() {
        return enseignantId;
    }

    // Setters
    public void setEnseignantId(Integer enseignantId) {
        this.enseignantId = enseignantId;
    }

    // Override toString() to display name and enseignant_id together
    @Override
    public String toString() {
        return nomMatiere + " (Enseignant ID: " + (enseignantId != null ? enseignantId : "None") + ")";
    }
}
