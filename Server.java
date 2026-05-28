import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Server.java
 * Multi-Client Chat Application - Server Program
 * Protocol: TCP | Port: 5000
 * Handles multiple clients using multithreading
 */
public class Server {

    // Port number for the server
    public static final int PORT = 5000;

    // Thread-safe map of connected clients: username -> ClientHandler
    public static Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();

    // Predefined users: username -> SHA-256 hashed password (Now Thread-Safe)
    public static Map<String, String> userDatabase = new ConcurrentHashMap<>();

    // Log file writer
    public static FileWriter logWriter;

    public static void main(String[] args) {
        // Initialize predefined users with SHA-256 hashed passwords
        userDatabase.put("ammar",    Security.hashPassword("1234"));
        userDatabase.put("abdullah", Security.hashPassword("1234"));
        userDatabase.put("faizan",   Security.hashPassword("1234"));

        // Initialize log file
        try {
            logWriter = new FileWriter("server.log", true); // append mode
            log("SERVER STARTED on port " + PORT);
        } catch (IOException e) {
            System.out.println("[ERROR] Could not create log file: " + e.getMessage());
        }

        System.out.println("==============================================");
        System.out.println("   Multi-Client Chat Server Started");
        System.out.println("   Listening on port: " + PORT);
        System.out.println("   Protocol: TCP | Host: localhost");
        System.out.println("==============================================");

        // Start accepting client connections
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[SERVER] New connection from: " + clientSocket.getInetAddress().getHostAddress());

                    // Create a new thread for each client
                    ClientHandler handler = new ClientHandler(clientSocket);
                    Thread thread = new Thread(handler);
                    thread.start();

                } catch (IOException e) {
                    log("[ERROR] Failed to accept client: " + e.getMessage());
                    System.out.println("[ERROR] Failed to accept client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log("[ERROR] Server could not start: " + e.getMessage());
            System.out.println("[ERROR] Server could not start on port " + PORT + ": " + e.getMessage());
        }
    }

    /**
     * Broadcast a message to ALL connected clients
     */
    public static void broadcastToAll(String message, String senderUsername) {
        String formatted = "[BROADCAST][" + senderUsername + "]: " + message;
        System.out.println(formatted);
        log(formatted);
        for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
            entry.getValue().sendMessage(formatted);
        }
    }

    /**
     * Send a private message to a specific client
     */
    public static void sendPrivateMessage(String message, String senderUsername, String targetUsername) {
        ClientHandler target = connectedClients.get(targetUsername);
        if (target != null) {
            String formatted = "[PRIVATE][" + senderUsername + " -> " + targetUsername + "]: " + message;
            target.sendMessage(formatted);
            // Also show to sender
            connectedClients.get(senderUsername).sendMessage(formatted);
            log(formatted);
        } else {
            // Notify sender that target user not found
            ClientHandler sender = connectedClients.get(senderUsername);
            if (sender != null) {
                sender.sendMessage("[SERVER] User '" + targetUsername + "' not found or not connected.");
            }
        }
    }

    /**
     * Remove a client from connected list on disconnect
     */
    public static void removeClient(String username) {
        connectedClients.remove(username);
        log("[DISCONNECT] User '" + username + "' disconnected. Active clients: " + connectedClients.size());
        System.out.println("[SERVER] User '" + username + "' disconnected. Active clients: " + connectedClients.size());
        broadcastSystemMessage("User '" + username + "' has left the chat.");
    }

    /**
     * Broadcast a system notification to all clients
     */
    public static void broadcastSystemMessage(String message) {
        String formatted = "[SERVER NOTIFICATION] " + message;
        for (ClientHandler handler : connectedClients.values()) {
            handler.sendMessage(formatted);
        }
    }

    /**
     * Log messages to server.log file with timestamp
     * Thread-Safe: added synchronized keyword
     */
    public static synchronized void log(String message) {
        try {
            if (logWriter != null) {
                String timestamp = new java.util.Date().toString();
                logWriter.write("[" + timestamp + "] " + message + "\n");
                logWriter.flush();
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Logging failed: " + e.getMessage());
        }
    }
}

// Changes made in new code: * Changed userDatabase to a ConcurrentHashMap.
// Added the synchronized keyword to the log method.

//--------------------------------------------------------------
//           >> Old Code <<
// import java.io.*;
// import java.net.*;
// import java.util.*;
// import java.util.concurrent.*;

// /**
//  * Server.java
//  * Multi-Client Chat Application - Server Program
//  * Protocol: TCP | Port: 5000
//  * Handles multiple clients using multithreading
//  */
// public class Server {

//     // Port number for the server
//     public static final int PORT = 5000;

//     // Thread-safe map of connected clients: username -> ClientHandler
//     public static Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();

//     // Predefined users: username -> SHA-256 hashed password
//     public static Map<String, String> userDatabase = new HashMap<>();

//     // Log file writer
//     public static FileWriter logWriter;

//     public static void main(String[] args) {
//         // Initialize predefined users with SHA-256 hashed passwords
//         userDatabase.put("ammar",    Security.hashPassword("1234"));
//         userDatabase.put("abdullah", Security.hashPassword("1234"));
//         userDatabase.put("faizan",   Security.hashPassword("1234"));

//         // Initialize log file
//         try {
//             logWriter = new FileWriter("server.log", true); // append mode
//             log("SERVER STARTED on port " + PORT);
//         } catch (IOException e) {
//             System.out.println("[ERROR] Could not create log file: " + e.getMessage());
//         }

//         System.out.println("==============================================");
//         System.out.println("   Multi-Client Chat Server Started");
//         System.out.println("   Listening on port: " + PORT);
//         System.out.println("   Protocol: TCP | Host: localhost");
//         System.out.println("==============================================");

//         // Start accepting client connections
//         try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//             while (true) {
//                 try {
//                     Socket clientSocket = serverSocket.accept();
//                     System.out.println("[SERVER] New connection from: " + clientSocket.getInetAddress().getHostAddress());

//                     // Create a new thread for each client
//                     ClientHandler handler = new ClientHandler(clientSocket);
//                     Thread thread = new Thread(handler);
//                     thread.start();

//                 } catch (IOException e) {
//                     log("[ERROR] Failed to accept client: " + e.getMessage());
//                     System.out.println("[ERROR] Failed to accept client: " + e.getMessage());
//                 }
//             }
//         } catch (IOException e) {
//             log("[ERROR] Server could not start: " + e.getMessage());
//             System.out.println("[ERROR] Server could not start on port " + PORT + ": " + e.getMessage());
//         }
//     }

//     /**
//      * Broadcast a message to ALL connected clients
//      */
//     public static void broadcastToAll(String message, String senderUsername) {
//         String formatted = "[BROADCAST][" + senderUsername + "]: " + message;
//         System.out.println(formatted);
//         log(formatted);
//         for (Map.Entry<String, ClientHandler> entry : connectedClients.entrySet()) {
//             entry.getValue().sendMessage(formatted);
//         }
//     }

//     /**
//      * Send a private message to a specific client
//      */
//     public static void sendPrivateMessage(String message, String senderUsername, String targetUsername) {
//         ClientHandler target = connectedClients.get(targetUsername);
//         if (target != null) {
//             String formatted = "[PRIVATE][" + senderUsername + " -> " + targetUsername + "]: " + message;
//             target.sendMessage(formatted);
//             // Also show to sender
//             connectedClients.get(senderUsername).sendMessage(formatted);
//             log(formatted);
//         } else {
//             // Notify sender that target user not found
//             ClientHandler sender = connectedClients.get(senderUsername);
//             if (sender != null) {
//                 sender.sendMessage("[SERVER] User '" + targetUsername + "' not found or not connected.");
//             }
//         }
//     }

//     /**
//      * Remove a client from connected list on disconnect
//      */
//     public static void removeClient(String username) {
//         connectedClients.remove(username);
//         log("[DISCONNECT] User '" + username + "' disconnected. Active clients: " + connectedClients.size());
//         System.out.println("[SERVER] User '" + username + "' disconnected. Active clients: " + connectedClients.size());
//         broadcastSystemMessage("User '" + username + "' has left the chat.");
//     }

//     /**
//      * Broadcast a system notification to all clients
//      */
//     public static void broadcastSystemMessage(String message) {
//         String formatted = "[SERVER NOTIFICATION] " + message;
//         for (ClientHandler handler : connectedClients.values()) {
//             handler.sendMessage(formatted);
//         }
//     }

//     /**
//      * Log messages to server.log file with timestamp
//      */
//     public static void log(String message) {
//         try {
//             if (logWriter != null) {
//                 String timestamp = new java.util.Date().toString();
//                 logWriter.write("[" + timestamp + "] " + message + "\n");
//                 logWriter.flush();
//             }
//         } catch (IOException e) {
//             System.out.println("[ERROR] Logging failed: " + e.getMessage());
//         }
//     }
// }
