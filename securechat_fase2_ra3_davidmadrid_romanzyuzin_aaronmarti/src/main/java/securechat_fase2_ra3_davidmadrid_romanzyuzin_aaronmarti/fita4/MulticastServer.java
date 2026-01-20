package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita4;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastServer {

    public static void main(String[] args) {
        int port = 12347;
        byte[] buf = new byte[1024];

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress ipgrupo = InetAddress.getByName("230.0.0.1");

            while (true) {
                String missatge = "Noticia importante para todos los clientes!";
                buf = missatge.getBytes();

                DatagramPacket packet = new DatagramPacket(buf, buf.length, ipgrupo, port);

                socket.send(packet);
                System.out.println("Mensaje enviado al grupo " + ipgrupo + ": " + missatge);
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            System.err.println("Error al servidor Multicast: " + e.getMessage());
        }
    }

}
