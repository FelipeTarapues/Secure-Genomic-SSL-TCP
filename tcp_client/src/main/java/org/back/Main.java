package org.back;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String trustStorePath = "C:\\Users\\thoma\\Desktop\\Universidad\\2025-03\\Backend\\Proyecto1\\Secure-Genomic-SSL-TCP\\Secure-Genomic-SSL-TCP\\tcp_client\\certs\\truststore.jks";
        String trustStorePassword = "123456";
        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

        TCPClient client = new TCPClient("127.0.0.1", 2020);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Menú Principal del Sistema Genómico ---");
            System.out.println("1. Crear Nuevo Paciente");
            System.out.println("2. Consultar Paciente por ID");
            System.out.println("3. Actualizar Paciente");
            System.out.println("4. Eliminar Paciente (Lógico)");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opción: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Error: Por favor ingrese un número.");
                continue;
            }

            switch (choice) {
                case 1:
                    handleCreatePatient(scanner, client);
                    break;
                case 2:
                    handleGetPatient(scanner, client);
                    break;
                case 3:
                    handleUpdatePatient(scanner, client);
                    break;
                case 4:
                    handleDeletePatient(scanner, client);
                    break;
                case 5:
                    System.out.println("Saliendo del programa...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Opción no válida. Por favor, intente de nuevo.");
            }
        }
    }

    private static void handleCreatePatient(Scanner scanner, TCPClient client) {
        try {
            System.out.println("\n--- Creación de Nuevo Paciente ---");
            Patient patient = new Patient();
            System.out.print("Nombre completo: ");
            patient.setFull_name(scanner.nextLine());
            System.out.print("Documento de identidad: ");
            patient.setDocument_id(scanner.nextLine());
            System.out.print("Edad: ");
            patient.setAge(Integer.parseInt(scanner.nextLine()));
            System.out.print("Sexo (M/F): ");
            patient.setSex(scanner.nextLine().charAt(0));
            System.out.print("Email de contacto: ");
            patient.setContact_email(scanner.nextLine());
            System.out.print("Notas clínicas: ");
            patient.setClinical_notes(scanner.nextLine());
            System.out.print("Ruta completa al archivo FASTA: ");
            String filePath = scanner.nextLine();

            byte[] fastaBytes = Files.readAllBytes(Paths.get(filePath));

            client.sendCreatePatientRequest(patient, fastaBytes);

        } catch (IOException e) {
            System.out.println("Error al leer el archivo FASTA: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error: La edad debe ser un número.");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error de seguridad: " + e.getMessage());
        }
    }

    private static void handleGetPatient(Scanner scanner, TCPClient client) {
        System.out.print("\n-> Ingrese el ID del paciente a consultar: ");
        String idToGet = scanner.nextLine();
        client.sendGetPatientRequest(idToGet);
    }

    private static void handleUpdatePatient(Scanner scanner, TCPClient client) {
        System.out.print("\n-> Ingrese el ID del paciente a actualizar: ");
        String idToUpdate = scanner.nextLine();
        System.out.print("-> Ingrese el campo a actualizar (ej: full_name, age): ");
        String field = scanner.nextLine();
        System.out.print("-> Ingrese el nuevo valor: ");
        String value = scanner.nextLine();
        client.sendUpdatePatientRequest(idToUpdate, field, value);
    }

    private static void handleDeletePatient(Scanner scanner, TCPClient client) {
        System.out.print("\n-> Ingrese el ID del paciente a eliminar: ");
        String idToDelete = scanner.nextLine();
        client.sendDeletePatientRequest(idToDelete);
    }
}