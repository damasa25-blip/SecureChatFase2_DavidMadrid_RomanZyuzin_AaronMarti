package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita5;

public class Cliente extends Thread {
    String nom;
    String host;
    int port;
    
    public Cliente(String nom, String host, int port) {
        this.nom = nom;
        this.host = host;
        this.port = port;
    }

    public String getNom() {
        return nom;
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    
    @Override
    public void run() {
        ChatCliente.main(null, this.nom);
    }
}
