package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FASE 4 - FITA 1
 * SecureChat Server 2.0 - Servidor con Cifrado AES/GCM
 * Versión con cifrado simétrico precompartido
 */
public class ServidorEscalable {
    private static final int PORT = 12348; // Puerto diferente para no confundir con Fase 3
    private static final int MAX_THREADS = 100;
    
    // Clave precompartida para FITA 1 (en Base64)
    // En producción, esto debería estar en configuración segura
    private static final String SHARED_KEY = "8Zl5ryWZeRV4mVGhlnde6A2WBpdqXZx+xdIYzJ6X7yg=";
    
    private ServerSocket serverSocket;
    private SessionManager sessionManager;
    private ExecutorService threadPool;
    private PerformanceMonitor performanceMonitor;
    private Seguretat seguretat;
    private boolean running;
    
    // Contadores para métricas
    private AtomicInteger totalConnections;
    private AtomicInteger activeConnections;
    private AtomicLong totalMessagesProcessed;

    public ServidorEscalable() {
        this.sessionManager = new SessionManager();
        this.threadPool = Executors.newFixedThreadPool(MAX_THREADS);
        this.performanceMonitor = new PerformanceMonitor();
        this.seguretat = new Seguretat(SHARED_KEY);
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
        System.out.println("║       SecureChat Server 2.0 - FASE 4 FITA 1          ║");
        System.out.println("║              AES/GCM Cifrado Simétrico                ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("Puerto: " + PORT);
        System.out.println("Pool de hilos: " + MAX_THREADS + " threads");
        System.out.println("Cifrado: AES/GCM-256");
        System.out.println("Clave: Precompartida (FITA 1)");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("🔒 Mensajes MSG viajan cifrados");
        System.out.println("Esperando conexiones...\n");
        
        // Mostrar información de cifrado
        seguretat.printInfo();
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
                
                // Crear y ejecutar handler con seguridad
                ClientHandler handler = new ClientHandler(
                    clientSocket, 
                    sessionManager, 
                    this,
                    seguretat
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
