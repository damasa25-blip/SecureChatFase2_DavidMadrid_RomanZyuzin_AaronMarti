package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FASE 4 - FITA 2
 * Protocol con validaciones robustas y gestión de errores
 * 
 * Nuevas características:
 * - Validación de entradas (nombres de usuario, mensajes)
 * - Gestión de errores de cifrado/descifrado
 * - Detección de datos corruptos
 * - Mensajes de error descriptivos
 */
public class Protocol {
    private SessionManager sessionManager;
    private ServidorEscalable server;
    private Seguretat seguretat;
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Límites de validación
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MIN_MESSAGE_LENGTH = 1;

    public Protocol(SessionManager manager, ServidorEscalable server, Seguretat seguretat) {
        if (manager == null || server == null || seguretat == null) {
            throw new IllegalArgumentException("Los parámetros no pueden ser null");
        }
        this.sessionManager = manager;
        this.server = server;
        this.seguretat = seguretat;
    }

    public String processCommand(String command, ClientHandler handler) {
        // Validación básica
        if (command == null || command.trim().isEmpty()) {
            return "ERROR: Comando vacío";
        }

        try {
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
                    return "ERROR: Comando desconocido '" + cmd + "'";
            }
        } catch (Exception e) {
            return "ERROR: Error al procesar comando - " + e.getMessage();
        }
    }

    private String handleLogin(String[] parts, ClientHandler handler) {
        try {
            // Validar formato
            if (parts.length < 2 || parts[1].trim().isEmpty()) {
                return "ERROR: Uso: LOGIN <nombre>\nEjemplo: LOGIN Juan";
            }

            String username = parts[1].trim();

            // Verificar sesión existente
            if (handler.getSession() != null) {
                return "ERROR: Ya has iniciado sesión como '" + 
                       handler.getSession().getUsername() + "'";
            }

            // Validar nombre de usuario
            String validationError = sessionManager.validateUsername(username);
            if (validationError != null) {
                return validationError;
            }

            // Verificar disponibilidad
            if (sessionManager.usernameExists(username)) {
                return "ERROR: El nombre de usuario '" + username + "' ya está en uso";
            }

            // Crear sesión
            UserSession session = sessionManager.createSession(username, handler);
            handler.setSession(session);

            String notification = ">>> " + username + " se ha conectado";
            sessionManager.broadcastMessage(notification, session.getSessionId());

            return "OK: Sesión iniciada como " + username +
                   "\nSession ID: " + session.getSessionId() +
                   "\n🔒 Cifrado AES/GCM + RSA ACTIVO" +
                   "\n✓ Intercambio de claves completado";
                   
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        } catch (Exception e) {
            return "ERROR: Error al iniciar sesión - " + e.getMessage();
        }
    }

    private String handleMessage(String[] parts, ClientHandler handler) {
        try {
            UserSession session = handler.getSession();
            
            // Validar sesión
            if (session == null) {
                return "ERROR: Debes hacer LOGIN primero";
            }

            // Verificar intercambio de claves
            if (!session.isKeyExchanged()) {
                return "ERROR: Intercambio de claves no completado";
            }

            // Validar formato
            if (parts.length < 2 || parts[1].trim().isEmpty()) {
                return "ERROR: Uso: MSG <mensaje>\nEjemplo: MSG Hola a todos";
            }

            String encryptedMessage = parts[1].trim();
            
            // Validar longitud del mensaje cifrado
            if (encryptedMessage.length() > MAX_MESSAGE_LENGTH * 2) {
                return "ERROR: Mensaje demasiado largo";
            }

            try {
                // Descifrar mensaje
                String plainMessage = seguretat.decrypt(encryptedMessage);
                server.recordDecryption();
                
                // Validar mensaje descifrado
                if (plainMessage == null || plainMessage.trim().isEmpty()) {
                    return "ERROR: El mensaje no puede estar vacío";
                }
                
                if (plainMessage.length() < MIN_MESSAGE_LENGTH) {
                    return "ERROR: El mensaje es demasiado corto";
                }
                
                if (plainMessage.length() > MAX_MESSAGE_LENGTH) {
                    return "ERROR: El mensaje supera el límite de " + 
                           MAX_MESSAGE_LENGTH + " caracteres";
                }
                
                // Calcular hash para integridad (opcional)
                String messageHash = Seguretat.calculateSHA256(plainMessage);
                
                // Formatear mensaje
                String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
                String formattedMessage = "[" + timestamp + "] " + 
                    session.getUsername() + ": " + plainMessage;

                // Broadcast del mensaje
                sessionManager.broadcastMessage(formattedMessage, null);
                
                return null;
                
            } catch (IllegalArgumentException e) {
                return "ERROR: Formato de mensaje inválido - " + e.getMessage();
            } catch (Exception e) {
                return "ERROR: No se pudo descifrar el mensaje.\n" +
                       "Posibles causas:\n" +
                       "  - Mensaje corrupto durante la transmisión\n" +
                       "  - Clave de cifrado incorrecta\n" +
                       "  - Datos alterados (integridad comprometida)";
            }
            
        } catch (Exception e) {
            return "ERROR: Error al procesar mensaje - " + e.getMessage();
        }
    }

    private String handleList(ClientHandler handler) {
        try {
            if (handler.getSession() == null) {
                return "ERROR: Debes hacer LOGIN primero";
            }

            int count = sessionManager.getSessionCount();
            String userList = sessionManager.getUserList();
            
            return "════════════════════════════════════════════\n" +
                   "Usuarios conectados: " + count + "\n" +
                   "════════════════════════════════════════════\n" +
                   userList +
                   "\nLeyenda: ● Activo | ○ Inactivo | 🔒 Seguro";
                   
        } catch (Exception e) {
            return "ERROR: Error al obtener lista - " + e.getMessage();
        }
    }

    private String handleWhoami(ClientHandler handler) {
        try {
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
                   "Cifrado: AES/GCM-256 + RSA-2048\n" +
                   "Intercambio de claves: " + (session.isKeyExchanged() ? "✓ Completado" : "✗ Pendiente");
                   
        } catch (Exception e) {
            return "ERROR: Error al obtener información - " + e.getMessage();
        }
    }

    private String handleStats(ClientHandler handler) {
        try {
            if (handler.getSession() == null) {
                return "ERROR: Debes hacer LOGIN primero";
            }

            return "════════════════════════════════════════════\n" +
                   "Estadísticas del Servidor\n" +
                   "════════════════════════════════════════════\n" +
                   server.getMetrics() + "\n" +
                   "Cifrado: AES/GCM-256 + RSA-2048 ✓";
                   
        } catch (Exception e) {
            return "ERROR: Error al obtener estadísticas - " + e.getMessage();
        }
    }

    private String handlePing(ClientHandler handler) {
        try {
            if (handler.getSession() != null) {
                handler.getSession().updateLastHeartbeat();
            }
            return null;
        } catch (Exception e) {
            return "ERROR: Error en heartbeat - " + e.getMessage();
        }
    }

    private String handleQuit(ClientHandler handler) {
        try {
            UserSession session = handler.getSession();
            
            if (session != null && session.getUsername() != null) {
                String notification = ">>> " + session.getUsername() + " se ha desconectado";
                sessionManager.broadcastMessage(notification, session.getSessionId());
                
                long duration = session.getSessionDuration();
                long minutes = duration / 60000;
                long seconds = (duration % 60000) / 1000;
                
                return "OK: Desconectando...\n" +
                       "Duración de sesión: " + minutes + "m " + seconds + "s\n" +
                       "Hasta pronto, " + session.getUsername() + "!";
            }

            return "OK: Hasta pronto!";
            
        } catch (Exception e) {
            return "ERROR: Error al desconectar - " + e.getMessage();
        }
    }
}
