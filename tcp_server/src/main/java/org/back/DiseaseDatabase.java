package org.back;

import org.back.model.Disease;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DiseaseDatabase {
    // Usamos un HashMap para un acceso rápido a las enfermedades por su ID.
    private final Map<String, Disease> diseases = new HashMap<>();
    private final String catalogPath = "C:\\Users\\thoma\\Desktop\\Universidad\\2025-03\\Backend\\Proyecto1\\Secure-Genomic-SSL-TCP\\Secure-Genomic-SSL-TCP\\tcp_server\\database\\diseases\\disease_catalog.csv";
    private final String fastaFolderPath = "C:\\Users\\thoma\\Desktop\\Universidad\\2025-03\\Backend\\Proyecto1\\Secure-Genomic-SSL-TCP\\Secure-Genomic-SSL-TCP\\tcp_server\\database\\diseases\\";

    /**
     * Carga toda la base de datos de enfermedades (catálogo y secuencias FASTA) en memoria.
     */
    public void load() throws IOException {
        System.out.println("Cargando base de datos de enfermedades...");
        loadCatalog();
        loadFastaSequences();
        System.out.println(diseases.size() + " enfermedades cargadas exitosamente.");
    }

    /**
     * Lee el archivo de catálogo y llena el mapa inicial de enfermedades.
     */
    private void loadCatalog() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(catalogPath))) {
            String line;
            // Omitimos la primera línea (el encabezado)
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Disease disease = new Disease();
                disease.setDisease_id(values[0]);
                disease.setName(values[1]);
                disease.setSeverity(Integer.parseInt(values[2]));
                diseases.put(disease.getDisease_id(), disease);
            }
        }
    }

    /**
     * Itera sobre las enfermedades ya cargadas y lee sus respectivos archivos FASTA
     * para añadir la secuencia de ADN a cada una.
     */
    private void loadFastaSequences() throws IOException {
        for (Disease disease : diseases.values()) {
            String fastaPath = fastaFolderPath + disease.getDisease_id() + ".fasta";
            // Leemos todas las líneas, omitimos la primera ('>...') y unimos el resto.
            String sequence = Files.lines(Paths.get(fastaPath))
                    .skip(1)
                    .collect(Collectors.joining());
            disease.setSequence(sequence);
        }
    }

    /**
     * Permite que otras partes del programa accedan a la base de datos cargada.
     * @return El mapa de enfermedades.
     */
    public Map<String, Disease> getDiseases() {
        return diseases;
    }
}