package ec.edu.monster.controlador;

import com.google.gson.*;
import ec.edu.monster.modelo.*;
import java.util.List;
import java.util.Map;

public class PaisController {
    private PaisDAO dao = new PaisDAO();

    /**
     * Resultado de una importación: cuántos países entregó la API, cuántos se guardaron
     * y cuántos ya existían en la base (y por eso se omitieron).
     */
    public record ResultadoImportacion(int recibidos, int guardados, int existentes) {
        ResultadoImportacion sumar(ResultadoImportacion otro) {
            return new ResultadoImportacion(recibidos + otro.recibidos,
                    guardados + otro.guardados, existentes + otro.existentes);
        }
    }

    /**
     * Importa TODOS los países. Como el plan gratuito limita a 100 por llamada,
     * se recorre la lista por páginas usando meta.more.
     */
    public ResultadoImportacion importarPaises(){
        ResultadoImportacion total = new ResultadoImportacion(0, 0, 0);
        int offset = 0;
        boolean hayMas = true;
        try{
            while (hayMas) {
                String json = ConsumoAPI.obtenerDatosPagina(ConsumoAPI.MAX_LIMIT, offset);
                if (json == null || json.isEmpty()) break;

                JsonElement raiz = JsonParser.parseString(json);
                if (!raiz.isJsonObject()) break;
                JsonObject data = raiz.getAsJsonObject().getAsJsonObject("data");
                if (data == null) break;

                JsonArray objects = data.has("objects") && data.get("objects").isJsonArray()
                        ? data.getAsJsonArray("objects") : new JsonArray();
                if (objects.size() == 0) break;

                total = total.sumar(procesarYGuardarJson(objects));

                JsonObject meta = data.has("meta") ? data.getAsJsonObject("meta") : null;
                hayMas = meta != null && meta.has("more") && meta.get("more").getAsBoolean();
                offset += ConsumoAPI.MAX_LIMIT;
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return total;
    }

    public boolean importarPaisPorNombre(String nombreBusqueda) {
        try {
            String json = ConsumoAPI.obtenerDatosPorNombre(nombreBusqueda);
            JsonArray array = extraerObjects(json);
            return procesarYGuardarJson(array).guardados() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Resultado de una búsqueda: los países encontrados y de dónde se obtuvieron. */
    public record ResultadoBusqueda(List<Pais> paises, boolean desdeApi) {}

    /**
     * Busca primero en MySQL; si no hay coincidencias consulta la API,
     * guarda lo encontrado en MySQL y vuelve a buscar en la base de datos.
     */
    public ResultadoBusqueda buscarPais(String nombre) {
        List<Pais> encontrados = dao.buscarPorNombre(nombre);
        if (!encontrados.isEmpty()) {
            return new ResultadoBusqueda(encontrados, false);
        }
        importarPaisPorNombre(nombre);
        return new ResultadoBusqueda(dao.buscarPorNombre(nombre), true);
    }

    /**
     * En la API v5 los países vienen dentro de { "data": { "objects": [ ... ] } }.
     * Devuelve ese array, o un array vacío si la respuesta no es válida.
     */
    private JsonArray extraerObjects(String json) {
        if (json == null || json.isEmpty()) return new JsonArray();
        JsonElement raiz = JsonParser.parseString(json);
        if (!raiz.isJsonObject()) return new JsonArray();
        JsonObject root = raiz.getAsJsonObject();
        if (!root.has("data") || !root.get("data").isJsonObject()) return new JsonArray();
        JsonObject data = root.getAsJsonObject("data");
        if (!data.has("objects") || !data.get("objects").isJsonArray()) return new JsonArray();
        return data.getAsJsonArray("objects");
    }

    private ResultadoImportacion procesarYGuardarJson(JsonArray array) {
        int guardados = 0;
        int existentes = 0;
        for(JsonElement e : array){
            JsonObject obj = e.getAsJsonObject();

            String nombre = obj.getAsJsonObject("names").get("common").getAsString();
            if (dao.existePorNombre(nombre)) { existentes++; continue; } // evita duplicados
            String region = obj.has("region") && !obj.get("region").isJsonNull()
                    ? obj.get("region").getAsString() : "N/A";
            long poblacion = obj.has("population") && !obj.get("population").isJsonNull()
                    ? obj.get("population").getAsLong() : 0;

            String capital = "N/A";
            if (obj.has("capitals") && obj.get("capitals").isJsonArray()) {
                JsonArray caps = obj.getAsJsonArray("capitals");
                if (caps.size() > 0) {
                    capital = caps.get(0).getAsJsonObject().get("name").getAsString();
                }
            }

            String moneda = "N/A";
            if (obj.has("currencies") && obj.get("currencies").isJsonArray()) {
                JsonArray currencies = obj.getAsJsonArray("currencies");
                if (currencies.size() > 0) {
                    moneda = currencies.get(0).getAsJsonObject().get("name").getAsString();
                }
            }

            Pais pais = new Pais(nombre, capital, region, poblacion, moneda);
            if (dao.guardar(pais)) guardados++;
        }
        return new ResultadoImportacion(array.size(), guardados, existentes);
    }

    public List<Pais> listarPaises() { return dao.listar(); }
    public List<Pais> filtrarPaisesPorRegion(String r) { return dao.buscarPorRegion(r); }
    public List<Pais> buscarEnBd(String nombre) { return dao.buscarPorNombre(nombre); }
    public boolean crearPais(Pais p) { return dao.guardar(p); }
    public boolean existePais(String nombre) { return dao.existePorNombre(nombre); }
    public boolean modificarPais(Pais p) { return dao.actualizar(p); }
    public boolean borrarPais(int id) { return dao.eliminar(id); }
    public boolean vaciarPaises() { return dao.vaciar(); }
    
    public String obtenerPaisMasPoblado() { return dao.obtenerMasPoblado(); }
    public Map<String, Integer> obtenerConteoPorRegion() { return dao.contarPaisesPorRegion(); }
}