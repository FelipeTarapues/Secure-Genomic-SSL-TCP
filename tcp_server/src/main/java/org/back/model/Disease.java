package org.back.model;

public class Disease {
    private String disease_id;
    private String name;
    private int severity;
    private String sequence; // Aquí guardaremos el ADN de la enfermedad



    public Disease(String disease_id, String sequence, int severity, String name) {
        this.disease_id = disease_id;
        this.sequence = sequence;
        this.severity = severity;
        this.name = name;
    }

    public Disease() {

    }

    public String getDisease_id() {
        return disease_id;
    }

    public String getName() {
        return name;
    }

    public int getSeverity() {
        return severity;
    }

    public String getSequence() {
        return sequence;
    }

    public void setDisease_id(String disease_id) {
        this.disease_id = disease_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}