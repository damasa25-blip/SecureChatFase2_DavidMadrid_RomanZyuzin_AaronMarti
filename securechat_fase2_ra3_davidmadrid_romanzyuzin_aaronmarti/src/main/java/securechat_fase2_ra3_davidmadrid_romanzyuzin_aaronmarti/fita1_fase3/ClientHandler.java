package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita1_fase3;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private String nombreUsuario;

    public ClientHandler(Socket socket) { this.socket = socket; }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);
            String linea;
            while ((linea = in.readLine()) != null) {
                // Delegamos la interpretación al Protocolo
                String respuesta = Protocol.procesarComando(linea, nombreUsuario);
                
                if (respuesta.startsWith("OK_LOGIN/")) {
                    nombreUsuario = respuesta.split("/")[1];
                    Storage.registrarUsuario(nombreUsuario, out);
                    out.println("Bienvenido " + nombreUsuario);
                } else if (respuesta.startsWith("BROADCAST/")) {
                    ServidorEscalable.broadcast(respuesta.split("/", 2)[1]);
                } else if (respuesta.equals("BYE")) {
                    break;
                } else {
                    out.println(respuesta);
                }
            }
        } catch (IOException e) { /* Manejar error */ }
        finally {
            if (nombreUsuario != null) Storage.eliminarUsuario(nombreUsuario);
            try { socket.close(); } catch (IOException e) {}
        }
    }
}