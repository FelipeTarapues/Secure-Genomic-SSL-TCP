package org.back;

import org.back.model.Disease;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenomeComparator {

    /**
     * Compara el genoma de un paciente contra una base de datos de enfermedades.
     * @param patientGenome La secuencia de ADN del paciente.
     * @param diseaseDb El mapa de enfermedades cargado en el servidor.
     * @return Una lista de las enfermedades que tuvieron coincidencia.
     */
    public List<Disease> findMatches(String patientGenome, Map<String, Disease> diseaseDb) {
        List<Disease> matches = new ArrayList<>();
        // Limpiamos el genoma del paciente por si tiene saltos de línea.
        String cleanPatientGenome = patientGenome.replace("\n", "").replace("\r", "");

        // Iteramos sobre cada enfermedad en la base de datos
        for (Disease disease : diseaseDb.values()) {
            if (cleanPatientGenome.contains(disease.getSequence())) {
                System.out.println("COINCIDENCIA ENCONTRADA: Paciente vs " + disease.getName());
                matches.add(disease);
            }
        }
        return matches;
    }
}