package org.back;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Guarda alertas recibidas del servidor en client_alerts.log
 * y permite leerlas desde el menú.
 */
public class alertas_loggin {
    private final Path file = Paths.get("client_alerts.log");
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public synchronized void append(String alertLine) {
        try {
            String line = LocalDateTime.now().format(fmt) + " - " + alertLine + "\n";
            Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error guardando alerta: " + e.getMessage());
        }
    }

    public synchronized List<String> readAll() {
        try {
            if (!Files.exists(file)) return List.of();
            return Files.readAllLines(file);
        } catch (IOException e) {
            System.err.println("Error leyendo alertas: " + e.getMessage());
            return List.of();
        }
    }
}
