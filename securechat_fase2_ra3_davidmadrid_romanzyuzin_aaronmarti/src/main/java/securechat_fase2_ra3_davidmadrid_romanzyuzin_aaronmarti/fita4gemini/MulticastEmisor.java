package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita4gemini;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastEmisor {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m"; 
    public static final String ANSI_CYAN = "\u001B[36m";   
    public static final String ANSI_GREEN = "\u001B[32m";  

    public static void main(String[] args) {
        int port = 12347;
        String groupIP = "224.0.0.1";

        System.out.println(ANSI_YELLOW + "--- FITA 4: Emisor Multicast ---" + ANSI_RESET);

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(groupIP);
            
            String msg = "¡Atención a todos! Mensaje global del grupo de David, Roman y Aaron.";
            byte[] buf = msg.getBytes();

            // Enviamos el paquete a la IP del grupo
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
            socket.send(packet);

            System.out.println(ANSI_CYAN + "Mensaje enviado al grupo " + groupIP + "..." + ANSI_RESET);
            System.out.println(ANSI_GREEN + "Contenido: " + msg + ANSI_RESET);

        } catch (Exception e) {
            System.err.println("Error en Emisor Multicast: " + e.getMessage());
        }
    }
}