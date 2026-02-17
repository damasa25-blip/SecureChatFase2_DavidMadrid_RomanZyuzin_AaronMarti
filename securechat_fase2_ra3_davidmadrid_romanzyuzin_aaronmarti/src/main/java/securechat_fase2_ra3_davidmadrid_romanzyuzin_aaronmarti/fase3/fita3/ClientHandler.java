package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * FITA 3 - ClientHandler optimizado
 * Versión con mejor manejo de recursos y rendimiento
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private SessionManager sessionManager;
    private ServidorEscalable server;
    private Protocol protocol;
    private BufferedReader input;
    private PrintWriter output;
    private UserSession session;
    private boolean connected;

    public ClientHandler(Socket socket, SessionManager manager, ServidorEscalable server) {
        this.clientSocket = socket;
        this.sessionManager = manager;
        this.server = server;
        this.protocol = new Protocol(manager, server);
        this.connected = true;
    }

    @Override
    public void run() {
        try {
            // Configurar streams con buffers optimizados
            input = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()), 8192);
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            sendWelcomeMessage();

            // Procesar comandos
            String message;
            while (connected && (message = input.readLine()) != null) {
                // Actualizar heartbeat
                if (session != null) {
                    session.updateLastHeartbeat();
                }

                // Procesar comando
                String response = protocol.processCommand(message, this);
                
                if (response != null && !response.isEmpty()) {
                    output.println(response);
                }

                if (message.trim().toUpperCase().startsWith("QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            // Conexión perdida
        } finally {
            cleanup();
        }
    }

    private void sendWelcomeMessage() {
        output.println("╔════════════════════════════════════════════════════════╗");
        output.println("║       SecureChat Server 1.0 - SERVIDOR ESCALABLE      ║");
        output.println("║              FITA 3 - Optimización Final              ║");
        output.println("╚════════════════════════════════════════════════════════╝");
        output.println("Comandos: LOGIN, MSG, LIST, WHOAMI, STATS, PING, QUIT\n");
    }

    public void sendMessage(String message) {
        if (output != null && !clientSocket.isClosed()) {
            output.println(message);
        }
    }

    public UserSession getSession() {
        return session;
    }

    public void setSession(UserSession session) {
        this.session = session;
    }

    public void disconnect() {
        connected = false;
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            // Ignorar
        }
    }

    private void cleanup() {
        connected = false;
        
        // Registrar desconexión
        server.recordDisconnection();
        
        // Eliminar sesión
        if (session != null) {
            String username = session.getUsername();
            sessionManager.removeSession(session.getSessionId());
            
            if (username != null) {
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
            // Ignorar
        }
    }
}
