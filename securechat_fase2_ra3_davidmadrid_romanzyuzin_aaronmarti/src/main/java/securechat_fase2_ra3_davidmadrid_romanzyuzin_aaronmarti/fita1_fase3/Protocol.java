package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita1_fase3;

public class Protocol {
    // Procesa los comandos LOGIN, MSG, LIST, QUIT [cite: 644]
    public static String procesarComando(String linea, String nombreActual) {
        String[] partes = linea.split("/", 2);
        String comando = partes[0].toUpperCase();
        String datos = (partes.length > 1) ? partes[1] : "";

        switch (comando) {
            case "LOGIN":
                if (datos.isEmpty() || Storage.existeUsuario(datos)) return "ERROR/Nombre no disponible";
                return "OK_LOGIN/" + datos;
            case "MSG":
                if (nombreActual == null) return "ERROR/Debes hacer login primero";
                return "BROADCAST/" + nombreActual + ": " + datos;
            case "LIST":
                return "LIST_RESPONSE/" + String.join(",", Storage.getUsuarios().keySet());
            case "QUIT":
                return "BYE";
            default:
                return "ERROR/Comando desconocido";
        }
    }
}