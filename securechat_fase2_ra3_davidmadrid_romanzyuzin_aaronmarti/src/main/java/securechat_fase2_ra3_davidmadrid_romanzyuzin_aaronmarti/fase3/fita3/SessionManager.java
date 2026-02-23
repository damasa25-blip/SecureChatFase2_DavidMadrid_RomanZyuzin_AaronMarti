package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita3;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * FITA 3 - SessionManager optimizado
 * Gestor de sesiones con sincronización eficiente
 */
public class SessionManager {
    private static final long HEARTBEAT_INTERVAL = 5000;
    private static final long HEARTBEAT_TIMEOUT = 15000;
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");

    private Map<String, UserSession> sessions;
    private Map<String, String> usernameToSessionId;
    private ScheduledExecutorService heartbeatExecutor;

    public SessionManager() {
        this.sessions = new ConcurrentHashMap<>();
        this.usernameToSessionId = new ConcurrentHashMap<>();
    }

    public synchronized UserSession createSession(String username, ClientHandler handler) {
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
                
                list.append(String.format("  %s %d. %-15s [%s] - %s\n", 
                    status, count++, username, session.getSessionId(), loginTime));
            }
        }
        
        return list.toString();
    }

    public void broadcastMessage(String message, String excludeSessionId) {
        sessions.values().parallelStream().forEach(session -> {
            if (excludeSessionId == null || !session.getSessionId().equals(excludeSessionId)) {
                ClientHandler handler = session.getHandler();
                if (handler != null) {
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
