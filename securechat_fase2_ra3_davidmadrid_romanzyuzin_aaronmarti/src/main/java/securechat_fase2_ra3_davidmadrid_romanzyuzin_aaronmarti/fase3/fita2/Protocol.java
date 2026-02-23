package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FITA 2 - Gestión de Sesiones
 * Clase Protocol - Procesamiento de comandos extendido
 * Incluye nuevos comandos para gestión de sesiones
 */
public class Protocol {
    private SessionManager sessionManager;
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");

    public Protocol(SessionManager manager) {
        this.sessionManager = manager;
    }

    /**
     * Procesa un comando recibido del cliente
     */
    public String processCommand(String command, ClientHandler handler) {
        if (command == null || command.trim().isEmpty()) {
            return "ERROR: Comando vacío";
        }

        String[] parts = command.trim().split(" ", 2);
        String cmd = parts[0].toUpperCase();

        switch (cmd) {
            case "LOGIN":
                return handleLogin(parts, handler);
            
            case "MSG":
                return handleMessage(parts, handler);
            
            case "LIST":
                return handleList(handler);
            
            case "WHOAMI":
                return handleWhoami(handler);
            
            case "PING":
                return handlePing(handler);
            
            case "QUIT":
                return handleQuit(handler);
            
            default:
                return "ERROR: Comando desconocido. Use LOGIN, MSG, LIST, WHOAMI, PING o QUIT";
        }
    }

    /**
     * Procesa el comando LOGIN - Crea una sesión real
     */
    private String handleLogin(String[] parts, ClientHandler handler) {
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            return "ERROR: Uso correcto: LOGIN <nombre>";
        }

        String username = parts[1].trim();

        // Verificar si ya tiene sesión activa
        if (handler.getSession() != null) {
            return "ERROR: Ya has iniciado sesión como " + 
                handler.getSession().getUsername();
        }

        // Verificar si el nombre de usuario ya está en uso
        if (sessionManager.usernameExists(username)) {
            return "ERROR: El nombre de usuario ya está en uso";
        }

        // Crear nueva sesión
        UserSession session = sessionManager.createSession(username, handler);
        handler.setSession(session);

        // Notificar a otros usuarios
        String notification = ">>> " + username + " se ha conectado";
        sessionManager.broadcastMessage(notification, session.getSessionId());

        String loginTime = LocalDateTime.now().format(TIME_FORMATTER);
        return "OK: Sesión iniciada como " + username + 
               "\nSession ID: " + session.getSessionId() +
               "\nHora de login: " + loginTime;
    }

    /**
     * Procesa el comando MSG
     */
    private String handleMessage(String[] parts, ClientHandler handler) {
        UserSession session = handler.getSession();
        
        if (session == null || session.getUsername() == null) {
            return "ERROR: Debes hacer LOGIN primero";
        }

        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            return "ERROR: Uso correcto: MSG <mensaje>";
        }

        String message = parts[1].trim();
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String formattedMessage = "[" + timestamp + "] " + 
            session.getUsername() + ": " + message;

        // Broadcast del mensaje a todos los usuarios
        sessionManager.broadcastMessage(formattedMessage, null);

        return null; // No responder al emisor (ya ve su mensaje en el broadcast)
    }

    /**
     * Procesa el comando LIST
     */
    private String handleList(ClientHandler handler) {
        if (handler.getSession() == null) {
            return "ERROR: Debes hacer LOGIN primero";
        }

        int count = sessionManager.getSessionCount();
        String userList = sessionManager.getUserList();
        
        return "═══════════════════════════════════════════\n" +
               "Usuarios conectados: " + count + "\n" +
               "═══════════════════════════════════════════\n" +
               userList;
    }

    /**
     * Procesa el comando WHOAMI - Muestra información de la sesión actual
     */
    private String handleWhoami(ClientHandler handler) {
        UserSession session = handler.getSession();
        
        if (session == null) {
            return "ERROR: No has iniciado sesión";
        }

        long sessionDuration = session.getSessionDuration();
        long minutes = sessionDuration / 60000;
        long seconds = (sessionDuration % 60000) / 1000;

        return "═══════════════════════════════════════════\n" +
               "Tu información de sesión:\n" +
               "═══════════════════════════════════════════\n" +
               "Usuario: " + session.getUsername() + "\n" +
               "Session ID: " + session.getSessionId() + "\n" +
               "Conectado desde: " + 
                   session.getLoginTime().format(TIME_FORMATTER) + "\n" +
               "Duración: " + minutes + "m " + seconds + "s\n" +
               "Estado: " + (session.isActive() ? "ACTIVO" : "INACTIVO");
    }

    /**
     * Procesa el comando PING - Verifica la conexión
     */
    private String handlePing(ClientHandler handler) {
        if (handler.getSession() != null) {
            handler.getSession().updateLastHeartbeat();
            return "PONG: Conexión activa";
        }
        return "PONG: Conectado (sin sesión)";
    }

    /**
     * Procesa el comando QUIT
     */
    private String handleQuit(ClientHandler handler) {
        UserSession session = handler.getSession();
        
        if (session != null && session.getUsername() != null) {
            // Notificar a otros usuarios
            String notification = ">>> " + session.getUsername() + " se ha desconectado";
            sessionManager.broadcastMessage(notification, session.getSessionId());
            
            long sessionDuration = session.getSessionDuration();
            long minutes = sessionDuration / 60000;
            long seconds = (sessionDuration % 60000) / 1000;
            
            return "OK: Desconectando...\n" +
                   "Duración de sesión: " + minutes + "m " + seconds + "s\n" +
                   "Hasta pronto, " + session.getUsername() + "!";
        }

        return "OK: Desconectando... Hasta pronto!";
    }
}
