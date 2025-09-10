package org.back;

public class Patient {
    private String patient_id; // Este lo generará el servidor
    private String full_name;
    private String document_id;
    private int age;
    private char sex; // 'M' o 'F'
    private String contact_email;
    private String registration_date;
    private String clinical_notes;
    private String checksum_fasta;
    private int file_size_bytes;
    private String status; // Ej: "ACTIVE" o "INACTIVE"

    public Patient(String patient_id, String full_name, String document_id, int age, char sex, String contact_email, String registration_date, String clinical_notes, String checksum_fasta, int file_size_bytes) {
        this.patient_id = patient_id;
        this.full_name = full_name;
        this.document_id = document_id;
        this.age = age;
        this.sex = sex;
        this.contact_email = contact_email;
        this.registration_date = registration_date;
        this.clinical_notes = clinical_notes;
        this.checksum_fasta = checksum_fasta;
        this.file_size_bytes = file_size_bytes;
    }

    public Patient() {

    }

    public String getPatient_id() {
        return patient_id;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getDocument_id() {
        return document_id;
    }

    public int getAge() {
        return age;
    }

    public char getSex() {
        return sex;
    }

    public String getContact_email() {
        return contact_email;
    }

    public String getRegistration_date() {
        return registration_date;
    }

    public String getClinical_notes() {
        return clinical_notes;
    }

    public String getChecksum_fasta() {
        return checksum_fasta;
    }

    public int getFile_size_bytes() {
        return file_size_bytes;
    }

    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setDocument_id(String document_id) {
        this.document_id = document_id;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setSex(char sex) {
        this.sex = sex;
    }

    public void setContact_email(String contact_email) {
        this.contact_email = contact_email;
    }

    public void setRegistration_date(String registration_date) {
        this.registration_date = registration_date;
    }

    public void setClinical_notes(String clinical_notes) {
        this.clinical_notes = clinical_notes;
    }

    public void setChecksum_fasta(String checksum_fasta) {
        this.checksum_fasta = checksum_fasta;
    }

    public void setFile_size_bytes(int file_size_bytes) {
        this.file_size_bytes = file_size_bytes;
    }

}