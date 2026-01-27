package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita5gemini;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClienteChat {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345)) {
            System.out.println("\u001B[32m--- Connectat al SecureChat ---\u001B[0m");

            // FIL DE LECTURA: Escolta missatges del servidor en paral·lel [cite: 594]
            new Thread(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println("\n\u001B[36m[Chat]: \u001B[0m" + msg);
                        System.out.print("> ");
                    }
                } catch (IOException e) {
                    System.out.println("S'ha perdut la connexió amb el servidor.");
                }
            }).start();

            // FIL D'ESCRIPTURA: Envia missatges al servidor [cite: 593]
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner sc = new Scanner(System.in);
            System.out.print("Introdueix el teu nom: ");
            String nom = sc.nextLine();

            while (true) {
                System.out.print("> ");
                String msg = sc.nextLine();
                if ("exit".equalsIgnoreCase(msg)) break;
                out.println(nom + ": " + msg);
            }

        } catch (IOException e) {
            System.err.println("Error de connexió: " + e.getMessage());
        }
    }
}
