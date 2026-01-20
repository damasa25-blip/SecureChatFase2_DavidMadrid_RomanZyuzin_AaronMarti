package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPserver {

    public static void main(String[] args) {

        int port = 12346; // Port per al servidor UDP
        byte[] buf = new byte[1024];

        try (DatagramSocket socket = new DatagramSocket(port)) {
            while (true) {
                System.out.println("Servidor UDP escoltant al port " + port + "...");
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                System.out.println("Adreça del client: " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
                System.out.println("Missatge rebut: " + new String(packet.getData(), 0, packet.getLength()));

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
