package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FASE 4 - FITA 2
 * SecureChat Server 2.0 - Servidor con intercambio de claves RSA
 * 
 * Características:
 * - Generación de par de claves RSA al iniciar
 * - Intercambio seguro de claves AES con cada cliente
 * - Validación robusta de entradas
 * - Gestión completa de errores
 */
public class ServidorEscalable {
    private static final int PORT = 12349; // Puerto diferente para FITA 2
    private static final int MAX_THREADS = 100;
    
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
        this.seguretat = new Seguretat();
        this.totalConnections = new AtomicInteger(0);
        this.activeConnections = new AtomicInteger(0);
        this.totalMessagesProcessed = new AtomicLong(0);
        this.running = false;
    }

    /**
     * Obtiene el objeto Seguretat del servidor
     */
    public Seguretat getSeguretat() {
        return seguretat;
    }

    /**
     * Inicia el servidor
     */
    public void start() {
        try {
            // PASO 1: Generar par de claves RSA
            System.out.println("Generando par de claves RSA-2048...");
            seguretat.generateRSAKeyPair();
            System.out.println("✓ Claves RSA generadas correctamente\n");
            
            serverSocket = new ServerSocket(PORT);
            running = true;
            
            printBanner();
            
            // Iniciar monitores
            sessionManager.startHeartbeatMonitor();
            performanceMonitor.start();

            acceptConnections();
            
        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Imprime el banner del servidor
     */
    private void printBanner() {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║       SecureChat Server 2.0 - FASE 4 FITA 2          ║");
        System.out.println("║      RSA Key Exchange + AES/GCM Encryption            ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("Puerto: " + PORT);
        System.out.println("Pool de hilos: " + MAX_THREADS + " threads");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("Cifrado:");
        System.out.println("  • RSA-2048: Intercambio de claves");
        System.out.println("  • AES/GCM-256: Cifrado de mensajes");
        System.out.println("  • SHA-256: Verificación de integridad");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("Seguridad:");
        System.out.println("  ✓ Clave AES única por cliente");
        System.out.println("  ✓ Intercambio seguro con RSA");
        System.out.println("  ✓ Validación de entradas");
        System.out.println("  ✓ Gestión de errores robusta");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("Esperando conexiones...\n");
        
        // Mostrar información de cifrado
        seguretat.printInfo();
        System.out.println();
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
                
                System.out.println("[Servidor] Nueva conexión desde: " + 
                                 clientSocket.getInetAddress());
                
                // Crear y ejecutar handler con clave pública RSA
                String publicKeyBase64 = seguretat.getPublicKeyBase64();
                ClientHandler handler = new ClientHandler(
                    clientSocket, 
                    sessionManager, 
                    this,
                    publicKeyBase64
                );
                threadPool.execute(handler);
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error al aceptar conexión: " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("Error inesperado: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Registra un mensaje procesado
     */
    public void recordMessage() {
        totalMessagesProcessed.incrementAndGet();
        performanceMonitor.recordMessage();
    }

    /**
     * Registra una operación de cifrado
     */
    public void recordEncryption() {
        performanceMonitor.recordEncryption();
    }

    /**
     * Registra una operación de descifrado
     */
    public void recordDecryption() {
        performanceMonitor.recordDecryption();
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
            
            System.out.println("\n✓ Servidor detenido correctamente");
            
        } catch (IOException e) {
            System.err.println("Error al detener el servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("       Iniciando SecureChat Server 2.0 (FITA 2)       ");
        System.out.println("════════════════════════════════════════════════════════\n");
        
        ServidorEscalable server = new ServidorEscalable();
        
        // Hook para detener el servidor con Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
        }));
        
        server.start();
    }
}
