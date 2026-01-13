package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita3;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPclient {
    public static void main(String[] args) {

        String missatge = "Hola des del client UDP";
        byte[] buf = missatge.getBytes();
        DatagramSocket socket = null;
        InetAddress ip = null;
        try {
          ip = InetAddress.getByName("localhost");  
        } catch (Exception e) {
            System.err.println("Error al servidor UDP: " + e.getMessage());
        }
        
        int port = 12345; // Port per al servidor UDP
        DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, port);

        try (DatagramSocket datagramsocket = new DatagramSocket()){
            datagramsocket.send(packet);

        } catch (Exception e) {
            System.err.println("Error al servidor UDP: " + e.getMessage());
        }
    }
}
