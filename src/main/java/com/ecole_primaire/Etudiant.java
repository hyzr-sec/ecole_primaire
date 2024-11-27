package com.ecole_primaire;

public class Etudiant {
    private int idEtudiant;
    private String nomEtudiant;
    private int filiereId;
    private String email;
    private String dateNaissance;

    public Etudiant(int idEtudiant, String nomEtudiant, int filiereId, String email, String dateNaissance) {
        this.idEtudiant = idEtudiant;
        this.nomEtudiant = nomEtudiant;
        this.filiereId = filiereId;
        this.email = email;
        this.dateNaissance = dateNaissance;
    }

    public int getIdEtudiant() {
        return idEtudiant;
    }

    public String getNomEtudiant() {
        return nomEtudiant;
    }

    public int getFiliereId() {
        return filiereId;
    }

    public String getEmail() {
        return email;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setIdEtudiant(int idEtudiant) {
        this.idEtudiant = idEtudiant;
    }

    public void setNomEtudiant(String nomEtudiant) {
        this.nomEtudiant = nomEtudiant;
    }

    public void setFiliereId(int filiereId) {
        this.filiereId = filiereId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
}
