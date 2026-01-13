package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPserver {
    public static void main(String[] args) {
        System.out.println("UDP Server placeholder");
        DatagramSocket socket = null;
        DatagramPacket packet = null;
        int port = 12345; // Port per al servidor UDP
        String missatge = "Hola des del servidor UDP";
        try {
            socket = new DatagramSocket(port);
            System.out.println("Servidor UDP escoltant al port " + port + "...");
            
        } catch (Exception e) {
            System.err.println("Error al servidor UDP: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
