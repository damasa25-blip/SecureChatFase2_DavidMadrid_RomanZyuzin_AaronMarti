package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatCliente {
    // --- Codi ANSI per colors ---
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m"; // Color INFORMATIU
    public static final String ANSI_CYAN = "\u001B[36m";   // Color ENUNCIAT
    public static final String ANSI_GREEN = "\u001B[32m";  // Color RESPUESTA

    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;

        System.out.println(ANSI_YELLOW + "Cliente Abierto" + ANSI_RESET);
        Scanner sc = new Scanner(System.in);
        boolean conectado = false;

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                
            if(!conectado){
                System.out.println(ANSI_GREEN + "Connectat al servidor de chat a " + host + ":" + port + ANSI_RESET);
                conectado = true;
            }
            System.out.println("Introdueix el teu nom amb el comando LOGIN (ex: LOGIN/nom): ");
            // HILO PARA RECIBIR (Lectura del servidor)
            new Thread(() -> {
                try {
                    String r;
                    while ((r = in.readLine()) != null) {
                        System.out.println("\n" + r);
                    }
                } catch (IOException e) { 
                    System.err.println("Conexión cerrada.");
                }
            }).start();

            // HILO PRINCIPAL PARA ENVIAR (Escritura del usuario)
            while (true) {
                String msg = sc.nextLine();
                out.println(msg);

            }

        } catch (IOException e) {
            System.err.println("No se pudo conectar al servidor.");
        }
    }
}