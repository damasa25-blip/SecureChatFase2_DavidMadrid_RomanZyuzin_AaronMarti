package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita1;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FASE 4 - FITA 1
 * Protocol con cifrado AES/GCM para mensajes MSG
 * 
 * Comandos sin cifrar: LOGIN, LIST, WHOAMI, STATS, PING, QUIT
 * Comandos cifrados: MSG (solo el contenido del mensaje)
 */
public class Protocol {
    private SessionManager sessionManager;
    private ServidorEscalable server;
    private Seguretat seguretat;
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");

    public Protocol(SessionManager manager, ServidorEscalable server, Seguretat seguretat) {
        this.sessionManager = manager;
        this.server = server;
        this.seguretat = seguretat;
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
               "\nSession ID: " + session.getSessionId() +
               "\n🔒 Cifrado AES/GCM ACTIVO";
    }

    /**
     * Maneja mensajes cifrados con AES/GCM
     * El formato es: MSG <mensaje_cifrado_base64>
     */
    private String handleMessage(String[] parts, ClientHandler handler) {
        UserSession session = handler.getSession();
        
        if (session == null) {
            return "ERROR: Debes hacer LOGIN primero";
        }

        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            return "ERROR: Uso: MSG <mensaje>";
        }

        try {
            // El mensaje viene cifrado en Base64
            String encryptedMessage = parts[1].trim();
            
            // Descifrar mensaje
            String plainMessage = seguretat.decrypt(encryptedMessage);
            
            // Validar que el mensaje no esté vacío después de descifrar
            if (plainMessage == null || plainMessage.trim().isEmpty()) {
                return "ERROR: Mensaje vacío";
            }
            
            // Formatear mensaje con timestamp
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            String formattedMessage = "[" + timestamp + "] " + 
                session.getUsername() + ": " + plainMessage;

            // Broadcast del mensaje SIN CIFRAR a todos los clientes
            // Cada cliente no necesita descifrarlo de nuevo
            sessionManager.broadcastMessage(formattedMessage, null);
            
            return null;
            
        } catch (Exception e) {
            return "ERROR: No se pudo descifrar el mensaje - " + e.getMessage();
        }
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
               "Estado: " + (session.isActive() ? "ACTIVO" : "INACTIVO") + "\n" +
               "Cifrado: AES/GCM-256";
    }

    private String handleStats(ClientHandler handler) {
        if (handler.getSession() == null) {
            return "ERROR: Debes hacer LOGIN primero";
        }

        return "════════════════════════════════════════════\n" +
               "Estadísticas del Servidor\n" +
               "════════════════════════════════════════════\n" +
               server.getMetrics() + "\n" +
               "Cifrado: AES/GCM-256 ✓";
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
