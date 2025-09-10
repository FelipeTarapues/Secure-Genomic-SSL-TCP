package org.back;

import org.back.model.Disease;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportManager {
    private static final String REPORT_PATH = "C:\\Users\\thoma\\Desktop\\Universidad\\2025-03\\Backend\\Proyecto1\\Secure-Genomic-SSL-TCP\\Secure-Genomic-SSL-TCP\\tcp_server\\reports\\detections_report.csv";

    public static void logDetections(String patientId, List<Disease> diseases) {
        // Usamos try-with-resources y 'true' para añadir al final del archivo (append)
        try (PrintWriter out = new PrintWriter(new FileWriter(REPORT_PATH, true))) {
            for (Disease disease : diseases) {
                String detectionTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                String reportLine = String.join(",",
                        patientId,
                        disease.getDisease_id(),
                        String.valueOf(disease.getSeverity()),
                        detectionTime,
                        "Coincidencia encontrada para " + disease.getName()
                );
                out.println(reportLine);
            }
        } catch (IOException e) {
            System.err.println("Error al escribir en el reporte de detecciones: " + e.getMessage());
            e.printStackTrace();
        }
    }
}