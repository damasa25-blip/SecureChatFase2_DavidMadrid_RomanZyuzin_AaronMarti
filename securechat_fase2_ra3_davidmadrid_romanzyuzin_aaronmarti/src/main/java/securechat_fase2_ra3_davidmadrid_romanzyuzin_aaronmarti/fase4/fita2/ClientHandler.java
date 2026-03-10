package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.crypto.SecretKey;

/**
 * FASE 4 - FITA 2
 * ClientHandler con intercambio de claves RSA
 * 
 * Flujo de comunicación:
 * 1. Cliente conecta
 * 2. Servidor envía clave pública RSA
 * 3. Cliente genera clave AES, la cifra con RSA y la envía
 * 4. Servidor descifra clave AES con su clave privada
 * 5. Toda comunicación MSG usa la clave AES negociada
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private SessionManager sessionManager;
    private ServidorEscalable server;
    private Protocol protocol;
    private BufferedReader input;
    private PrintWriter output;
    private UserSession session;
    private Seguretat seguretat;
    private boolean connected;
    private boolean keyExchangeCompleted;

    public ClientHandler(Socket socket, SessionManager manager, ServidorEscalable server, String publicKeyRSA) {
        this.clientSocket = socket;
        this.sessionManager = manager;
        this.server = server;
        this.seguretat = new Seguretat();
        this.connected = true;
        this.keyExchangeCompleted = false;
        
        // Enviar clave pública al cliente inmediatamente después de conectar
        try {
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()), 8192);
                
            // PASO 1: Enviar clave pública RSA al cliente
            output.println("RSA_PUBLIC_KEY:" + publicKeyRSA);
            
        } catch (IOException e) {
            System.err.println("Error al configurar streams: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            sendWelcomeMessage();

            // PASO 2: Esperar clave AES cifrada del cliente
            String message = input.readLine();
            
            if (message != null && message.startsWith("AES_KEY_ENCRYPTED:")) {
                String encryptedAESKey = message.substring("AES_KEY_ENCRYPTED:".length());
                
                try {
                    // PASO 3: Descifrar clave AES con clave privada RSA
                    String aesKeyBase64 = server.getSeguretat().decryptRSA(encryptedAESKey);
                    SecretKey aesKey = Seguretat.base64ToKey(aesKeyBase64);
                    
                    // Establecer clave AES para este cliente
                    seguretat.setAESKey(aesKey);
                    keyExchangeCompleted = true;
                    
                    output.println("KEY_EXCHANGE_OK");
                    
                    System.out.println("[Servidor] Intercambio de claves completado con cliente " + 
                                     clientSocket.getInetAddress());
                    
                } catch (Exception e) {
                    output.println("KEY_EXCHANGE_ERROR: " + e.getMessage());
                    System.err.println("Error en intercambio de claves: " + e.getMessage());
                    return;
                }
            } else {
                output.println("ERROR: Se esperaba clave AES cifrada");
                return;
            }

            // PASO 4: Crear protocol con la clave negociada
            this.protocol = new Protocol(sessionManager, server, seguretat);

            // Procesar comandos normalmente
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
            if (connected) {
                System.err.println("Error de conexión: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void sendWelcomeMessage() {
        output.println("╔════════════════════════════════════════════════════════╗");
        output.println("║       SecureChat Server 2.0 - FASE 4 FITA 2          ║");
        output.println("║      RSA Key Exchange + AES/GCM Encryption            ║");
        output.println("╚════════════════════════════════════════════════════════╝");
        output.println("Comandos: LOGIN, MSG, LIST, WHOAMI, STATS, PING, QUIT");
        output.println("🔒 Intercambio de claves RSA completado");
        output.println("🔐 Mensajes cifrados con AES/GCM-256\n");
    }

    public void sendMessage(String message) {
        if (output != null && !clientSocket.isClosed()) {
            try {
                output.println(message);
            } catch (Exception e) {
                System.err.println("Error al enviar mensaje: " + e.getMessage());
            }
        }
    }

    public UserSession getSession() {
        return session;
    }

    public void setSession(UserSession session) {
        this.session = session;
        if (session != null && keyExchangeCompleted) {
            session.setKeyExchanged(true);
        }
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
