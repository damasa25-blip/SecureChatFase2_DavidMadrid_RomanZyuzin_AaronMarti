package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * FASE 4 - FITA 1
 * Cliente con cifrado AES/GCM para mensajes
 */
public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12348; // Mismo puerto que el servidor FITA 1
    private static final long PING_INTERVAL = 5000;
    
    // Clave precompartida (la misma que el servidor)
    private static final String SHARED_KEY = "8Zl5ryWZeRV4mVGhlnde6A2WBpdqXZx+xdIYzJ6X7yg=";
    
    public static void main(String[] args) {
        Seguretat seguretat = new Seguretat(SHARED_KEY);
        
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("╔════════════════════════════════════════════════════════╗");
            System.out.println("║    Conectado a SecureChat Server 2.0 (FASE 4 FITA 1) ║");
            System.out.println("║              AES/GCM Cifrado Simétrico                ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println("🔒 Tus mensajes viajan cifrados con AES/GCM-256\n");
            
            // Executor para heartbeat
            ScheduledExecutorService pingExecutor = Executors.newSingleThreadScheduledExecutor();
            final boolean[] loggedIn = {false};
            
            // Enviar PING cada 5 segundos
            pingExecutor.scheduleAtFixedRate(() -> {
                if (loggedIn[0]) {
                    output.println("PING");
                }
            }, PING_INTERVAL, PING_INTERVAL, TimeUnit.MILLISECONDS);
            
            // Hilo para recibir mensajes
            Thread receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = input.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("\n[Desconectado del servidor]");
                }
            });
            receiveThread.start();

            // Enviar comandos
            String userInput;
            while (scanner.hasNextLine()) {
                userInput = scanner.nextLine();
                
                if (userInput.trim().isEmpty()) {
                    continue;
                }
                
                // Procesar comando
                String commandToSend = processCommand(userInput, seguretat);
                
                if (commandToSend != null) {
                    output.println(commandToSend);
                }
                
                // Activar heartbeat después de LOGIN
                if (userInput.trim().toUpperCase().startsWith("LOGIN")) {
                    loggedIn[0] = true;
                }
                
                if (userInput.trim().toUpperCase().startsWith("QUIT")) {
                    break;
                }
            }
            
            pingExecutor.shutdown();

        } catch (IOException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
    }
    
    /**
     * Procesa comando del usuario
     * Si es MSG, cifra el contenido
     */
    private static String processCommand(String userInput, Seguretat seguretat) {
        String[] parts = userInput.trim().split(" ", 2);
        String cmd = parts[0].toUpperCase();
        
        if (cmd.equals("MSG") && parts.length >= 2) {
            try {
                // Cifrar el mensaje
                String plainMessage = parts[1];
                String encryptedMessage = seguretat.encrypt(plainMessage);
                
                // Retornar comando con mensaje cifrado
                return "MSG " + encryptedMessage;
                
            } catch (Exception e) {
                System.err.println("❌ Error al cifrar mensaje: " + e.getMessage());
                return null;
            }
        }
        
        // Para otros comandos, enviar tal cual
        return userInput;
    }
}
