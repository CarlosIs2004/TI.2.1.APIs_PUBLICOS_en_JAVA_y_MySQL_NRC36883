package ec.edu.monster.modelo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ConsumoAPI {

    // ============================================================
    //  API REST Countries v5 (la v3.1 fue DEPRECADA y ya no funciona)
    //  1) Regístrate gratis en: https://restcountries.com/sign-up
    //  2) Copia tu API key (formato rc_live_xxxxxxxx)
    //  3) Guárdala en REST_COUNTRIES_API_KEY (variable de entorno o archivo .env,
    //     ver env.example). Nunca la escribas directamente en el código.
    // ============================================================
    private static final String API_KEY = Config.get("REST_COUNTRIES_API_KEY", "");
    private static final String BASE_URL = "https://api.restcountries.com/countries/v5";

    // El plan gratuito permite un máximo de 100 países por llamada,
    // por eso "Importar Todo" se hace paginando (ver PaisController).
    public static final int MAX_LIMIT = 100;

    /** Trae una página de países (limit máx. 100). */
    public static String obtenerDatosPagina(int limit, int offset) {
        return ejecutarGet(BASE_URL + "?limit=" + limit + "&offset=" + offset);
    }

    /** Búsqueda por nombre (full-text search con el parámetro q). */
    public static String obtenerDatosPorNombre(String nombrePais) {
        String q = URLEncoder.encode(nombrePais.trim(), StandardCharsets.UTF_8);
        return ejecutarGet(BASE_URL + "?q=" + q);
    }

    /** Realiza el GET con autenticación Bearer y devuelve el cuerpo de la respuesta. */
    private static String ejecutarGet(String endpoint) {
        if (API_KEY.isEmpty()) {
            System.out.println("Falta la variable REST_COUNTRIES_API_KEY (ver env.example).");
            return "";
        }
        StringBuilder resultado = new StringBuilder();
        HttpURLConnection con = null;
        try {
            URL url = new URL(endpoint);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + API_KEY);
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("User-Agent", "TallerJavaMVC/1.0");
            con.setConnectTimeout(15000);
            con.setReadTimeout(20000);

            int responseCode = con.getResponseCode();
            // Si es 200 leemos el cuerpo normal; si no, leemos el stream de error
            // para ver el mensaje real de la API (key inválida, límite, etc.).
            InputStream stream = (responseCode == HttpURLConnection.HTTP_OK)
                    ? con.getInputStream()
                    : con.getErrorStream();

            if (stream != null) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String linea;
                    while ((linea = br.readLine()) != null) {
                        resultado.append(linea);
                    }
                }
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Error API. HTTP " + responseCode + " -> " + resultado);
                // Devolvemos vacío para que el controlador lo trate como "sin datos".
                return "";
            }
        } catch (Exception e) {
            System.out.println("Error de conexión con la API: " + e.getMessage());
            e.printStackTrace();
            return "";
        } finally {
            if (con != null) con.disconnect();
        }
        return resultado.toString();
    }
}
