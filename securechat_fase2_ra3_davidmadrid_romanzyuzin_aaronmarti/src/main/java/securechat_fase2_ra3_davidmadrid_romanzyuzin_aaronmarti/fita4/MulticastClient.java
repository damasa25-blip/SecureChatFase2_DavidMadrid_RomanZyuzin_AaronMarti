package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita4;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastClient {

  public static void main(String[] args) {

        String missatge = "Hola des del client UDP";
        byte[] buf = missatge.getBytes();
        int port = 12347;


    try (MulticastSocket socket = new MulticastSocket(12347)) { // 1. Escuchar en este puerto
    InetAddress ipgrupo = InetAddress.getByName("230.0.0.1");
    
    // 2. Unirse al grupo (sintonizar la emisora)
    socket.joinGroup(ipgrupo);
    System.out.println("Cliente suscrito al grupo 230.0.0.1. Esperando mensajes...");

    byte[] receptorBuf = new byte[1024];

    // 3. Bucle para recibir múltiples mensajes
    while (true) {
        DatagramPacket packet = new DatagramPacket(receptorBuf, receptorBuf.length);
        
        // El programa se detiene aquí hasta que el EMISOR envíe algo
        socket.receive(packet); 

        String texto = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Mensaje recibido del emisor: " + texto);
    }

    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
    }
    
}
