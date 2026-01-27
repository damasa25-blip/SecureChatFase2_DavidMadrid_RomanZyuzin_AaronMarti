package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita5;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServidor {
// --- Codi ANSI per colors ---
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m"; // Color INFORMATIU
    public static final String ANSI_CYAN = "\u001B[36m";   // Color ENUNCIAT
    public static final String ANSI_GREEN = "\u001B[32m";  // Color RESPUESTA

    // Lista de escritores para enviar mensajes a todos
    public static List<PrintWriter> escritores = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        int port = 12345;
        System.out.println("");
        System.out.println(ANSI_YELLOW + "Servidor Abierto y escuchando..." + ANSI_RESET);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                // 1. Aceptamos al cliente
                Socket socket = serverSocket.accept();
                System.out.println(ANSI_CYAN + "Nuevo cliente conectado desde: " + ANSI_RESET + socket.getInetAddress());

                // 2. Creamos su canal de salida y lo añadimos a la lista
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                escritores.add(out);

                // 3. Lanzamos un hilo para que escuche a este cliente concreto
                new ClientHandler(socket, out).start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    // El BROADCAST: Envía el mensaje a todos los de la lista
    public static void broadcast(String msg) {
        for (PrintWriter writer : escritores) {
            writer.println(msg);
        }
    }
}