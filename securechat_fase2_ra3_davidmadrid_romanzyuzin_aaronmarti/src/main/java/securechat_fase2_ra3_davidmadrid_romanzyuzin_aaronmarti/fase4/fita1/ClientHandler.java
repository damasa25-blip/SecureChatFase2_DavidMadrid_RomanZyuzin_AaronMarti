package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * FASE 4 - FITA 1
 * ClientHandler con soporte para cifrado AES/GCM
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

    public ClientHandler(Socket socket, SessionManager manager, ServidorEscalable server, Seguretat seguretat) {
        this.clientSocket = socket;
        this.sessionManager = manager;
        this.server = server;
        this.protocol = new Protocol(manager, server, seguretat);
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
        output.println("║       SecureChat Server 2.0 - FASE 4 FITA 1          ║");
        output.println("║              AES/GCM Cifrado Simétrico                ║");
        output.println("╚════════════════════════════════════════════════════════╝");
        output.println("Comandos: LOGIN, MSG, LIST, WHOAMI, STATS, PING, QUIT");
        output.println("🔒 Mensajes MSG viajan cifrados con AES/GCM-256\n");
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
