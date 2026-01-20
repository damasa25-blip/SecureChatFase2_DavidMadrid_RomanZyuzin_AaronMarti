package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPclient {

    // Colors ANSI
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m"; 
    public static final String ANSI_CYAN = "\u001B[36m";   
    public static final String ANSI_GREEN = "\u001B[32m";

    public static void main(String[] args) {

        System.out.println("");
        String missatge = "Hola des del client UDP";
        byte[] buf = missatge.getBytes();
        int port = 12346;


    try (DatagramSocket socket = new DatagramSocket()) {
        InetAddress ip = InetAddress.getByName("localhost");
        DatagramPacket packetEnvio = new DatagramPacket(buf, buf.length, ip, port);
    
        socket.send(packetEnvio);
        System.out.println(ANSI_CYAN + "Mensaje enviado a " + ANSI_RESET + ip.getHostAddress() + ":" + port + ANSI_RESET );


        byte[] receptorBuf = new byte[1024];
        DatagramPacket respuestaPacket = new DatagramPacket(receptorBuf, receptorBuf.length);
    
        socket.receive(respuestaPacket); 
    

        String texto = new String(respuestaPacket.getData(), 0, respuestaPacket.getLength());
        System.out.println(ANSI_GREEN + "Notificación del servidor: " + ANSI_RESET + texto);

    } catch (Exception e) {
        System.err.println("Error en la comunicación: " + e.getMessage());
    }
    }
}
