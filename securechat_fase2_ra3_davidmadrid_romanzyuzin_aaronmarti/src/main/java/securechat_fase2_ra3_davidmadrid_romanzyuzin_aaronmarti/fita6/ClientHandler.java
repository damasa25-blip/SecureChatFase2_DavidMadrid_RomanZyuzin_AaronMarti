package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler extends Thread {

    // --- Codi ANSI per colors ---
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m"; // Color INFORMATIU
    public static final String ANSI_CYAN = "\u001B[36m";   // Color ENUNCIAT
    public static final String ANSI_GREEN = "\u001B[32m";  // Color RESPUESTA

    private Socket socket;
    private PrintWriter out;
    private String nombreUsuario = null;

    public ClientHandler(Socket socket, PrintWriter out) {
        this.socket = socket;
        this.out = out;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String linea;
            while ((linea = in.readLine()) != null) {
                // Separamos el comando del contenido
                // Ejemplo: "LOGIN|Aaron" -> partes[0]="LOGIN", partes[1]="Aaron"
                String[] partes = linea.split("/", 2);
                String comando = partes[0].toUpperCase();
                String datos = (partes.length > 1) ? partes[1] : "";
                out.println("Recibido comando: " + comando);

                switch (comando) {
                    case "LOGIN":
                        manejarLogin(datos);
                        break;
                    case "MSG":
                        manejarMensaje(datos);
                        break;
                    case "LIST":
                        manejarListar();
                        break;
                    case "QUIT":
                        return; // Sale del bucle y cierra
                    default:
                        out.println("ERROR|Comando no reconocido");
                }
            }
        } catch (IOException e) {
            // Manejar desconexión
        } finally {
            limpiarConexion();
        }
    }

    private void manejarLogin(String nombre) {
    if (nombre.isEmpty() || ChatServidor.mapaClientes.containsKey(nombre)) {
        out.println("ERROR|Nombre no válido o ya ocupado");
    } else {
        this.nombreUsuario = nombre;
        ChatServidor.mapaClientes.put(nombre, out);
        out.println(ANSI_GREEN + "OK|" + ANSI_RESET + "Bienvenido "  + nombre);
        String msg = "se ha unido al chat.";
        ChatServidor.broadcast(nombre, msg);
    }
}

    private void manejarMensaje(String mensaje) {
        
        if (this.nombreUsuario == null) {
            out.println("ERROR|Debes iniciar sesión primero");
            return;
        }
        ChatServidor.broadcast(this.nombreUsuario, mensaje);
    }

    private void manejarListar() {
        if (this.nombreUsuario == null) {
            out.println("ERROR|Debes iniciar sesión primero");
            return;
        }
        StringBuilder lista = new StringBuilder("LIST|");
        for (String nombre : ChatServidor.mapaClientes.keySet()) {
            lista.append(nombre).append(",");
        }
        // Eliminar la última coma
        if (lista.length() > 5) {
            lista.setLength(lista.length() - 1);
        }
        out.println(lista.toString());
    }

    private void limpiarConexion() {
        if (this.nombreUsuario != null) {
            ChatServidor.mapaClientes.remove(this.nombreUsuario);
            ChatServidor.broadcast(this.nombreUsuario, " ha salido del chat.");
        }
        try {
            socket.close();
        } catch (IOException e) {
            // Ignorar
        }
    }
}
