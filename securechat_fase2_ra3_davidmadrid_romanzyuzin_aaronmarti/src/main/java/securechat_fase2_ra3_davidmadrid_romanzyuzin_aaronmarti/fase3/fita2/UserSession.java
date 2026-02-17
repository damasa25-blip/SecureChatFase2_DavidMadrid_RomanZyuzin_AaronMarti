package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita2;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FITA 2 - Gestión de Sesiones
 * Clase UserSession - Representa una sesión de usuario
 * Contiene toda la información de estado de un usuario conectado
 */
public class UserSession {
    private String sessionId;
    private String username;
    private LocalDateTime loginTime;
    private long lastHeartbeat;
    private ClientHandler handler;
    private boolean active;

    public UserSession(String username, ClientHandler handler) {
        this.sessionId = UUID.randomUUID().toString().substring(0, 8);
        this.username = username;
        this.handler = handler;
        this.loginTime = LocalDateTime.now();
        this.lastHeartbeat = System.currentTimeMillis();
        this.active = true;
    }

    /**
     * Actualiza el timestamp del último heartbeat
     */
    public synchronized void updateLastHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
    }

    /**
     * Verifica si la sesión está inactiva (sin heartbeat)
     * @param timeoutMs Timeout en milisegundos
     * @return true si está inactiva
     */
    public synchronized boolean isInactive(long timeoutMs) {
        return (System.currentTimeMillis() - lastHeartbeat) > timeoutMs;
    }

    /**
     * Obtiene la duración de la sesión en milisegundos
     */
    public long getSessionDuration() {
        return System.currentTimeMillis() - 
            loginTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // Getters y setters
    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public ClientHandler getHandler() {
        return handler;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id='" + sessionId + '\'' +
                ", user='" + username + '\'' +
                ", active=" + active +
                '}';
    }
}
