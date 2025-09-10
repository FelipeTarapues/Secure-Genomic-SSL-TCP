package org.back;

import org.back.model.Disease;
import org.back.model.Patient;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Map<String, Disease> diseaseDb;

    public ClientHandler(Socket socket, Map<String, Disease> diseaseDb) {
        this.clientSocket = socket;
        this.diseaseDb = diseaseDb;
    }
    @Override
    public void run() {
        // Usamos try-with-resources para que los streams se cierren solos.
        ServerLogger.log("Iniciando manejo para cliente " + clientSocket.getInetAddress() + " en hilo " + Thread.currentThread().getId());
        try (
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            String commandLine = dis.readUTF(); // Lee la línea de comando, ej: "CREATE_PATIENT|..."
            String[] parts = commandLine.split("\\|"); // Divide la línea por el "|"
            String command = parts[0]; // El primer elemento es el comando
            PatientManager patientManager = new PatientManager(); // Instanciamos el gestor

            ServerLogger.log("Comando '" + command + "' recibido de " + clientSocket.getInetAddress());

            switch (command) {
                case "CREATE_PATIENT":
                    try {
                        String fullName = parts[1];
                        String documentId = parts[2];
                        int age = Integer.parseInt(parts[3]);
                        char sex = parts[4].charAt(0);
                        String email = parts[5];
                        String notes = parts[6];
                        String checksum = parts[7];
                        int fileSize = Integer.parseInt(parts[8]);
                        byte[] fastaBytes = new byte[fileSize];
                        dis.readFully(fastaBytes);
                        ServerLogger.log("Archivo FASTA recibido (" + fastaBytes.length + " bytes) para documento " + documentId);

                        try {
                            String calculatedChecksum = ChecksumUtil.calculateChecksumMD5(fastaBytes);
                            if (!calculatedChecksum.equals(checksum)) {
                                System.out.println("Error de checksum. Esperado: " + checksum + ", Calculado: " + calculatedChecksum);
                                ServerLogger.log("ERROR: Checksum no coincide para documento " + documentId + ". Esperado: " + checksum + ", Calculado: " + calculatedChecksum);
                                out.writeUTF("ERROR|El checksum del archivo no coincide. La transferencia puede estar corrupta.");
                                return; // Detiene la ejecución para este cliente
                            }
                            ServerLogger.log("Checksum verificado exitosamente para documento " + documentId);

                            System.out.println("Checksum verificado exitosamente.");
                        } catch (NoSuchAlgorithmException e) {
                            System.out.println("Error: Algoritmo MD5 no encontrado.");
                            out.writeUTF("ERROR|No se pudo verificar la integridad del archivo en el servidor.");
                            return;
                        }

                        String fastaContent = new String(fastaBytes);

                        if (!FastaValidator.isValid(fastaContent)) {
                            ServerLogger.log("ERROR: Formato FASTA inválido para documento " + documentId);
                            out.writeUTF("ERROR|El contenido del archivo no cumple con el formato FASTA requerido.");
                            return;
                        }
                        ServerLogger.log("Formato FASTA verificado exitosamente para documento " + documentId);

                        System.out.println("Formato FASTA verificado exitosamente.");

                        System.out.println("Archivo FASTA recibido con tamaño: " + fastaBytes.length + " bytes.");

                        Patient newPatient = new Patient();
                        newPatient.setFull_name(fullName);
                        newPatient.setDocument_id(documentId);
                        newPatient.setAge(age);
                        newPatient.setSex(sex);
                        newPatient.setContact_email(email);
                        newPatient.setClinical_notes(notes);
                        newPatient.setChecksum_fasta(checksum);
                        newPatient.setFile_size_bytes(fileSize);

                        // Guardamos los metadatos en el CSV y obtenemos el paciente con su ID
                        Patient createdPatient = patientManager.createPatient(newPatient);
                        String newPatientId = createdPatient.getPatient_id();

                        // Guardar el genoma del paciente en disco
                        savePatientGenome(newPatientId, fastaContent);

                        // Realizar la comparación genómica
                        System.out.println("Realizando comparación genómica para el paciente: " + newPatientId);
                        ServerLogger.log("Realizando comparación genómica para paciente " + newPatientId);
                        GenomeComparator comparator = new GenomeComparator();
                        List<Disease> matches = comparator.findMatches(fastaContent, this.diseaseDb);

                        // Reportar resultados y notificar al cliente según el resultado
                        if (matches.isEmpty()) {
                            // Si no hay coincidencias, enviar respuesta normal
                            System.out.println("No se encontraron coincidencias de enfermedades.");
                            ServerLogger.log("No se encontraron coincidencias para paciente " + newPatientId);
                            ServerLogger.log(matches.size() + " coincidencias encontradas para paciente " + newPatientId);
                            out.writeUTF("OK|" + newPatientId);
                        } else {
                            // Si HAY coincidencias, se registran y se notifica al cliente
                            System.out.println("¡Se encontraron " + matches.size() + " coincidencias!");
                            ReportManager.logDetections(newPatientId, matches);

                            // Construimos una respuesta especial para el cliente
                            String diseaseNames = matches.stream()
                                    .map(Disease::getName)
                                    .collect(Collectors.joining(", "));

                            out.writeUTF("OK|" + newPatientId + "|MATCHES_FOUND:" + diseaseNames);
                        }

                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                        System.out.println("Error: Comando CREATE_PATIENT malformado.");
                        out.writeUTF("ERROR|El formato del comando es incorrecto.");
                    } catch (IOException e) {
                        System.out.println("Error al recibir el archivo FASTA: " + e.getMessage());
                        out.writeUTF("ERROR|Hubo un problema al recibir el archivo.");
                    }
                    break;
                case "GET_PATIENT":
                    try {
                        String patientIdToFind = parts[1];
                        Patient foundPatient = patientManager.findPatientById(patientIdToFind);

                        if (foundPatient != null) {
                            // Si lo encontramos, enviamos sus datos de vuelta
                            String patientData = String.join("|",
                                    foundPatient.getFull_name(),
                                    foundPatient.getDocument_id(),
                                    String.valueOf(foundPatient.getAge())

                            );
                            out.writeUTF("OK|" + patientData);
                        } else {
                            // Si no lo encontramos, enviamos un error
                            out.writeUTF("ERROR|Paciente con ID " + patientIdToFind + " no encontrado.");
                        }
                    } catch (Exception e) {
                        out.writeUTF("ERROR|Error al buscar paciente.");
                    }
                    break;
                case "DELETE_PATIENT":
                    try {
                        String patientIdToDelete = parts[1];
                        boolean success = patientManager.deletePatient(patientIdToDelete);

                        if (success) {
                            out.writeUTF("OK|Paciente " + patientIdToDelete + " marcado como inactivo.");
                        } else {
                            out.writeUTF("ERROR|Paciente " + patientIdToDelete + " no encontrado.");
                        }
                    } catch (Exception e) {
                        out.writeUTF("ERROR|Error al eliminar paciente.");
                    }
                    break;
                case "UPDATE_PATIENT":
                    try {
                        // Verificamos que el comando tenga las 4 partes necesarias
                        if (parts.length < 4) {
                            out.writeUTF("ERROR|Comando UPDATE_PATIENT incompleto.");
                            break;
                        }
                        String patientIdToUpdate = parts[1];
                        String field = parts[2];
                        String value = parts[3];

                        boolean success = patientManager.updatePatient(patientIdToUpdate, field, value);

                        if (success) {
                            out.writeUTF("OK|Paciente " + patientIdToUpdate + " actualizado correctamente.");
                        } else {
                            out.writeUTF("ERROR|No se pudo encontrar o actualizar al paciente " + patientIdToUpdate);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        out.writeUTF("ERROR|Error en el servidor al procesar la solicitud de actualización.");
                    }
                    break;

            }

        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                // Asegúrate de cerrar el socket cuando termines con el cliente.
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void savePatientGenome(String patientId, String fastaContent) {
        // Define la ruta donde se guardarán los genomas de los pacientes
        String filePath = "database/patient_genomes/" + patientId + ".fasta";
        try {
            // Crea la carpeta si no existe
            Files.createDirectories(Paths.get("database/patient_genomes"));
            // Escribe el contenido en el nuevo archivo
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
                writer.print(fastaContent);
            }
            System.out.println("Genoma del paciente " + patientId + " guardado en: " + filePath);
            ServerLogger.log("Genoma del paciente " + patientId + " guardado en: " + filePath);

        } catch (IOException e) {
            System.err.println("Error al guardar el genoma del paciente " + patientId + ": " + e.getMessage());
            ServerLogger.log("ERROR: No se pudo guardar el genoma del paciente " + patientId + " - " + e.getMessage());
        }
    }

}