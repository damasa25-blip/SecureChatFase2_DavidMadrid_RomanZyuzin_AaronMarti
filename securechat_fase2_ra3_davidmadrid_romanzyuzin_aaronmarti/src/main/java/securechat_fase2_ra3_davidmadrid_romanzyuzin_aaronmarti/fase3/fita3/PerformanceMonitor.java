package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita3;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FITA 3 - Monitor de Rendimiento
 * Monitoriza y reporta métricas de rendimiento del servidor
 */
public class PerformanceMonitor {
    private static final long REPORT_INTERVAL = 30000; // 30 segundos
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

    private LocalDateTime startTime;
    private AtomicLong totalConnections;
    private AtomicLong totalMessages;
    private AtomicLong messagesLastSecond;
    private ScheduledExecutorService reportExecutor;
    
    // Para calcular mensajes por segundo
    private long lastMessageCount;
    private long lastCheckTime;

    public PerformanceMonitor() {
        this.startTime = LocalDateTime.now();
        this.totalConnections = new AtomicLong(0);
        this.totalMessages = new AtomicLong(0);
        this.messagesLastSecond = new AtomicLong(0);
        this.lastMessageCount = 0;
        this.lastCheckTime = System.currentTimeMillis();
    }

    /**
     * Inicia el monitor de rendimiento
     */
    public void start() {
        reportExecutor = Executors.newSingleThreadScheduledExecutor();
        
        // Reportar cada 30 segundos
        reportExecutor.scheduleAtFixedRate(() -> {
            printReport();
        }, REPORT_INTERVAL, REPORT_INTERVAL, TimeUnit.MILLISECONDS);
        
        System.out.println("Monitor de rendimiento iniciado\n");
    }

    /**
     * Detiene el monitor
     */
    public void stop() {
        if (reportExecutor != null && !reportExecutor.isShutdown()) {
            reportExecutor.shutdown();
        }
    }

    /**
     * Registra una nueva conexión
     */
    public void recordConnection() {
        totalConnections.incrementAndGet();
    }

    /**
     * Registra un mensaje procesado
     */
    public void recordMessage() {
        totalMessages.incrementAndGet();
    }

    /**
     * Calcula mensajes por segundo
     */
    public double getMessagesPerSecond() {
        long currentTime = System.currentTimeMillis();
        long currentMessages = totalMessages.get();
        
        long timeDiff = currentTime - lastCheckTime;
        long messageDiff = currentMessages - lastMessageCount;
        
        if (timeDiff > 0) {
            double mps = (messageDiff * 1000.0) / timeDiff;
            
            // Actualizar para la próxima medición
            lastCheckTime = currentTime;
            lastMessageCount = currentMessages;
            
            return mps;
        }
        
        return 0.0;
    }

    /**
     * Imprime un reporte de rendimiento
     */
    private void printReport() {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        double mps = getMessagesPerSecond();
        
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("  REPORTE DE RENDIMIENTO [" + timestamp + "]");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("  Conexiones totales: " + totalConnections.get());
        System.out.println("  Mensajes procesados: " + totalMessages.get());
        System.out.println("  Mensajes/segundo: " + DECIMAL_FORMAT.format(mps));
        System.out.println("  Uptime: " + getUptime());
        System.out.println("════════════════════════════════════════════════════════\n");
    }

    /**
     * Genera un reporte completo
     */
    public String getReport() {
        double mps = getMessagesPerSecond();
        
        return "════════════════════════════════════════════════════════\n" +
               "         REPORTE FINAL DE RENDIMIENTO                  \n" +
               "════════════════════════════════════════════════════════\n" +
               "  Inicio: " + startTime.format(TIME_FORMATTER) + "\n" +
               "  Fin: " + LocalDateTime.now().format(TIME_FORMATTER) + "\n" +
               "  Uptime: " + getUptime() + "\n" +
               "  \n" +
               "  Conexiones totales: " + totalConnections.get() + "\n" +
               "  Mensajes procesados: " + totalMessages.get() + "\n" +
               "  Mensajes/segundo (promedio): " + DECIMAL_FORMAT.format(mps) + "\n" +
               "════════════════════════════════════════════════════════";
    }

    /**
     * Calcula el uptime del servidor
     */
    private String getUptime() {
        long uptimeMs = System.currentTimeMillis() - 
            startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMs);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMs) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMs) % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
