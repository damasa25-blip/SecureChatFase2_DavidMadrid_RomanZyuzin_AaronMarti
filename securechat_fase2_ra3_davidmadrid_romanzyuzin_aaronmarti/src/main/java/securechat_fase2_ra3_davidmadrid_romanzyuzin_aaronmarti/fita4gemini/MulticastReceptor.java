package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita4gemini;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceptor {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m"; 
    public static final String ANSI_CYAN = "\u001B[36m";   
    public static final String ANSI_GREEN = "\u001B[32m";  

    public static void main(String[] args) {
        int port = 12347;
        String groupIP = "224.0.0.1"; // Dirección de grupo estándar

        System.out.println(ANSI_YELLOW + "--- FITA 4: Receptor Multicast ---" + ANSI_RESET);

        try (MulticastSocket socket = new MulticastSocket(port)) {
            InetAddress group = InetAddress.getByName(groupIP);
            
            // Unirse al grupo
            socket.joinGroup(group);
            System.out.println(ANSI_YELLOW + "Unido al grupo: " + groupIP + " en el puerto " + port + ANSI_RESET);
            System.out.println(ANSI_YELLOW + "Esperando mensajes del grupo..." + ANSI_RESET);

            byte[] buf = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); // Espera mensaje

                String msg = new String(packet.getData(), 0, packet.getLength());
                System.out.println(ANSI_CYAN + "[Multicast de " + packet.getAddress().getHostAddress() + "]: " + ANSI_GREEN + msg + ANSI_RESET);
                
                if ("QUIT".equalsIgnoreCase(msg)) break; // Opción para salir
            }

            socket.leaveGroup(group); // Salir del grupo al terminar
        } catch (Exception e) {
            System.err.println("Error en Receptor Multicast: " + e.getMessage());
        }
    }
}