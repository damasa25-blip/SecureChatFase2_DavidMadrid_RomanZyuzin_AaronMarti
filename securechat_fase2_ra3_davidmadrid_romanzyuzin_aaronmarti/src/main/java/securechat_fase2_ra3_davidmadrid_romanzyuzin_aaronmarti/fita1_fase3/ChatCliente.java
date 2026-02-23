package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita1_fase3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatCliente {
    // Colores para una interfaz más clara
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;

        try (Socket socket = new Socket(host, port)) {
            System.out.println(ANSI_GREEN + "--- Conectado al SecureChat Server 1.0 ---" + ANSI_RESET);
            System.out.println(ANSI_YELLOW + "Comandos disponibles: LOGIN/nombre, MSG/texto, LIST, QUIT" + ANSI_RESET);

            // HILO DE LECTURA: Recibe lo que el Servidor envía
            new Thread(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String respuestaServidor;
                    while ((respuestaServidor = in.readLine()) != null) {
                        // El cliente solo imprime lo que el servidor le manda ya procesado
                        System.out.println("\n" + ANSI_CYAN + "[Servidor]: " + ANSI_RESET + respuestaServidor);
                        System.out.print("> ");
                    }
                } catch (IOException e) {
                    System.out.println(ANSI_YELLOW + "Conexión terminada con el servidor." + ANSI_RESET);
                }
            }).start();

            // HILO DE ESCRITURA: Envía lo que el usuario escribe
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.print("> ");
                String input = sc.nextLine();
                
                // Enviamos la línea tal cual al servidor. 
                // La clase 'Protocol' en el servidor será la que decida si es un LOGIN o un MSG.
                out.println(input);

                if (input.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error: No se pudo conectar con el servidor en " + host + ":" + port);
        }
    }
}