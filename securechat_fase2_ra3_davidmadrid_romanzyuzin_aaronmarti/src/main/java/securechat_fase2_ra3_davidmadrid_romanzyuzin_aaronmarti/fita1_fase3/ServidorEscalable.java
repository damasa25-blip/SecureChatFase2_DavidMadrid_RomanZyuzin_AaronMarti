package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita1_fase3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorEscalable {
    public static void main(String[] args) {
        int puerto = 12345;
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Servidor Fase 3 iniciado en puerto " + puerto);
            while (true) {
                Socket socket = serverSocket.accept(); // Acepta conexión
                // LOG DE CONEXIÓN
                System.out.println("[SERVER]: Nova connexió física des de " + socket.getInetAddress());
                new ClientHandler(socket).start();    // Delega al handler 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String mensaje) {
        Storage.getUsuarios().values().forEach(out -> out.println(mensaje));
    }
}