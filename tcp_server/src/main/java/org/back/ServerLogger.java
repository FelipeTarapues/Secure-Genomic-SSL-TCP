package org.back;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerLogger {
    // Define la ruta y el nombre del archivo donde se guardarán los registros.
    private static final String LOG_FILE = "logs/server.log";
    // Un formato estándar para la fecha y hora.
    private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Escribe un mensaje en el archivo de log de forma sincronizada.
     * 'synchronized' es CRUCIAL aquí para evitar que múltiples hilos
     * intenten escribir en el archivo al mismo tiempo, lo que podría corromperlo.
     * @param message El mensaje que se quiere registrar.
     */
    public static synchronized void log(String message) {
        try {
            // Asegurarse de que el directorio 'logs' exista.
            Files.createDirectories(Paths.get("logs"));

            // Usamos try-with-resources para que el archivo se cierre solo.
            // El 'true' en FileWriter significa que añadiremos al final del archivo (append).
            try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                String timestamp = LocalDateTime.now().format(dtf);
                out.println(timestamp + " - " + message);
            }
        } catch (IOException e) {
            // Si el logger falla, imprimimos el error en la consola para no perderlo.
            System.err.println("Error: No se pudo escribir en el archivo de log.");
            e.printStackTrace();
        }
    }
}