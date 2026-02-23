package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita1;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FITA 1 - Refactorización por Capas
 * Clase UserRepository - Gestión de usuarios
 * Responsabilidad: Gestión de usuarios conectados, nombres de usuario, estado de sesión
 */
public class UserRepository {
    // Estructura thread-safe para almacenar usuarios
    private Map<String, ClientHandler> connectedUsers;

    public UserRepository() {
        this.connectedUsers = new ConcurrentHashMap<>();
    }

    /**
     * Añade un usuario al repositorio
     */
    public synchronized void addUser(String username, ClientHandler handler) {
        connectedUsers.put(username, handler);
        System.out.println("Usuario registrado: " + username + 
            " (Total: " + connectedUsers.size() + ")");
    }

    /**
     * Elimina un usuario del repositorio
     */
    public synchronized void removeUser(String username) {
        if (username != null && connectedUsers.remove(username) != null) {
            System.out.println("Usuario eliminado: " + username + 
                " (Total: " + connectedUsers.size() + ")");
        }
    }

    /**
     * Verifica si un usuario existe
     */
    public boolean userExists(String username) {
        return connectedUsers.containsKey(username);
    }

    /**
     * Obtiene la lista de usuarios conectados
     */
    public String getUserList() {
        if (connectedUsers.isEmpty()) {
            return "  (No hay usuarios conectados)";
        }

        StringBuilder list = new StringBuilder();
        int count = 1;
        for (String username : connectedUsers.keySet()) {
            list.append("  ").append(count++).append(". ").append(username).append("\n");
        }
        return list.toString();
    }

    /**
     * Envía un mensaje a todos los usuarios conectados
     * @param message Mensaje a enviar
     * @param excludeUser Usuario a excluir del broadcast (puede ser null)
     */
    public void broadcastMessage(String message, String excludeUser) {
        for (Map.Entry<String, ClientHandler> entry : connectedUsers.entrySet()) {
            if (excludeUser == null || !entry.getKey().equals(excludeUser)) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    /**
     * Obtiene el número de usuarios conectados
     */
    public int getUserCount() {
        return connectedUsers.size();
    }
}
