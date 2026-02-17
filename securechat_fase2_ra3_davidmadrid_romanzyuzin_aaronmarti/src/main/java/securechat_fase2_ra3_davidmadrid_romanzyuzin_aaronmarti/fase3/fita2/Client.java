package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita2;

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
 * FITA 2 - Cliente de prueba con heartbeat automático
 * Cliente que envía pings automáticos cada 5 segundos
 */
public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12346;
    private static final long PING_INTERVAL = 5000; // 5 segundos
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado al servidor SecureChat (FITA 2)");
            System.out.println("Heartbeat automático activado\n");
            
            // Executor para enviar pings automáticos
            ScheduledExecutorService pingExecutor = Executors.newSingleThreadScheduledExecutor();
            final boolean[] loggedIn = {false};
            
            // Enviar PING cada 5 segundos después de hacer LOGIN
            pingExecutor.scheduleAtFixedRate(() -> {
                if (loggedIn[0]) {
                    output.println("PING");
                }
            }, PING_INTERVAL, PING_INTERVAL, TimeUnit.MILLISECONDS);
            
            // Hilo para recibir mensajes del servidor
            Thread receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = input.readLine()) != null) {
                        // Filtrar respuestas PONG para no saturar la consola
                        if (!message.startsWith("PONG")) {
                            System.out.println(message);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("\nDesconectado del servidor");
                }
            });
            receiveThread.start();

            // Enviar mensajes al servidor
            String userInput;
            while (scanner.hasNextLine()) {
                userInput = scanner.nextLine();
                output.println(userInput);
                
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
}
