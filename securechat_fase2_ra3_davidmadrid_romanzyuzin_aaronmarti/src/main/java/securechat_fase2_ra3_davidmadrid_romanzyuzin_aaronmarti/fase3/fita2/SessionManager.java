package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * FITA 2 - Gestión de Sesiones
 * Clase SessionManager - Gestor central de sesiones
 * Responsabilidades:
 * - Crear y eliminar sesiones
 * - Mantener tabla de usuarios conectados (thread-safe)
 * - Implementar mecanismo de heartbeat
 * - Detectar y eliminar clientes inactivos
 */
public class SessionManager {
    private static final long HEARTBEAT_INTERVAL = 5000; // 5 segundos
    private static final long HEARTBEAT_TIMEOUT = 15000; // 15 segundos
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");

    // Tabla de sesiones activas (thread-safe)
    private Map<String, UserSession> sessions;
    
    // Índice por nombre de usuario para búsquedas rápidas
    private Map<String, String> usernameToSessionId;
    
    // Executor para el monitor de heartbeat
    private ScheduledExecutorService heartbeatExecutor;

    public SessionManager() {
        this.sessions = new ConcurrentHashMap<>();
        this.usernameToSessionId = new ConcurrentHashMap<>();
    }

    /**
     * Crea una nueva sesión para un usuario
     */
    public synchronized UserSession createSession(String username, ClientHandler handler) {
        UserSession session = new UserSession(username, handler);
        
        sessions.put(session.getSessionId(), session);
        usernameToSessionId.put(username, session.getSessionId());
        
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        System.out.println("[" + timestamp + "] Nueva sesión creada: " + 
            username + " [" + session.getSessionId() + "] " +
            "(Total: " + sessions.size() + ")");
        
        return session;
    }

    /**
     * Elimina una sesión
     */
    public synchronized void removeSession(String sessionId) {
        UserSession session = sessions.remove(sessionId);
        
        if (session != null && session.getUsername() != null) {
            usernameToSessionId.remove(session.getUsername());
            
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            System.out.println("[" + timestamp + "] Sesión eliminada: " + 
                session.getUsername() + " [" + sessionId + "] " +
                "(Total: " + sessions.size() + ")");
        }
    }

    /**
     * Verifica si un nombre de usuario ya existe
     */
    public boolean usernameExists(String username) {
        return usernameToSessionId.containsKey(username);
    }

    /**
     * Obtiene la lista de usuarios conectados
     */
    public String getUserList() {
        if (sessions.isEmpty()) {
            return "  (No hay usuarios conectados)";
        }

        StringBuilder list = new StringBuilder();
        int count = 1;
        
        for (UserSession session : sessions.values()) {
            String username = session.getUsername();
            if (username != null) {
                String loginTime = session.getLoginTime().format(TIME_FORMATTER);
                String status = session.isActive() ? "●" : "○";
                
                list.append(String.format("  %s %d. %-15s [%s] - desde %s\n", 
                    status, count++, username, session.getSessionId(), loginTime));
            }
        }
        
        return list.toString();
    }

    /**
     * Envía un mensaje a todos los usuarios conectados
     * @param message Mensaje a enviar
     * @param excludeSessionId Session ID a excluir (puede ser null)
     */
    public void broadcastMessage(String message, String excludeSessionId) {
        for (UserSession session : sessions.values()) {
            if (excludeSessionId == null || !session.getSessionId().equals(excludeSessionId)) {
                ClientHandler handler = session.getHandler();
                if (handler != null) {
                    handler.sendMessage(message);
                }
            }
        }
    }

    /**
     * Obtiene el número de sesiones activas
     */
    public int getSessionCount() {
        return sessions.size();
    }

    /**
     * Inicia el monitor de heartbeat
     * Verifica periódicamente si hay clientes inactivos
     */
    public void startHeartbeatMonitor() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            checkInactiveSessions();
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
        
        System.out.println("Monitor de heartbeat iniciado");
    }

    /**
     * Detiene el monitor de heartbeat
     */
    public void stopHeartbeatMonitor() {
        if (heartbeatExecutor != null && !heartbeatExecutor.isShutdown()) {
            heartbeatExecutor.shutdown();
            System.out.println("Monitor de heartbeat detenido");
        }
    }

    /**
     * Verifica sesiones inactivas y las desconecta
     */
    private void checkInactiveSessions() {
        for (UserSession session : sessions.values()) {
            if (session.isInactive(HEARTBEAT_TIMEOUT)) {
                String username = session.getUsername();
                String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
                
                System.out.println("[" + timestamp + "] Cliente inactivo detectado: " + 
                    username + " [" + session.getSessionId() + "]");
                
                // Desconectar cliente
                session.setActive(false);
                ClientHandler handler = session.getHandler();
                if (handler != null) {
                    handler.sendMessage("ERROR: Desconectado por inactividad");
                    handler.disconnect();
                }
                
                // Notificar a otros usuarios
                String notification = ">>> " + username + 
                    " se ha desconectado (inactividad)";
                broadcastMessage(notification, session.getSessionId());
            }
        }
    }
}
