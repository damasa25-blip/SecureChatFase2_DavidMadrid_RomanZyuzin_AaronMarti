package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita5;
import java.util.ArrayList;
import java.util.List;

public class Chat {
    public static void main(String[] args) {
        List<Cliente> Cliente = new ArrayList<>();
        Cliente.add(new Cliente("David", "localhost", 12345));
        Cliente.add(new Cliente("Roman", "localhost", 12345));
        Cliente.add(new Cliente("Aaron", "localhost", 12345));
        
        for (Cliente c : Cliente) {
            ChatCliente clientThread = new ChatCliente(c.getNom(), c.getHost(), c.getPort());
            clientThread.start();
        }
    }
    
}
