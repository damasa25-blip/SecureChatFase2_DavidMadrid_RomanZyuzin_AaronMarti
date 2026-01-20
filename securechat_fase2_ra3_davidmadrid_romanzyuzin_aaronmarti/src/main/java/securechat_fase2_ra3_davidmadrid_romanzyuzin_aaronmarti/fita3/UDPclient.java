package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPclient {

    public static void main(String[] args) {

        String missatge = "Hola des del client UDP";
        byte[] buf = missatge.getBytes();
        int port = 12346;


    try (DatagramSocket socket = new DatagramSocket()) {
        InetAddress ip = InetAddress.getByName("localhost");
        DatagramPacket packetEnvio = new DatagramPacket(buf, buf.length, ip, port);
    

        socket.send(packetEnvio);
        System.out.println("Mensaje enviado...");


        byte[] receptorBuf = new byte[1024];
        DatagramPacket respuestaPacket = new DatagramPacket(receptorBuf, receptorBuf.length);
    
        socket.receive(respuestaPacket); 
    

        String texto = new String(respuestaPacket.getData(), 0, respuestaPacket.getLength());
        System.out.println("Notificación del servidor: " + texto);

    } catch (Exception e) {
        System.err.println("Error en la comunicación: " + e.getMessage());
    }
    }
}
