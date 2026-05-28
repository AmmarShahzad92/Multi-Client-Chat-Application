import java.io.*;
import java.net.*;

/**
 * Client.java
 * Multi-Client Chat Application - Client Program
 * Connects to server on localhost:5000 via TCP
 * Two threads: one for sending, one for receiving messages
 */
public class Client {

    // Server connection details
    private static final String SERVER_HOST = "localhost";
    private static final int    SERVER_PORT = 5000;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("   Multi-Client Chat Application - Client");
        System.out.println("   Connecting to " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println("==============================================");

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {

            System.out.println("[CLIENT] Connected to server successfully.");

            // Set up streams
            BufferedReader serverInput  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter    serverOutput = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput    = new BufferedReader(new InputStreamReader(System.in));

            // Thread 1: Receive messages from server and print them
            Thread receiveThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverInput.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                    // Reaching here means server closed connection (returned null)
                    System.out.println("[CLIENT] Server closed the connection.");
                    System.exit(0); // Force exit to unblock main thread's Scanner
                } catch (IOException e) {
                    System.out.println("[CLIENT] Disconnected from server.");
                    System.exit(0); // Force exit to unblock main thread's Scanner
                }
            });

            receiveThread.setDaemon(true); // Dies when main thread ends
            receiveThread.start();

            // Thread 2 (main thread): Read user input and send to server
            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                // Input validation
                if (userMessage.trim().isEmpty()) continue;

                serverOutput.println(userMessage);

                // If user typed /quit, stop sending
                if (userMessage.trim().equalsIgnoreCase("/quit")) {
                    System.out.println("[CLIENT] Disconnecting...");
                    break;
                }
            }

        } catch (ConnectException e) {
            System.out.println("[ERROR] Could not connect to server at " + SERVER_HOST + ":" + SERVER_PORT);
            System.out.println("[ERROR] Make sure the server is running first.");
        } catch (UnknownHostException e) {
            System.out.println("[ERROR] Unknown host: " + SERVER_HOST);
        } catch (IOException e) {
            System.out.println("[ERROR] Connection error: " + e.getMessage());
        }

        System.out.println("[CLIENT] Session ended. Goodbye!");
    }
}
//Changes made in new code : * Added System.exit(0) to the receiveThread whenever it detects that the server stream returns null or throws an exception, forcefully killing the blocked userInput.readLine() scanner.

//--------------------------------------------------------------
//           >> Old Code <<

// import java.io.*;
// import java.net.*;

// /**
//  * Client.java
//  * Multi-Client Chat Application - Client Program
//  * Connects to server on localhost:5000 via TCP
//  * Two threads: one for sending, one for receiving messages
//  */
// public class Client {

//     // Server connection details
//     private static final String SERVER_HOST = "localhost";
//     private static final int    SERVER_PORT = 5000;

//     public static void main(String[] args) {
//         System.out.println("==============================================");
//         System.out.println("   Multi-Client Chat Application - Client");
//         System.out.println("   Connecting to " + SERVER_HOST + ":" + SERVER_PORT);
//         System.out.println("==============================================");

//         try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {

//             System.out.println("[CLIENT] Connected to server successfully.");

//             // Set up streams
//             BufferedReader serverInput  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//             PrintWriter    serverOutput = new PrintWriter(socket.getOutputStream(), true);
//             BufferedReader userInput    = new BufferedReader(new InputStreamReader(System.in));

//             // Thread 1: Receive messages from server and print them
//             Thread receiveThread = new Thread(() -> {
//                 try {
//                     String serverMessage;
//                     while ((serverMessage = serverInput.readLine()) != null) {
//                         System.out.println(serverMessage);
//                     }
//                 } catch (IOException e) {
//                     System.out.println("[CLIENT] Disconnected from server.");
//                 }
//             });

//             receiveThread.setDaemon(true); // Dies when main thread ends
//             receiveThread.start();

//             // Thread 2 (main thread): Read user input and send to server
//             String userMessage;
//             while ((userMessage = userInput.readLine()) != null) {
//                 // Input validation
//                 if (userMessage.trim().isEmpty()) continue;

//                 serverOutput.println(userMessage);

//                 // If user typed /quit, stop sending
//                 if (userMessage.trim().equalsIgnoreCase("/quit")) {
//                     System.out.println("[CLIENT] Disconnecting...");
//                     break;
//                 }
//             }

//         } catch (ConnectException e) {
//             System.out.println("[ERROR] Could not connect to server at " + SERVER_HOST + ":" + SERVER_PORT);
//             System.out.println("[ERROR] Make sure the server is running first.");
//         } catch (UnknownHostException e) {
//             System.out.println("[ERROR] Unknown host: " + SERVER_HOST);
//         } catch (IOException e) {
//             System.out.println("[ERROR] Connection error: " + e.getMessage());
//         }

//         System.out.println("[CLIENT] Session ended. Goodbye!");
//     }
// }
