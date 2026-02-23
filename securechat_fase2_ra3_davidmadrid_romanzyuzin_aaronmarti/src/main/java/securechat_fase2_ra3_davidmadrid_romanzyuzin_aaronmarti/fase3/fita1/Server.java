package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * FITA 1 - Refactorización por Capas
 * Clase Server - Punto de entrada del servidor
 * Responsabilidad: Aceptar conexiones y delegar la gestión a ClientHandler
 */
public class Server {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private UserRepository userRepository;
    private boolean running;

    public Server() {
        this.userRepository = new UserRepository();
        this.running = false;
    }

    /**
     * Inicia el servidor y comienza a aceptar conexiones
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("╔════════════════════════════════════════════╗");
            System.out.println("║    SecureChat Server 1.0 - FITA 1         ║");
            System.out.println("║    Arquitectura por Capas                  ║");
            System.out.println("╚════════════════════════════════════════════╝");
            System.out.println("Servidor iniciado en puerto " + PORT);
            System.out.println("Esperando conexiones...\n");

            acceptConnections();
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    /**
     * Acepta conexiones de clientes y crea un handler para cada uno
     */
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión desde: " + clientSocket.getInetAddress());
                
                // Delegar la gestión del cliente a ClientHandler
                ClientHandler handler = new ClientHandler(clientSocket, userRepository);
                new Thread(handler).start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error al aceptar conexión: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Detiene el servidor
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("Servidor detenido");
        } catch (IOException e) {
            System.err.println("Error al detener el servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        
        // Hook para detener el servidor con Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nDeteniendo servidor...");
            server.stop();
        }));
        
        server.start();
    }
}
