package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita1;

/**
 * FITA 1 - Refactorización por Capas
 * Clase Protocol - Procesamiento de comandos
 * Responsabilidad: Interpretar y procesar las comandos del cliente (LOGIN, MSG, LIST, QUIT)
 * Esta capa es independiente de la lógica de sockets
 */
public class Protocol {
    private UserRepository userRepository;

    public Protocol(UserRepository repository) {
        this.userRepository = repository;
    }

    /**
     * Procesa un comando recibido del cliente
     * @param command Comando recibido
     * @param handler Handler del cliente que envió el comando
     * @return Respuesta a enviar al cliente
     */
    public String processCommand(String command, ClientHandler handler) {
        if (command == null || command.trim().isEmpty()) {
            return "ERROR: Comando vacío";
        }

        String[] parts = command.trim().split(" ", 2);
        String cmd = parts[0].toUpperCase();

        switch (cmd) {
            case "LOGIN":
                return handleLogin(parts, handler);
            
            case "MSG":
                return handleMessage(parts, handler);
            
            case "LIST":
                return handleList(handler);
            
            case "QUIT":
                return handleQuit(handler);
            
            default:
                return "ERROR: Comando desconocido. Use LOGIN, MSG, LIST o QUIT";
        }
    }

    /**
     * Procesa el comando LOGIN
     */
    private String handleLogin(String[] parts, ClientHandler handler) {
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            return "ERROR: Uso correcto: LOGIN <nombre>";
        }

        String username = parts[1].trim();

        // Verificar si el usuario ya ha iniciado sesión
        if (handler.getUsername() != null) {
            return "ERROR: Ya has iniciado sesión como " + handler.getUsername();
        }

        // Verificar si el nombre de usuario ya está en uso
        if (userRepository.userExists(username)) {
            return "ERROR: El nombre de usuario ya está en uso";
        }

        // Registrar usuario
        userRepository.addUser(username, handler);
        handler.setUsername(username);

        // Notificar a otros usuarios
        String notification = ">>> " + username + " se ha conectado";
        userRepository.broadcastMessage(notification, username);

        return "OK: Sesión iniciada como " + username;
    }

    /**
     * Procesa el comando MSG
     */
    private String handleMessage(String[] parts, ClientHandler handler) {
        if (handler.getUsername() == null) {
            return "ERROR: Debes hacer LOGIN primero";
        }

        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            return "ERROR: Uso correcto: MSG <mensaje>";
        }

        String message = parts[1].trim();
        String formattedMessage = "[" + handler.getUsername() + "]: " + message;

        // Broadcast del mensaje a todos los usuarios
        userRepository.broadcastMessage(formattedMessage, null);

        return "OK: Mensaje enviado";
    }

    /**
     * Procesa el comando LIST
     */
    private String handleList(ClientHandler handler) {
        if (handler.getUsername() == null) {
            return "ERROR: Debes hacer LOGIN primero";
        }

        String userList = userRepository.getUserList();
        return "Usuarios conectados:\n" + userList;
    }

    /**
     * Procesa el comando QUIT
     */
    private String handleQuit(ClientHandler handler) {
        String username = handler.getUsername();
        
        if (username != null) {
            // Notificar a otros usuarios
            String notification = ">>> " + username + " se ha desconectado";
            userRepository.broadcastMessage(notification, username);
        }

        return "OK: Desconectando... Hasta pronto!";
    }
}
