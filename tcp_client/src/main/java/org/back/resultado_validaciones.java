package org.back;

import java.util.ArrayList;
import java.util.List;

/**
 * Validacion de errores acumulados.
 */
public class resultado_validaciones {
    private final List<String> errors = new ArrayList<>();

    public void addError(String e) { errors.add(e); }
    public boolean hasErrors() { return !errors.isEmpty(); }
    public List<String> getErrors() { return errors; }
}
