package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * FITA 2 - Gestión de Sesiones
 * Clase ClientHandler - Gestión de comunicación con cada cliente
 * Incluye soporte para heartbeat y detección de clientes inactivos
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private SessionManager sessionManager;
    private Protocol protocol;
    private BufferedReader input;
    private PrintWriter output;
    private UserSession session;
    private boolean connected;

    public ClientHandler(Socket socket, SessionManager manager) {
        this.clientSocket = socket;
        this.sessionManager = manager;
        this.protocol = new Protocol(manager);
        this.connected = true;
    }

    @Override
    public void run() {
        try {
            // Configurar streams de entrada/salida
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            // Mensaje de bienvenida
            sendWelcomeMessage();

            // Procesar comandos del cliente
            String message;
            while (connected && (message = input.readLine()) != null) {
                // Actualizar último heartbeat (cada mensaje es un heartbeat)
                if (session != null) {
                    session.updateLastHeartbeat();
                }

                // Procesar comando según el protocolo
                String response = protocol.processCommand(message, this);
                
                if (response != null && !response.isEmpty()) {
                    output.println(response);
                }

                // Verificar si el cliente se desconectó
                if (message.trim().toUpperCase().startsWith("QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + getIdentifier());
        } finally {
            cleanup();
        }
    }

    /**
     * Envía el mensaje de bienvenida al cliente
     */
    private void sendWelcomeMessage() {
        output.println("╔════════════════════════════════════════════╗");
        output.println("║    Bienvenido a SecureChat Server 1.0     ║");
        output.println("║    FITA 2 - Gestión de Sesiones           ║");
        output.println("╚════════════════════════════════════════════╝");
        output.println("Comandos disponibles:");
        output.println("  LOGIN <nombre>  - Iniciar sesión");
        output.println("  MSG <mensaje>   - Enviar mensaje");
        output.println("  LIST           - Ver usuarios conectados");
        output.println("  WHOAMI         - Ver tu información");
        output.println("  PING           - Verificar conexión");
        output.println("  QUIT           - Desconectar\n");
    }

    /**
     * Envía un mensaje a este cliente
     */
    public void sendMessage(String message) {
        if (output != null && !clientSocket.isClosed()) {
            output.println(message);
        }
    }

    /**
     * Obtiene la sesión del usuario
     */
    public UserSession getSession() {
        return session;
    }

    /**
     * Establece la sesión del usuario
     */
    public void setSession(UserSession session) {
        this.session = session;
    }

    /**
     * Desconecta este cliente (llamado por el monitor de heartbeat)
     */
    public void disconnect() {
        connected = false;
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            // Ignorar errores al cerrar
        }
    }

    /**
     * Obtiene un identificador para logs
     */
    private String getIdentifier() {
        if (session != null && session.getUsername() != null) {
            return session.getUsername();
        }
        return clientSocket.getInetAddress().toString();
    }

    /**
     * Limpieza de recursos
     */
    private void cleanup() {
        connected = false;
        
        // Eliminar sesión
        if (session != null) {
            String username = session.getUsername();
            sessionManager.removeSession(session.getSessionId());
            
            if (username != null) {
                System.out.println("Sesión cerrada: " + username + 
                    " [" + session.getSessionId() + "]");
                
                // Notificar desconexión
                String notification = ">>> " + username + " se ha desconectado";
                sessionManager.broadcastMessage(notification, session.getSessionId());
            }
        }

        // Cerrar recursos
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            // Ignorar errores al cerrar
        }
    }
}
