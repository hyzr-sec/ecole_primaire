package com.ecole_primaire;

public class Matiere {
    private int id;
    private String name;

    public Matiere(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
