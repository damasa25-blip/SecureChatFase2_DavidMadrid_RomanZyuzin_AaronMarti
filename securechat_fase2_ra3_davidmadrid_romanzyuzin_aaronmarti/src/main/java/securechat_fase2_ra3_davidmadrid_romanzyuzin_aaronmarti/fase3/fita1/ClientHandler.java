package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * FITA 1 - Refactorización por Capas
 * Clase ClientHandler - Gestión de comunicación con cada cliente
 * Responsabilidad: Manejar la comunicación con un cliente específico (un hilo por cliente)
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private UserRepository userRepository;
    private Protocol protocol;
    private BufferedReader input;
    private PrintWriter output;
    private String username;
    private boolean connected;

    public ClientHandler(Socket socket, UserRepository repository) {
        this.clientSocket = socket;
        this.userRepository = repository;
        this.protocol = new Protocol(repository);
        this.connected = true;
    }

    @Override
    public void run() {
        try {
            // Configurar streams de entrada/salida
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            // Mensaje de bienvenida
            output.println("╔════════════════════════════════════════════╗");
            output.println("║    Bienvenido a SecureChat Server 1.0     ║");
            output.println("║    FITA 1 - Arquitectura por Capas        ║");
            output.println("╚════════════════════════════════════════════╝");
            output.println("Comandos disponibles:");
            output.println("  LOGIN <nombre>  - Iniciar sesión");
            output.println("  MSG <mensaje>   - Enviar mensaje");
            output.println("  LIST           - Ver usuarios conectados");
            output.println("  QUIT           - Desconectar\n");

            // Procesar comandos del cliente
            String message;
            while (connected && (message = input.readLine()) != null) {
                // Delegar el procesamiento al protocolo
                String response = protocol.processCommand(message, this);
                output.println(response);

                // Verificar si el cliente se desconectó
                if (message.trim().toUpperCase().startsWith("QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + 
                (username != null ? username : clientSocket.getInetAddress()));
        } finally {
            cleanup();
        }
    }

    /**
     * Envía un mensaje a este cliente
     */
    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    /**
     * Obtiene el nombre de usuario
     */
    public String getUsername() {
        return username;
    }

    /**
     * Establece el nombre de usuario
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Limpieza de recursos
     */
    private void cleanup() {
        connected = false;
        
        // Eliminar usuario del repositorio
        if (username != null) {
            userRepository.removeUser(username);
            System.out.println("Usuario desconectado: " + username);
        }

        // Cerrar recursos
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar recursos: " + e.getMessage());
        }
    }
}
