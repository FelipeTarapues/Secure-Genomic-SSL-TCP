package org.back;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Menú en consola separado del cliente.
 */
public class Menu {
    private final TCPClient client;
    private final Scanner scanner = new Scanner(System.in);
    private final alertas_loggin alertsLogger = new alertas_loggin();

    public Menu(String host, int port) {
        this.client = new TCPClient(host, port);
    }

    public void show() {
        int option = -1;
        while (option != 0) {
            System.out.println("\n=== MENÚ CLIENTE ===");
            System.out.println("1. Crear paciente");
            System.out.println("2. Consultar paciente");
            System.out.println("3. Actualizar paciente (notas y opcional FASTA)");
            System.out.println("4. Eliminar paciente (lógico)");
            System.out.println("5. Ver alertas locales");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            try {
                option = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) { option = -1; }

            switch (option) {
                case 1 -> handleCreate();
                case 2 -> handleRetrieve();
                case 3 -> handleUpdate();
                case 4 -> handleDelete();
                case 5 -> handleViewAlerts();
                case 0 -> System.out.println("Adiós 👋");
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void handleCreate() {
        try {
            System.out.print("Nombre completo: ");
            String name = scanner.nextLine();
            System.out.print("Documento ID: ");
            String doc = scanner.nextLine();
            System.out.print("Edad: ");
            int age = Integer.parseInt(scanner.nextLine());
            System.out.print("Sexo (M/F): ");
            String sex = scanner.nextLine();
            System.out.print("Correo: ");
            String email = scanner.nextLine();
            System.out.print("Notas clínicas: ");
            String notes = scanner.nextLine();
            System.out.print("Ruta archivo FASTA: ");
            String fasta = scanner.nextLine();

            Paciente p = new Paciente(name, doc, age, sex, email, notes, fasta);
            resultado_validaciones vr = Validaciones.validate(p);
            if (vr.hasErrors()) {
                System.out.println("Errores de validación:");
                vr.getErrors().forEach(err -> System.out.println("- " + err));
                return;
            }

            client.createPatient(p, alertsLogger);

        } catch (Exception e) {
            System.out.println("Error en handleCreate: " + e.getMessage());
        }
    }

    private void handleRetrieve() {
        System.out.print("ID del paciente (ej: P0001): ");
        String id = scanner.nextLine().trim();
        if (id.isBlank()) {
            System.out.println("ID vacío.");
            return;
        }
        client.getPatient(id);
    }

    private void handleUpdate() {
        try {
            System.out.print("ID del paciente a actualizar: ");
            String id = scanner.nextLine().trim();
            if (id.isBlank()) { System.out.println("ID vacío."); return; }
            System.out.print("Nuevas notas clínicas: ");
            String notes = scanner.nextLine();
            System.out.print("¿Deseas reemplazar el FASTA? (s/N): ");
            String opt = scanner.nextLine().trim();
            String newFasta = null;
            if (opt.equalsIgnoreCase("s") || opt.equalsIgnoreCase("y")) {
                System.out.print("Ruta nuevo FASTA: ");
                newFasta = scanner.nextLine().trim();
                if (!Files.exists(Paths.get(newFasta))) {
                    System.out.println("Archivo FASTA no encontrado: " + newFasta);
                    return;
                }
            }
            client.updatePatient(id, notes, newFasta, alertsLogger);
        } catch (Exception e) {
            System.out.println("Error en handleUpdate: " + e.getMessage());
        }
    }

    private void handleDelete() {
        System.out.print("ID del paciente a eliminar: ");
        String id = scanner.nextLine().trim();
        if (id.isBlank()) { System.out.println("ID vacío."); return; }
        client.deletePatient(id);
    }

    private void handleViewAlerts() {
        List<String> lines = alertsLogger.readAll();
        if (lines.isEmpty()) {
            System.out.println("No hay alertas locales guardadas.");
        } else {
            System.out.println("Alertas locales (mostrando últimas 200 líneas si hay muchas):");
            int start = Math.max(0, lines.size() - 200);
            for (int i = start; i < lines.size(); i++) System.out.println(lines.get(i));
        }
    }
}
