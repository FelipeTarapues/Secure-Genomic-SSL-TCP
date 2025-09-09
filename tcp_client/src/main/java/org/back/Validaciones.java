package org.back;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validaciones básicas que debe hacer el cliente antes de enviar.
 * - email: debe terminar en .com
 * - sexo: M o F
 * - edad: 1..120
 * - fasta: existencia, primera línea '>' y secuencias A,C,G,T,N
 */
public class Validaciones {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com$");
    private static final Pattern FASTA_SEQ_LINE = Pattern.compile("^[ACGTN]+$");

    public static resultado_validaciones validate(Paciente p) {
        resultado_validaciones result = new resultado_validaciones();

        if (p.getFullName() == null || p.getFullName().isBlank()) result.addError("Nombre vacío.");
        if (p.getDocumentId() == null || p.getDocumentId().isBlank()) result.addError("Documento vacío.");
        if (p.getAge() <= 0 || p.getAge() > 120) result.addError("Edad inválida (1-120).");
        if (p.getSex() == null || !(p.getSex().equalsIgnoreCase("M") || p.getSex().equalsIgnoreCase("F")))
            result.addError("Sexo inválido (debe ser 'M' o 'F').");
        if (p.getContactEmail() == null || !EMAIL_PATTERN.matcher(p.getContactEmail()).matches())
            result.addError("Correo inválido (debe terminar en .com).");

        if (p.getFastaPath() == null || p.getFastaPath().isBlank()) {
            result.addError("Ruta FASTA no especificada.");
            return result;
        }
        Path fasta = Paths.get(p.getFastaPath());
        if (!Files.exists(fasta)) {
            result.addError("Archivo FASTA no encontrado: " + p.getFastaPath());
            return result;
        }

        try {
            List<String> lines = Files.readAllLines(fasta);
            if (lines.isEmpty()) {
                result.addError("FASTA vacío.");
                return result;
            }
            if (!lines.get(0).startsWith(">")) {
                result.addError("Primera línea del FASTA debe comenzar con '>' y un identificador.");
            }
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (!line.isEmpty() && !FASTA_SEQ_LINE.matcher(line).matches()) {
                    result.addError("Línea " + (i + 1) + " del FASTA contiene caracteres inválidos. Solo A,C,G,T,N permitidos.");
                    break;
                }
            }
        } catch (Exception e) {
            result.addError("Error leyendo FASTA: " + e.getMessage());
        }

        return result;
    }
}
