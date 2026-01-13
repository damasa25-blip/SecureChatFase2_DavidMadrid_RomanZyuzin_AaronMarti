package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPserver {

    // --- Codi ANSI per colors ---
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m"; // Color INFORMATIU
    public static final String ANSI_CYAN = "\u001B[36m";   // Color ENUNCIAT
    public static final String ANSI_GREEN = "\u001B[32m";  // Color RESPUESTA

    public static void main(String[] args) {
        System.out.println("");
        int port = 12345; // Port per al servidor TCP
        System.out.println(ANSI_YELLOW + "--- FITA 2: Servidor TCP (Eco) ---" + ANSI_RESET);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(ANSI_YELLOW + "Servidor escoltant al port " + port + "..." + ANSI_RESET);

            // Acceptar la connexió del client 
            try (Socket socket = serverSocket.accept(); // Espera una connexió entrant
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Per llegir dades del client
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) { // Per enviar dades al client

                System.out.println(ANSI_CYAN + "Client connectat des de: " + ANSI_RESET + socket.getInetAddress() + ANSI_RESET); // Mostrar adreça del client

                // Llegir missatge del client 
                String missatge = in.readLine(); // Espera un missatge del client
                System.out.println(ANSI_CYAN + "Missatge rebut: " + ANSI_RESET + missatge + ANSI_RESET); // Mostrar missatge rebut

                // Retornar el missatge (Eco)
                out.println(missatge); // Enviar resposta al client
                System.out.println(ANSI_GREEN + "Resposta enviada al client." + ANSI_RESET); // Confirmació d'enviament
            }
        } catch (IOException e) {
            System.err.println("Error al servidor: " + e.getMessage());
        }
    }
}
