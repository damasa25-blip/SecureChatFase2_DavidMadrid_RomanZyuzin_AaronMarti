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

                // 2. El paquete va dirigido a la IP del grupo, no a un cliente concreto
                DatagramPacket packet = new DatagramPacket(buf, buf.length, ipgrupo, port);

                // 3. ¡Lanzamos el mensaje al grupo!
                socket.send(packet);
                System.out.println("Mensaje enviado al grupo " + ipgrupo + ": " + missatge);
                // Esperamos 5 segundos antes de enviar el siguiente para que nos de tiempo a ver las capturas
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            System.err.println("Error al servidor Multicast: " + e.getMessage());
        }
    }

}
