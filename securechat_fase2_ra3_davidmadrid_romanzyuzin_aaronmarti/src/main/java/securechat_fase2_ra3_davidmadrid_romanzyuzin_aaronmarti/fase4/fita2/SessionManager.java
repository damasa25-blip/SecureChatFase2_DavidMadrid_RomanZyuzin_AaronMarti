package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * FASE 4 - FITA 2
 * Gestor de sesiones con validaciones robustas
 */
public class SessionManager {
    private static final long HEARTBEAT_INTERVAL = 5000;
    private static final long HEARTBEAT_TIMEOUT = 15000;
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Validaciones
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MIN_USERNAME_LENGTH = 3;

    private Map<String, UserSession> sessions;
    private Map<String, String> usernameToSessionId;
    private ScheduledExecutorService heartbeatExecutor;

    public SessionManager() {
        this.sessions = new ConcurrentHashMap<>();
        this.usernameToSessionId = new ConcurrentHashMap<>();
    }

    /**
     * Valida un nombre de usuario
     * @return null si es válido, mensaje de error si no lo es
     */
    public String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "ERROR: El nombre de usuario no puede estar vacío";
        }
        
        username = username.trim();
        
        if (username.length() < MIN_USERNAME_LENGTH) {
            return "ERROR: El nombre debe tener al menos " + MIN_USERNAME_LENGTH + " caracteres";
        }
        
        if (username.length() > MAX_USERNAME_LENGTH) {
            return "ERROR: El nombre no puede superar " + MAX_USERNAME_LENGTH + " caracteres";
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "ERROR: El nombre solo puede contener letras, números y guiones bajos";
        }
        
        return null; // Válido
    }

    public synchronized UserSession createSession(String username, ClientHandler handler) {
        // Validar antes de crear
        String validationError = validateUsername(username);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }
        
        UserSession session = new UserSession(username, handler);
        
        sessions.put(session.getSessionId(), session);
        usernameToSessionId.put(username, session.getSessionId());
        
        return session;
    }

    public synchronized void removeSession(String sessionId) {
        UserSession session = sessions.remove(sessionId);
        if (session != null && session.getUsername() != null) {
            usernameToSessionId.remove(session.getUsername());
        }
    }

    public boolean usernameExists(String username) {
        return usernameToSessionId.containsKey(username);
    }

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
                String secure = session.isKeyExchanged() ? "🔒" : "⚠️";
                
                list.append(String.format("  %s %s %d. %-15s [%s] - %s\n", 
                    status, secure, count++, username, session.getSessionId(), loginTime));
            }
        }
        
        return list.toString();
    }

    public void broadcastMessage(String message, String excludeSessionId) {
        if (message == null || message.trim().isEmpty()) {
            return; // No enviar mensajes vacíos
        }
        
        sessions.values().parallelStream().forEach(session -> {
            if (excludeSessionId == null || !session.getSessionId().equals(excludeSessionId)) {
                ClientHandler handler = session.getHandler();
                if (handler != null && session.isActive()) {
                    handler.sendMessage(message);
                }
            }
        });
    }

    public int getSessionCount() {
        return sessions.size();
    }

    public void startHeartbeatMonitor() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(
            this::checkInactiveSessions, 
            HEARTBEAT_INTERVAL, 
            HEARTBEAT_INTERVAL, 
            TimeUnit.MILLISECONDS
        );
    }

    public void stopHeartbeatMonitor() {
        if (heartbeatExecutor != null && !heartbeatExecutor.isShutdown()) {
            heartbeatExecutor.shutdown();
        }
    }

    private void checkInactiveSessions() {
        for (UserSession session : sessions.values()) {
            if (session.isInactive(HEARTBEAT_TIMEOUT)) {
                session.setActive(false);
                ClientHandler handler = session.getHandler();
                if (handler != null) {
                    handler.sendMessage("ERROR: Desconectado por inactividad");
                    handler.disconnect();
                }
            }
        }
    }
}
