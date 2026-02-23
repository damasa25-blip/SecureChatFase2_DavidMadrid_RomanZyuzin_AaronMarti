package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * FITA 1 - Cliente de prueba
 * Cliente simple para probar el servidor con arquitectura por capas
 */
public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;
    
    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado al servidor SecureChat");
            
            // Hilo para recibir mensajes del servidor
            Thread receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = input.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Desconectado del servidor");
                }
            });
            receiveThread.start();

            // Enviar mensajes al servidor
            String userInput;
            while (scanner.hasNextLine()) {
                userInput = scanner.nextLine();
                output.println(userInput);
                
                if (userInput.trim().toUpperCase().startsWith("QUIT")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
    }
}
