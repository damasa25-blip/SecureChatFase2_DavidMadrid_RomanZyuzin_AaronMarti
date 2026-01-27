package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita5gemini;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServidorEscalable {
    // Colores para la consola
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m"; 
    public static final String ANSI_CYAN = "\u001B[36m";   
    public static final String ANSI_GREEN = "\u001B[32m";  

    private static final int PORT = 12345;
    
    // El profesor recomienda CopyOnWriteArrayList para evitar ConcurrentModificationException 
    private static List<ClientHandler> clientes = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println(ANSI_YELLOW + "--- FITA 5: Servidor de Chat Escalable ---" + ANSI_RESET);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(ANSI_YELLOW + "Fil Acceptador: Esperant connexions al port " + PORT + "..." + ANSI_RESET);

            while (true) {
                // 1. Fil Acceptador: accepta la connexió [cite: 625, 626]
                Socket socket = serverSocket.accept();
                System.out.println(ANSI_CYAN + "[LOG]: Nou client connectat des de " + socket.getInetAddress() + ANSI_RESET);

                // 2. Crea el gestor i l'afegeix a la llista per al broadcast [cite: 626]
                ClientHandler handler = new ClientHandler(socket);
                clientes.add(handler);

                // 3. Arrenca un fil dedicat per a aquest client [cite: 626, 687]
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Error al servidor: " + e.getMessage());
        }
    }

    // El mètode Broadcast recorre la llista i envia el missatge a tots 
    public static void broadcast(String missatge, ClientHandler remitent) {
        for (ClientHandler cliente : clientes) {
            // Opcionalment, no enviem el missatge al mateix que l'ha escrit [cite: 641]
            if (cliente != remitent) {
                cliente.enviarMissatge(missatge);
            }
        }
    }

    // Gestiona l'eliminació de clients en cas de desconnexió [cite: 645, 681]
    public static void eliminarClient(ClientHandler handler) {
        clientes.remove(handler);
        System.out.println(ANSI_YELLOW + "[LOG]: Client desconnectat. Total actius: " + clientes.size() + ANSI_RESET);
    }
}

// Classe que gestiona cada fil de client [cite: 630, 632]
class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String missatge;
            // Bucle d'escolta: llegeix línies del client [cite: 630]
            while ((missatge = in.readLine()) != null) {
                System.out.println(ServidorEscalable.ANSI_CYAN + "Missatge rebut: " + ServidorEscalable.ANSI_RESET + missatge);
                
                // Reenviar a la resta de clients [cite: 634, 637]
                ServidorEscalable.broadcast(missatge, this);
            }
        } catch (IOException e) {
            // Gestió d'excepcions per evitar que el servidor caigui [cite: 644, 695]
        } finally {
            desconnectar();
        }
    }

    public void enviarMissatge(String missatge) {
        if (out != null) {
            out.println(missatge);
        }
    }

    private void desconnectar() {
        try {
            ServidorEscalable.eliminarClient(this);
            if (socket != null) socket.close();// Tancar recursos [cite: 682, 693]
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}