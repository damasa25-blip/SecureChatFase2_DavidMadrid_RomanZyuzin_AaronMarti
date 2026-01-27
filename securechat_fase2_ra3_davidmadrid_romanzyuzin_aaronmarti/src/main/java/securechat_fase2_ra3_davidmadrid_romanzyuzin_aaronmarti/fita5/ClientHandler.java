package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler extends Thread {
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
                System.out.println("Retransmitiendo: " + mensaje);
                ChatServidor.broadcast(mensaje); // Enviamos a todos
            }
        } catch (IOException e) {
            System.err.println("Cliente desconectado.");
        } finally {

        }
    }
}


