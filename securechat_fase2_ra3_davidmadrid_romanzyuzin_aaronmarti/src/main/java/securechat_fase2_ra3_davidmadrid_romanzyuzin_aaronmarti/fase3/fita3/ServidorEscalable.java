package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FITA 3 - Pruebas de Carga y Optimización
 * SecureChat Server 1.0 - Servidor Escalable
 * Versión optimizada con métricas de rendimiento
 */
public class ServidorEscalable {
    private static final int PORT = 12347;
    private static final int MAX_THREADS = 100; // Pool optimizado
    
    private ServerSocket serverSocket;
    private SessionManager sessionManager;
    private ExecutorService threadPool;
    private PerformanceMonitor performanceMonitor;
    private boolean running;
    
    // Contadores para métricas
    private AtomicInteger totalConnections;
    private AtomicInteger activeConnections;
    private AtomicLong totalMessagesProcessed;

    public ServidorEscalable() {
        this.sessionManager = new SessionManager();
        this.threadPool = Executors.newFixedThreadPool(MAX_THREADS);
        this.performanceMonitor = new PerformanceMonitor();
        this.totalConnections = new AtomicInteger(0);
        this.activeConnections = new AtomicInteger(0);
        this.totalMessagesProcessed = new AtomicLong(0);
        this.running = false;
    }

    /**
     * Inicia el servidor escalable
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            
            printBanner();
            
            // Iniciar monitor de sesiones y rendimiento
            sessionManager.startHeartbeatMonitor();
            performanceMonitor.start();

            acceptConnections();
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    /**
     * Imprime el banner del servidor
     */
    private void printBanner() {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║       SecureChat Server 1.0 - SERVIDOR ESCALABLE      ║");
        System.out.println("║              FITA 3 - Optimización Final              ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("Puerto: " + PORT);
        System.out.println("Pool de hilos: " + MAX_THREADS + " threads");
        System.out.println("Heartbeat: 5s intervalo, 15s timeout");
        System.out.println("Monitor de rendimiento: ACTIVO");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("Esperando conexiones...\n");
    }

    /**
     * Acepta conexiones de clientes
     */
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // Incrementar contadores
                totalConnections.incrementAndGet();
                activeConnections.incrementAndGet();
                
                // Registrar conexión
                performanceMonitor.recordConnection();
                
                // Crear y ejecutar handler
                ClientHandler handler = new ClientHandler(
                    clientSocket, 
                    sessionManager, 
                    this
                );
                threadPool.execute(handler);
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error al aceptar conexión: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Registra un mensaje procesado (para métricas)
     */
    public void recordMessage() {
        totalMessagesProcessed.incrementAndGet();
        performanceMonitor.recordMessage();
    }

    /**
     * Registra una desconexión
     */
    public void recordDisconnection() {
        activeConnections.decrementAndGet();
    }

    /**
     * Obtiene métricas del servidor
     */
    public String getMetrics() {
        return String.format(
            "Conexiones totales: %d | Activas: %d | Mensajes: %d | TPS: %.2f",
            totalConnections.get(),
            activeConnections.get(),
            totalMessagesProcessed.get(),
            performanceMonitor.getMessagesPerSecond()
        );
    }

    /**
     * Detiene el servidor
     */
    public void stop() {
        running = false;
        
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("Deteniendo servidor...");
        System.out.println("════════════════════════════════════════════════════════");
        
        try {
            // Detener monitores
            sessionManager.stopHeartbeatMonitor();
            performanceMonitor.stop();
            
            // Mostrar estadísticas finales
            System.out.println("\n" + performanceMonitor.getReport());
            
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
        ServidorEscalable server = new ServidorEscalable();
        
        // Hook para detener el servidor con Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
        }));
        
        server.start();
    }
}
