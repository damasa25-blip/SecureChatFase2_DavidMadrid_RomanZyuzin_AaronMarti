package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita2;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FASE 4 - FITA 2
 * Representa una sesión de usuario con información de estado
 */
public class UserSession {
    private String sessionId;
    private String username;
    private LocalDateTime loginTime;
    private long lastHeartbeat;
    private ClientHandler handler;
    private boolean active;
    private boolean keyExchanged; // Indica si se completó el intercambio de claves

    public UserSession(String username, ClientHandler handler) {
        this.sessionId = UUID.randomUUID().toString().substring(0, 8);
        this.username = username;
        this.handler = handler;
        this.loginTime = LocalDateTime.now();
        this.lastHeartbeat = System.currentTimeMillis();
        this.active = true;
        this.keyExchanged = false;
    }

    public synchronized void updateLastHeartbeat() {
        this.lastHeartbeat = System.currentTimeMillis();
    }

    public synchronized boolean isInactive(long timeoutMs) {
        return (System.currentTimeMillis() - lastHeartbeat) > timeoutMs;
    }

    public long getSessionDuration() {
        return System.currentTimeMillis() - 
            loginTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // Getters y Setters
    public String getSessionId() { return sessionId; }
    public String getUsername() { return username; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public long getLastHeartbeat() { return lastHeartbeat; }
    public ClientHandler getHandler() { return handler; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isKeyExchanged() { return keyExchanged; }
    public void setKeyExchanged(boolean keyExchanged) { this.keyExchanged = keyExchanged; }
}
