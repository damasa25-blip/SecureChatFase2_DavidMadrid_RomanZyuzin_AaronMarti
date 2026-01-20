package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatCliente {
 // --- Codi ANSI per colors ---
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m"; // Color INFORMATIU
    public static final String ANSI_CYAN = "\u001B[36m";   // Color ENUNCIAT
    public static final String ANSI_GREEN = "\u001B[32m";  // Color RESPUESTA

    public static void main(String[] args) {
        System.out.println("");
        int port = 12345; // Port del servidor TCP
        String host = "localhost"; // Adreça del servidor (localhost per a proves locals
        System.out.println(ANSI_YELLOW + "--- FITA 2: Client TCP ---" + ANSI_RESET);

        try (Socket socket = new Socket(host, port); 
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); 
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println(ANSI_CYAN + "Connectat al servidor: " + ANSI_RESET + host + ":" + port + ANSI_RESET);

            // Enviar missatge 
            String missatgeEnviament = "Hola servidor, sóc el grup de David, Roman i Aaron!";
            out.println(missatgeEnviament);


            // Rebre resposta
            String resposta = in.readLine();
            System.out.println(ANSI_GREEN + "Resposta del servidor: " + ANSI_RESET + resposta + ANSI_RESET);
        } catch (IOException e) {
            System.err.println("Error al client: " + e.getMessage());
        }
    }
}
