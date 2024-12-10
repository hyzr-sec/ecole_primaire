package com.ecole_primaire;

public class StudentAverage {

    private String studentName;
    private double average;

    public StudentAverage(String studentName, double average) {
        this.studentName = studentName;
        this.average = average;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }
}
