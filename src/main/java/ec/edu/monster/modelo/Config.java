package ec.edu.monster.modelo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads configuration from environment variables, falling back to a local ".env" file
 * (the same one used by docker-compose). Keeps secrets out of the source code.
 */
public final class Config {

    private static final Map<String, String> ENV_FILE = cargarArchivoEnv();

    private Config() {
    }

    /** Returns the value of the variable, or {@code defecto} when it is not defined. */
    public static String get(String clave, String defecto) {
        String valor = System.getenv(clave);
        if (valor == null || valor.isBlank()) {
            valor = ENV_FILE.get(clave);
        }
        return (valor == null || valor.isBlank()) ? defecto : valor;
    }

    private static Map<String, String> cargarArchivoEnv() {
        Map<String, String> valores = new HashMap<>();
        Path archivo = Path.of(".env");
        if (!Files.isRegularFile(archivo)) {
            return valores;
        }
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                linea = linea.trim();
                int igual = linea.indexOf('=');
                if (linea.isEmpty() || linea.startsWith("#") || igual < 1) {
                    continue;
                }
                String valor = linea.substring(igual + 1).trim();
                if (valor.length() >= 2 && (valor.startsWith("\"") && valor.endsWith("\"")
                        || valor.startsWith("'") && valor.endsWith("'"))) {
                    valor = valor.substring(1, valor.length() - 1);
                }
                valores.put(linea.substring(0, igual).trim(), valor);
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo .env: " + e.getMessage());
        }
        return valores;
    }
}
