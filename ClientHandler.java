import java.io.*;
import java.net.*;

/**
 * ClientHandler.java
 * Runs as a separate thread for each connected client
 * Handles: authentication, message routing, disconnection
 */
public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username;
    private String clientIP;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientIP = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            // Set up input/output streams
            input  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            // Step 1: Authenticate the client
            if (!authenticate()) {
                socket.close();
                return;
            }

            // Step 2: Notify everyone this user joined
            Server.log("[CONNECT] User '" + username + "' connected from IP: " + clientIP);
            Server.broadcastSystemMessage("User '" + username + "' has joined the chat!");
            sendMessage("[SERVER] Welcome " + username + "! Commands: /broadcast <msg> | /pm <user> <msg> | /quit");

            // Step 3: Listen for messages
            String message;
            while ((message = input.readLine()) != null) {
                message = message.trim();

                // Input validation
                if (!Security.isValidInput(message)) continue;

                Server.log("[MSG][" + username + "]: " + message);

                if (message.equalsIgnoreCase("/quit")) {
                    // Graceful disconnect
                    sendMessage("[SERVER] Goodbye " + username + "!");
                    break;

                } else if (message.startsWith("/broadcast ")) {
                    // Broadcast to all clients
                    String broadcastMsg = message.substring(11).trim();
                    if (Security.isValidInput(broadcastMsg)) {
                        Server.broadcastToAll(broadcastMsg, username);
                    } else {
                        sendMessage("[SERVER] Broadcast message cannot be empty.");
                    }

                } else if (message.startsWith("/pm ")) {
                    // Private message: /pm <username> <message>
                    String[] parts = message.split(" ", 3);
                    if (parts.length < 3) {
                        sendMessage("[SERVER] Usage: /pm <username> <message>");
                    } else {
                        String targetUser = parts[1].trim();
                        String privateMsg = parts[2].trim();
                        if (targetUser.equals(username)) {
                            sendMessage("[SERVER] You cannot send a private message to yourself.");
                        } else if (Security.isValidInput(privateMsg)) {
                            Server.sendPrivateMessage(privateMsg, username, targetUser);
                        } else {
                            sendMessage("[SERVER] Private message cannot be empty.");
                        }
                    }

                } else {
                    // Regular text message — send as broadcast by default
                    Server.broadcastToAll(message, username);
                }
            }

        } catch (IOException e) {
            Server.log("[ERROR] Connection error for '" + username + "': " + e.getMessage());
            System.out.println("[ERROR] Connection lost for: " + (username != null ? username : clientIP));
        } finally {
            // Clean up on disconnect
            if (username != null) {
                Server.removeClient(username);
            }
            try {
                socket.close();
            } catch (IOException e) {
                Server.log("[ERROR] Failed to close socket: " + e.getMessage());
            }
        }
    }

    /**
     * Authenticate the client with username and password
     * Allows 3 attempts before closing connection
     */
    private boolean authenticate() throws IOException {
        sendMessage("========================================");
        sendMessage("   Welcome to Multi-Client Chat App");
        sendMessage("   Protocol: TCP | Port: 5000");
        sendMessage("========================================");

        int attempts = 0;
        int maxAttempts = 3;

        while (attempts < maxAttempts) {
            sendMessage("Enter username:");
            String enteredUsername = input.readLine();

            sendMessage("Enter password:");
            String enteredPassword = input.readLine();

            // Validate inputs
            if (!Security.isValidInput(enteredUsername) || !Security.isValidInput(enteredPassword)) {
                sendMessage("[ERROR] Username and password cannot be empty.");
                attempts++;
                continue;
            }

            enteredUsername = enteredUsername.trim();
            String hashedInput = Security.hashPassword(enteredPassword.trim());

            // Check credentials
            if (Server.userDatabase.containsKey(enteredUsername) &&
                Server.userDatabase.get(enteredUsername).equals(hashedInput)) {

                // Check if already logged in
                if (Server.connectedClients.containsKey(enteredUsername)) {
                    sendMessage("[ERROR] User '" + enteredUsername + "' is already logged in.");
                    Server.log("[AUTH FAILED] Duplicate login attempt for: " + enteredUsername);
                    return false;
                }

                // Success
                this.username = enteredUsername;
                Server.connectedClients.put(username, this);
                sendMessage("[SUCCESS] Authentication successful! Hello, " + username + "!");
                Server.log("[AUTH SUCCESS] User '" + username + "' authenticated from IP: " + clientIP);
                return true;

            } else {
                attempts++;
                int remaining = maxAttempts - attempts;
                sendMessage("[ERROR] Invalid credentials. " + remaining + " attempt(s) remaining.");
                Server.log("[AUTH FAILED] Failed login attempt from IP: " + clientIP + " (attempt " + attempts + ")");
            }
        }

        sendMessage("[BLOCKED] Too many failed attempts. Connection closed.");
        Server.log("[BLOCKED] IP " + clientIP + " blocked after " + maxAttempts + " failed attempts.");
        return false;
    }

    /**
     * Send a message to this client
     */
    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }
}

//Changes made in new code: * Completely removed the catch (SocketTimeoutException e) block to clean up the code, keeping the general IOException handler.

//---------------------------------------------------------------
//           >> Old Code <<

// import java.io.*;
// import java.net.*;

// /**
//  * ClientHandler.java
//  * Runs as a separate thread for each connected client
//  * Handles: authentication, message routing, disconnection
//  */
// public class ClientHandler implements Runnable {

//     private Socket socket;
//     private BufferedReader input;
//     private PrintWriter output;
//     private String username;
//     private String clientIP;

//     public ClientHandler(Socket socket) {
//         this.socket = socket;
//         this.clientIP = socket.getInetAddress().getHostAddress();
//     }

//     @Override
//     public void run() {
//         try {
//             // Set up input/output streams
//             input  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//             output = new PrintWriter(socket.getOutputStream(), true);

//             // Step 1: Authenticate the client
//             if (!authenticate()) {
//                 socket.close();
//                 return;
//             }

//             // Step 2: Notify everyone this user joined
//             Server.log("[CONNECT] User '" + username + "' connected from IP: " + clientIP);
//             Server.broadcastSystemMessage("User '" + username + "' has joined the chat!");
//             sendMessage("[SERVER] Welcome " + username + "! Commands: /broadcast <msg> | /pm <user> <msg> | /quit");

//             // Step 3: Listen for messages
//             String message;
//             while ((message = input.readLine()) != null) {
//                 message = message.trim();

//                 // Input validation
//                 if (!Security.isValidInput(message)) continue;

//                 Server.log("[MSG][" + username + "]: " + message);

//                 if (message.equalsIgnoreCase("/quit")) {
//                     // Graceful disconnect
//                     sendMessage("[SERVER] Goodbye " + username + "!");
//                     break;

//                 } else if (message.startsWith("/broadcast ")) {
//                     // Broadcast to all clients
//                     String broadcastMsg = message.substring(11).trim();
//                     if (Security.isValidInput(broadcastMsg)) {
//                         Server.broadcastToAll(broadcastMsg, username);
//                     } else {
//                         sendMessage("[SERVER] Broadcast message cannot be empty.");
//                     }

//                 } else if (message.startsWith("/pm ")) {
//                     // Private message: /pm <username> <message>
//                     String[] parts = message.split(" ", 3);
//                     if (parts.length < 3) {
//                         sendMessage("[SERVER] Usage: /pm <username> <message>");
//                     } else {
//                         String targetUser = parts[1].trim();
//                         String privateMsg = parts[2].trim();
//                         if (targetUser.equals(username)) {
//                             sendMessage("[SERVER] You cannot send a private message to yourself.");
//                         } else if (Security.isValidInput(privateMsg)) {
//                             Server.sendPrivateMessage(privateMsg, username, targetUser);
//                         } else {
//                             sendMessage("[SERVER] Private message cannot be empty.");
//                         }
//                     }

//                 } else {
//                     // Regular text message — send as broadcast by default
//                     Server.broadcastToAll(message, username);
//                 }
//             }

//         } catch (SocketTimeoutException e) {
//             Server.log("[TIMEOUT] User '" + username + "' connection timed out.");
//             System.out.println("[TIMEOUT] User '" + username + "' timed out.");
//         } catch (IOException e) {
//             Server.log("[ERROR] Connection error for '" + username + "': " + e.getMessage());
//             System.out.println("[ERROR] Connection lost for: " + (username != null ? username : clientIP));
//         } finally {
//             // Clean up on disconnect
//             if (username != null) {
//                 Server.removeClient(username);
//             }
//             try {
//                 socket.close();
//             } catch (IOException e) {
//                 Server.log("[ERROR] Failed to close socket: " + e.getMessage());
//             }
//         }
//     }

//     /**
//      * Authenticate the client with username and password
//      * Allows 3 attempts before closing connection
//      */
//     private boolean authenticate() throws IOException {
//         sendMessage("========================================");
//         sendMessage("   Welcome to Multi-Client Chat App");
//         sendMessage("   Protocol: TCP | Port: 5000");
//         sendMessage("========================================");

//         int attempts = 0;
//         int maxAttempts = 3;

//         while (attempts < maxAttempts) {
//             sendMessage("Enter username:");
//             String enteredUsername = input.readLine();

//             sendMessage("Enter password:");
//             String enteredPassword = input.readLine();

//             // Validate inputs
//             if (!Security.isValidInput(enteredUsername) || !Security.isValidInput(enteredPassword)) {
//                 sendMessage("[ERROR] Username and password cannot be empty.");
//                 attempts++;
//                 continue;
//             }

//             enteredUsername = enteredUsername.trim();
//             String hashedInput = Security.hashPassword(enteredPassword.trim());

//             // Check credentials
//             if (Server.userDatabase.containsKey(enteredUsername) &&
//                 Server.userDatabase.get(enteredUsername).equals(hashedInput)) {

//                 // Check if already logged in
//                 if (Server.connectedClients.containsKey(enteredUsername)) {
//                     sendMessage("[ERROR] User '" + enteredUsername + "' is already logged in.");
//                     Server.log("[AUTH FAILED] Duplicate login attempt for: " + enteredUsername);
//                     return false;
//                 }

//                 // Success
//                 this.username = enteredUsername;
//                 Server.connectedClients.put(username, this);
//                 sendMessage("[SUCCESS] Authentication successful! Hello, " + username + "!");
//                 Server.log("[AUTH SUCCESS] User '" + username + "' authenticated from IP: " + clientIP);
//                 return true;

//             } else {
//                 attempts++;
//                 int remaining = maxAttempts - attempts;
//                 sendMessage("[ERROR] Invalid credentials. " + remaining + " attempt(s) remaining.");
//                 Server.log("[AUTH FAILED] Failed login attempt from IP: " + clientIP + " (attempt " + attempts + ")");
//             }
//         }

//         sendMessage("[BLOCKED] Too many failed attempts. Connection closed.");
//         Server.log("[BLOCKED] IP " + clientIP + " blocked after " + maxAttempts + " failed attempts.");
//         return false;
//     }

//     /**
//      * Send a message to this client
//      */
//     public void sendMessage(String message) {
//         if (output != null) {
//             output.println(message);
//         }
//     }
// }
