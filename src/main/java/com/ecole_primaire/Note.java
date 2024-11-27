package com.ecole_primaire;

public class Note {
    private String matiere;
    private float note;

    public Note(String matiere, float note) {
        this.matiere = matiere;
        this.note = note;
    }

    public String getMatiere() {
        return matiere;
    }

    public float getNote() {
        return note;
    }
}
