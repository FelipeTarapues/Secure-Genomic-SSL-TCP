package org.back;

import org.back.model.Patient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PatientManager {

    private final String csvFilePath = "C:\\Users\\thoma\\Desktop\\Universidad\\2025-03\\Backend\\Proyecto1\\Secure-Genomic-SSL-TCP\\Secure-Genomic-SSL-TCP\\tcp_server\\database\\patients.csv"; // Ruta al archivo CSV

    public PatientManager() {
        // Podríamos inicializar el archivo aquí si no existe
    }

    /**
     * Crea un nuevo paciente, le asigna un ID único y lo guarda en el archivo CSV.
     * @param patient El objeto Patient con los datos recibidos del cliente.
     * @return El paciente con su nuevo ID asignado.
     */
    public Patient createPatient(Patient patient) throws IOException {
        // 1. Generar un ID único para el nuevo paciente
        String newId = UUID.randomUUID().toString();
        patient.setPatient_id(newId);

        // 2. Formatear los datos del paciente a una línea de CSV
        String csvLine = String.join(",",
                patient.getPatient_id(),
                patient.getFull_name(),
                patient.getDocument_id(),
                String.valueOf(patient.getAge()),
                String.valueOf(patient.getSex()),
                patient.getContact_email(),
                patient.getStatus()
        );

        // Usamos 'true' en FileWriter para que añada la línea al final (append)
        try (PrintWriter out = new PrintWriter(new FileWriter(csvFilePath, true))) {
            out.println(csvLine);
        }

        System.out.println("Nuevo paciente guardado con ID: " + newId);
        return patient;
    }

    public Patient findPatientById(String patientId) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values[0].equals(patientId)) {
                    Patient patient = new Patient();
                    patient.setPatient_id(values[0]);
                    patient.setFull_name(values[1]);
                    patient.setDocument_id(values[2]);
                    patient.setAge(Integer.parseInt(values[3]));
                    patient.setStatus(values[4]);
                    return patient;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean deletePatient(String patientId) throws IOException {
        // Leer todas las líneas del archivo a una lista en memoria
        List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
        boolean patientFound = false;

        // Crear una nueva lista para guardar las líneas modificadas
        List<String> updatedLines = lines.stream()
                .map(line -> {
                    String[] values = line.split(",");
                    if (values[0].equals(patientId)) {
                        // Si es el paciente que buscamos, cambiamos su estado
                        values[values.length - 1] = "INACTIVE";
                        return String.join(",", values);
                    }
                    return line;
                })
                .collect(Collectors.toList());

        // Verificamos si realmente encontramos y modificamos al paciente
        if (!lines.equals(updatedLines)) {
            patientFound = true;
            // 3. Reescribir el archivo completo con las líneas actualizadas
            Files.write(Paths.get(csvFilePath), updatedLines);
        }

        return patientFound;
    }
    /**
     * Actualiza un campo específico de un paciente.
     * @param patientId El ID del paciente a actualizar.
     * @param fieldToUpdate El nombre del campo a cambiar (ej: "full_name", "age").
     * @param newValue El nuevo valor para ese campo.
     * @return true si el paciente fue encontrado y actualizado, false en caso contrario.
     * @throws IOException Si ocurre un error de lectura o escritura.
     */
    public boolean updatePatient(String patientId, String fieldToUpdate, String newValue) throws IOException{
        List<String> lines = Files.readAllLines(Paths.get(csvFilePath));

        List<String> updatedLines = lines.stream()
                .map(line -> {
                    String[] values = line.split(",", -1); // -1 para no descartar campos vacíos
                    if (values.length > 0 && values[0].equals(patientId)) {
                        switch (fieldToUpdate.toLowerCase()) {
                            case "full_name":
                                values[1] = newValue;
                                break;
                            case "document_id":
                                values[2] = newValue;
                                break;
                            case "age":
                                values[3] = newValue;
                                break;
                            case "sex":
                                values[4] = newValue;
                                break;
                            case "contact_email":
                                values[5] = newValue;
                                break;
                            case "status":
                                values[values.length - 1] = newValue;
                                break;
                            default:
                                return line;
                        }
                        return String.join(",", values);
                    }
                    return line;
                })
                .collect(Collectors.toList());

        if (!lines.equals(updatedLines)) {
            Files.write(Paths.get(csvFilePath), updatedLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Paciente con ID " + patientId + " actualizado.");
            return true;
        }

        System.out.println("No se encontró o no se pudo actualizar el paciente con ID " + patientId);
        return false;
    }

}
