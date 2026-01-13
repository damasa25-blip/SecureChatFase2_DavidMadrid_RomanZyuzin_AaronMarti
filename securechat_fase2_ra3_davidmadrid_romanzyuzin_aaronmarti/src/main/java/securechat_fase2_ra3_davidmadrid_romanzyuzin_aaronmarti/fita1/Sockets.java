package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita1;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Sockets {

    // --- Codi ANSI per colors ---
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m"; // Color INFORMATIU
    public static final String ANSI_CYAN = "\u001B[36m";   // Color ENUNCIAT
    public static final String ANSI_GREEN = "\u001B[32m";  // Color RESPUESTA
    
    public static void main(String[] args) {    
        System.out.println("");
        System.out.println(ANSI_YELLOW + "FITA 1: Identificació d'IP Local i Loopback\n" + ANSI_RESET);
        
        try {
            // Obtenir l'adreça IP de la màquina local
            InetAddress localHost = InetAddress.getLocalHost();
            
            // Mostrar nom i adreça IP
            System.out.println(ANSI_CYAN + "Nom del dispositiu: " + ANSI_RESET + localHost.getHostName());
            System.out.println(ANSI_CYAN + "Adreça IP Local: " + ANSI_RESET + localHost.getHostAddress());
            
            // Comprovar si és loopback
            if (localHost.isLoopbackAddress()) {
                System.out.println(ANSI_GREEN + "L'adreça és de tipus loopback." + ANSI_RESET);
            } else {
                System.out.println(ANSI_GREEN + "L'adreça NO és de tipus loopback." + ANSI_RESET);
            }
            
            // Prova addicional amb loopback explícita
            InetAddress loopback = InetAddress.getLoopbackAddress();
            System.out.println("\n" + ANSI_CYAN + "Comprovació Loopback explícita:" + ANSI_RESET);
            System.out.println(ANSI_CYAN + "IP: " + ANSI_RESET + loopback.getHostAddress());
            System.out.println(ANSI_CYAN + "És loopback?: " + ANSI_RESET + loopback.isLoopbackAddress());

        } catch (UnknownHostException e) {
            System.err.println("No s'ha pogut determinar l'adreça IP local: " + e.getMessage());
        }
    }
}
