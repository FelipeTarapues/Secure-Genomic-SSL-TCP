package org.back;

/**
 * Contiene solo los datos que el cliente debe enviar al servidor.
 */
public class Paciente {
    private final String fullName;
    private final String documentId;
    private final int age;
    private final String sex; // "M" o "F"
    private final String contactEmail;
    private final String clinicalNotes;
    private final String fastaPath; // ruta al archivo FASTA local

    public Paciente(String fullName, String documentId, int age, String sex,
                   String contactEmail, String clinicalNotes, String fastaPath) {
        this.fullName = fullName;
        this.documentId = documentId;
        this.age = age;
        this.sex = sex;
        this.contactEmail = contactEmail;
        this.clinicalNotes = clinicalNotes;
        this.fastaPath = fastaPath;
    }

    public String getFullName() { return fullName; }
    public String getDocumentId() { return documentId; }
    public int getAge() { return age; }
    public String getSex() { return sex; }
    public String getContactEmail() { return contactEmail; }
    public String getClinicalNotes() { return clinicalNotes; }
    public String getFastaPath() { return fastaPath; }

    @Override
    public String toString() {
        return "Patient{" +
                "fullName='" + fullName + '\'' +
                ", documentId='" + documentId + '\'' +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", clinicalNotes='" + clinicalNotes + '\'' +
                ", fastaPath='" + fastaPath + '\'' +
                '}';
    }
}
