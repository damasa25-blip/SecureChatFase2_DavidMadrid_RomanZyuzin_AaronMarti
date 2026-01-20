package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fita5;

public class Client extends Thread {
    private ChatCliente chatCliente;

    public Client(ChatCliente chatCliente) {
        this.chatCliente = chatCliente;
    }

    @Override
    public void run() {
        chatCliente.main(null);
    }
}
