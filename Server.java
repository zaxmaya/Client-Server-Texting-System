import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class Server {
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    // Constructor to initialize the server
    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        pool = Executors.newFixedThreadPool(5);
    }

    // Start the server
    public void startServer() {
        pool.execute(this::acceptClients); // Handle client connections in a separate thread
        pool.execute(this::checkClientConnections); // Check client connections
        readServerInput(); // Handle server input
    }

    // Accept incoming client connections
    public void acceptClients() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    // Remove a client handler when a client disconnects
    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    // Send an image to all connected clients
    private void sendImageToAllClients(File imageFile) throws IOException {
        for (ClientHandler client : clients) {
            try {
                client.sendImage(imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Read server input for sending messages or images
    private void readServerInput() {
        Scanner scanner = new Scanner(System.in);
        printServerInstructions();
        while (true) {
            String input = scanner.nextLine();

            if ("quit".equalsIgnoreCase(input)) {
                break;
            } else if ("1".equals(input)) {
                System.out.println("Enter your message:");
                String message = scanner.nextLine();
                broadcastMessage(message);
            } else if ("2".equals(input)) {
                System.out.println("Enter the path of the image file to send:");
                String imagePath = scanner.nextLine();
                File imageFile = new File(imagePath);
                try {
                    sendImageToAllClients(imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            printServerInstructions();
        }
    }

    // Broadcast a message to all connected clients
    private void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            System.out.println("\tSending message to client: " + message); // Debugging line
            client.sendMessage(message);
        }
    }

    // Check the status of client connections and remove disconnected clients
    private void checkClientConnections() {
        while (true) {
            try {
                Thread.sleep(5000); // Check every 5 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<ClientHandler> disconnectedClients = new ArrayList<>();
            for (ClientHandler client : clients) {
                try {
                    int bytesRead = client.getClientSocket().getInputStream().read();
                    if (bytesRead == -1) {
                        // Connection is lost
                        System.err.println("Lost connection with client.");
                        disconnectedClients.add(client);
                    }
                } catch (IOException e) {
                    System.err.println("Lost connection with client.");
                    disconnectedClients.add(client);
                }
            }

            // Remove disconnected clients from the list
            for (ClientHandler disconnectedClient : disconnectedClients) {
                clients.remove(disconnectedClient);
            }
        }
    }

    // Stop the server
    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Display instructions for server input
    private void printServerInstructions() {
        System.out.println("Enter '1' to send a text message, '2' to send an image, or 'quit' to exit:");
    }

    // Main method to start the server
    public static void main(String[] args) throws IOException {
        try {
            // Get the server's IP address
            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("Server IP address: " + ip.getHostAddress());

            // Prompt for the port number
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the port number: ");
            int port = scanner.nextInt();

            Server server = new Server(port);
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// ClientHandler class handles individual client connections
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DataOutputStream out;

    public Socket getClientSocket() {
        return clientSocket;
    }

    // Display an image received from the client
    private void displayImage(byte[] imageBytes) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Received Image");
                frame.setSize(300, 300);
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                ImageIcon imageIcon = new ImageIcon(imageBytes);
                JLabel label = new JLabel(imageIcon);
                frame.add(label);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            out = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Send a confirmation message to the client
            System.out.println("Connection successful with client.");

            String dataType;
            while ((dataType = in.readLine()) != null) {
                if ("TEXT".equals(dataType)) {
                    String textMessage = in.readLine();
                    // Process text message
                    System.out.println("\tReceived from Client: " + textMessage); // Print the received message to the server's console
                    System.out.println("Enter '1' to send a text message, '2' to send an image, or 'quit' to exit:");
                } else if ("IMAGE".equals(dataType)) {
                    String imageName = in.readLine();
                    int imageSize = Integer.parseInt(in.readLine());
                    byte[] imageBytes = new byte[imageSize];

                    int bytesRead = 0;
                    while (bytesRead < imageSize) {
                        int result = clientSocket.getInputStream().read(imageBytes, bytesRead, imageSize - bytesRead);
                        if (result == -1) break; // End of stream
                        bytesRead += result;
                    }

                    // Optionally save the image to a file
                    FileOutputStream fos = new FileOutputStream(imageName);
                    fos.write(imageBytes);
                    fos.close();
                    System.out.println("Received image: " + imageName);
                    displayImage(imageBytes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Server.removeClient(this);
    }

    // Send an image to the client
    public void sendImage(File imageFile) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        try {
            dataOutputStream.writeUTF("IMAGE");
            dataOutputStream.writeUTF(imageFile.getName());
            dataOutputStream.writeInt(imageBytes.length);
            dataOutputStream.write(imageBytes, 0, imageBytes.length);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send a text message to the client
    public void sendMessage(String message) {
        try {
            out.writeUTF("TEXT"); // Indicate message type
            out.writeUTF(message); // Send message
            out.flush(); // Ensure data is sent immediately
        } catch (Exception e) {
            System.err.println("Error sending message to client: " + e.getMessage());
        }
    }
}
