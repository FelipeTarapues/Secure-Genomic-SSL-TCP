package org.back;

public class FastaValidator {
    public static boolean isValid(String content) {
        if (content == null || content.trim().isEmpty()) return false;
        String[] lines = content.trim().split("\\R"); // Divide por saltos de línea
        if (!lines[0].startsWith(">")) return false; // La primera línea debe empezar con >
        for (int i = 1; i < lines.length; i++) {
            // Todas las demás líneas solo deben contener A, C, G, T, N
            if (!lines[i].matches("[ACGTN]+")) {
                return false;
            }
        }
        return true;
    }
}