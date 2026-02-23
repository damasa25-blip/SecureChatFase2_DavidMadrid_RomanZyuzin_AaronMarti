package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita3;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FITA 3 - Protocol optimizado
 * Incluye comando STATS para ver métricas del servidor
 */
public class Protocol {
    private SessionManager sessionManager;
    private ServidorEscalable server;
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");

    public Protocol(SessionManager manager, ServidorEscalable server) {
        this.sessionManager = manager;
        this.server = server;
    }

    public String processCommand(String command, ClientHandler handler) {
        if (command == null || command.trim().isEmpty()) {
            return null;
        }

        String[] parts = command.trim().split(" ", 2);
        String cmd = parts[0].toUpperCase();

        // Registrar mensaje procesado
        server.recordMessage();

        switch (cmd) {
            case "LOGIN":
                return handleLogin(parts, handler);
            case "MSG":
                return handleMessage(parts, handler);
            case "LIST":
                return handleList(handler);
            case "WHOAMI":
                return handleWhoami(handler);
            case "STATS":
                return handleStats(handler);
            case "PING":
                return handlePing(handler);
            case "QUIT":
                return handleQuit(handler);
            default:
                return "ERROR: Comando desconocido";
        }
    }

    private String handleLogin(String[] parts, ClientHandler handler) {
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            return "ERROR: Uso: LOGIN <nombre>";
        }

        String username = parts[1].trim();

        if (handler.getSession() != null) {
            return "ERROR: Ya has iniciado sesión";
        }

        if (sessionManager.usernameExists(username)) {
            return "ERROR: Nombre de usuario en uso";
        }

        UserSession session = sessionManager.createSession(username, handler);
        handler.setSession(session);

        String notification = ">>> " + username + " se ha conectado";
        sessionManager.broadcastMessage(notification, session.getSessionId());

        return "OK: Sesión iniciada como " + username +
               "\nSession ID: " + session.getSessionId();
    }

    private String handleMessage(String[] parts, ClientHandler handler) {
        UserSession session = handler.getSession();
        
        if (session == null) {
            return "ERROR: Debes hacer LOGIN primero";
        }

        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            return "ERROR: Uso: MSG <mensaje>";
        }

        String message = parts[1].trim();
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String formattedMessage = "[" + timestamp + "] " + 
            session.getUsername() + ": " + message;

        sessionManager.broadcastMessage(formattedMessage, null);
        return null;
    }

    private String handleList(ClientHandler handler) {
        if (handler.getSession() == null) {
            return "ERROR: Debes hacer LOGIN primero";
        }

        int count = sessionManager.getSessionCount();
        String userList = sessionManager.getUserList();
        
        return "════════════════════════════════════════════\n" +
               "Usuarios conectados: " + count + "\n" +
               "════════════════════════════════════════════\n" +
               userList;
    }

    private String handleWhoami(ClientHandler handler) {
        UserSession session = handler.getSession();
        
        if (session == null) {
            return "ERROR: No has iniciado sesión";
        }

        long duration = session.getSessionDuration();
        long minutes = duration / 60000;
        long seconds = (duration % 60000) / 1000;

        return "════════════════════════════════════════════\n" +
               "Tu información:\n" +
               "════════════════════════════════════════════\n" +
               "Usuario: " + session.getUsername() + "\n" +
               "Session ID: " + session.getSessionId() + "\n" +
               "Conectado desde: " + session.getLoginTime().format(TIME_FORMATTER) + "\n" +
               "Duración: " + minutes + "m " + seconds + "s\n" +
               "Estado: " + (session.isActive() ? "ACTIVO" : "INACTIVO");
    }

    private String handleStats(ClientHandler handler) {
        if (handler.getSession() == null) {
            return "ERROR: Debes hacer LOGIN primero";
        }

        return "════════════════════════════════════════════\n" +
               "Estadísticas del Servidor\n" +
               "════════════════════════════════════════════\n" +
               server.getMetrics();
    }

    private String handlePing(ClientHandler handler) {
        if (handler.getSession() != null) {
            handler.getSession().updateLastHeartbeat();
        }
        return null; // No responder para evitar spam
    }

    private String handleQuit(ClientHandler handler) {
        UserSession session = handler.getSession();
        
        if (session != null && session.getUsername() != null) {
            String notification = ">>> " + session.getUsername() + " se ha desconectado";
            sessionManager.broadcastMessage(notification, session.getSessionId());
            
            long duration = session.getSessionDuration();
            long minutes = duration / 60000;
            long seconds = (duration % 60000) / 1000;
            
            return "OK: Desconectando...\n" +
                   "Duración: " + minutes + "m " + seconds + "s\n" +
                   "Hasta pronto, " + session.getUsername() + "!";
        }

        return "OK: Hasta pronto!";
    }
}
