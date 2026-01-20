package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPserver {

    // Colors ANSI
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m"; 
    public static final String ANSI_CYAN = "\u001B[36m";   
    public static final String ANSI_GREEN = "\u001B[32m";

    public static void main(String[] args) {
        System.out.println("");
        int port = 12346; // Port per al servidor UDP
        byte[] buf = new byte[1024];

        try (DatagramSocket socket = new DatagramSocket(port)) {
            while (true) {
                System.out.println( ANSI_YELLOW + "Servidor UDP escoltant al port " + port + "..." + ANSI_RESET);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                System.out.println( ANSI_CYAN + "Adreça del client: " + ANSI_RESET + packet.getAddress().getHostAddress() + ":" + packet.getPort() + ANSI_RESET);
                System.out.println( ANSI_CYAN + "Missatge rebut: " +  ANSI_RESET + new String(packet.getData(), 0, packet.getLength()) + ANSI_RESET);

                String missatgeResposta = "Missatge rebut correctament";
                byte[] bufResposta = missatgeResposta.getBytes();
                DatagramPacket resposta = new DatagramPacket(bufResposta, bufResposta.length, packet.getAddress(), packet.getPort());
                socket.send(resposta);
            }
        } catch (Exception e) {
            System.err.println("Error al servidor UDP: " + e.getMessage());
        }
    }
}
