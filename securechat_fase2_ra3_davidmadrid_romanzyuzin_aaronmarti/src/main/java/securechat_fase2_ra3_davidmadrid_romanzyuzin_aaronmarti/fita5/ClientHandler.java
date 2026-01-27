package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler extends Thread {
    // --- Codi ANSI per colors ---
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m"; // Color INFORMATIU
    public static final String ANSI_CYAN = "\u001B[36m";   // Color ENUNCIAT
    public static final String ANSI_GREEN = "\u001B[32m";  // Color RESPUESTA
    
    private Socket socket;
    private PrintWriter out;

    public ClientHandler(Socket socket, PrintWriter out) {
        this.socket = socket;
        this.out = out;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                System.out.println(ANSI_GREEN + "Retransmitiendo: " + mensaje + ANSI_RESET);
                ChatServidor.broadcast(mensaje); // Enviamos a todos
            }
        } catch (IOException e) {
            System.err.println(ANSI_YELLOW + "Cliente desconectado." + ANSI_RESET);
        } finally {

        }
    }
}


