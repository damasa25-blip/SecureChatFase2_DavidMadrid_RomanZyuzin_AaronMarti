package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita2;

import javax.crypto.SecretKey;
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
 * FASE 4 - FITA 2
 * Cliente con intercambio de claves RSA
 * 
 * Flujo de conexión:
 * 1. Conectar al servidor
 * 2. Recibir clave pública RSA del servidor
 * 3. Generar clave AES aleatoria
 * 4. Cifrar clave AES con clave pública RSA
 * 5. Enviar clave AES cifrada al servidor
 * 6. Usar clave AES para cifrar/descifrar mensajes
 */
public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12349; // Puerto de FITA 2
    private static final long PING_INTERVAL = 5000;
    
    private static Seguretat seguretat;
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("╔════════════════════════════════════════════════════════╗");
            System.out.println("║    Conectado a SecureChat Server 2.0 (FASE 4 FITA 2) ║");
            System.out.println("║      RSA Key Exchange + AES/GCM Encryption            ║");
            System.out.println("╚════════════════════════════════════════════════════════╝\n");
            System.out.println("Realizando intercambio de claves...\n");
            
            // PASO 1: Recibir clave pública RSA del servidor
            String message = input.readLine();
            if (message == null || !message.startsWith("RSA_PUBLIC_KEY:")) {
                System.err.println("❌ Error: No se recibió la clave pública RSA");
                return;
            }
            
            String publicKeyRSA = message.substring("RSA_PUBLIC_KEY:".length());
            System.out.println("✓ Clave pública RSA recibida del servidor");
            
            // PASO 2: Generar clave AES aleatoria
            SecretKey aesKey = Seguretat.generateAESKey();
            String aesKeyBase64 = Seguretat.keyToBase64(aesKey);
            System.out.println("✓ Clave AES-256 generada localmente");
            
            // PASO 3: Cifrar clave AES con clave pública RSA
            String encryptedAESKey = Seguretat.encryptRSA(aesKeyBase64, publicKeyRSA);
            System.out.println("✓ Clave AES cifrada con RSA");
            
            // PASO 4: Enviar clave AES cifrada al servidor
            output.println("AES_KEY_ENCRYPTED:" + encryptedAESKey);
            System.out.println("✓ Clave AES enviada al servidor");
            
            // PASO 5: Esperar confirmación
            String confirmation = input.readLine();
            if (confirmation == null || !confirmation.equals("KEY_EXCHANGE_OK")) {
                System.err.println("❌ Error en intercambio de claves: " + confirmation);
                return;
            }
            
            System.out.println("✓ Intercambio de claves completado exitosamente");
            System.out.println("\n🔒 Todos tus mensajes viajarán cifrados con AES/GCM-256");
            System.out.println("🔐 La clave AES se negoció de forma segura con RSA-2048\n");
            
            // Inicializar Seguretat con la clave AES negociada
            seguretat = new Seguretat(aesKey);
            
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
                    String msg;
                    while ((msg = input.readLine()) != null) {
                        System.out.println(msg);
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
                try {
                    String commandToSend = processCommand(userInput);
                    
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
                } catch (Exception e) {
                    System.err.println("❌ Error al procesar comando: " + e.getMessage());
                }
            }
            
            pingExecutor.shutdown();

        } catch (IOException e) {
            System.err.println("❌ Error de conexión: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Procesa comando del usuario
     * Si es MSG, cifra el contenido con AES
     */
    private static String processCommand(String userInput) throws Exception {
        if (userInput == null || userInput.trim().isEmpty()) {
            throw new IllegalArgumentException("El comando no puede estar vacío");
        }
        
        String[] parts = userInput.trim().split(" ", 2);
        String cmd = parts[0].toUpperCase();
        
        if (cmd.equals("MSG")) {
            if (parts.length < 2 || parts[1].trim().isEmpty()) {
                throw new IllegalArgumentException("Uso: MSG <mensaje>");
            }
            
            try {
                // Cifrar el mensaje con AES
                String plainMessage = parts[1];
                String encryptedMessage = seguretat.encrypt(plainMessage);
                
                // Retornar comando con mensaje cifrado
                return "MSG " + encryptedMessage;
                
            } catch (Exception e) {
                throw new Exception("Error al cifrar mensaje: " + e.getMessage(), e);
            }
        }
        
        // Para otros comandos, enviar tal cual (sin cifrar)
        return userInput;
    }
}
