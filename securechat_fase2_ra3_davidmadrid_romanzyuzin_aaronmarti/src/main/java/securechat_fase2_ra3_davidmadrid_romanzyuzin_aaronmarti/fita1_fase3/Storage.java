package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita1_fase3;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    // Estructura sincronizada para gestionar sesiones [cite: 671]
    private static Map<String, PrintWriter> usuariosConectados = new ConcurrentHashMap<>();

    public static void registrarUsuario(String nombre, PrintWriter out) {
        usuariosConectados.put(nombre, out);
    }

    public static void eliminarUsuario(String nombre) {
        usuariosConectados.remove(nombre);
    }

    public static Map<String, PrintWriter> getUsuarios() {
        return usuariosConectados;
    }

    public static boolean existeUsuario(String nombre) {
        return usuariosConectados.containsKey(nombre);
    }
}