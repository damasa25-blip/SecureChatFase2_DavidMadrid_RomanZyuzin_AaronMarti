package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FITA 2 - Gestión de Sesiones y Disponibilidad
 * Clase Server - Punto de entrada del servidor con pool de hilos
 * Responsabilidad: Aceptar conexiones y mantener disponibilidad bajo carga
 */
public class Server {
    private static final int PORT = 12346;
    private static final int MAX_THREADS = 50; // Máximo de hilos para clientes
    
    private ServerSocket serverSocket;
    private SessionManager sessionManager;
    private ExecutorService threadPool;
    private boolean running;

    public Server() {
        this.sessionManager = new SessionManager();
        this.threadPool = Executors.newFixedThreadPool(MAX_THREADS);
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
            System.out.println("║    SecureChat Server 1.0 - FITA 2         ║");
            System.out.println("║    Gestión de Sesiones y Disponibilidad   ║");
            System.out.println("╚════════════════════════════════════════════╝");
            System.out.println("Servidor iniciado en puerto " + PORT);
            System.out.println("Pool de hilos: " + MAX_THREADS + " threads");
            System.out.println("Heartbeat activado: 5s intervalo, 15s timeout");
            System.out.println("Esperando conexiones...\n");

            // Iniciar monitor de sesiones
            sessionManager.startHeartbeatMonitor();

            acceptConnections();
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    /**
     * Acepta conexiones de clientes usando un pool de hilos
     * Esto garantiza disponibilidad incluso con muchos clientes activos
     */
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión desde: " + clientSocket.getInetAddress());
                
                // Usar el pool de hilos para manejar el cliente
                ClientHandler handler = new ClientHandler(clientSocket, sessionManager);
                threadPool.execute(handler);
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error al aceptar conexión: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Detiene el servidor y libera recursos
     */
    public void stop() {
        running = false;
        
        try {
            // Detener monitor de heartbeat
            sessionManager.stopHeartbeatMonitor();
            
            // Cerrar pool de hilos
            threadPool.shutdown();
            
            // Cerrar socket del servidor
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            System.out.println("Servidor detenido correctamente");
        } catch (IOException e) {
            System.err.println("Error al detener el servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        
        // Hook para detener el servidor con Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\nDeteniendo servidor...");
            server.stop();
        }));
        
        server.start();
    }
}
