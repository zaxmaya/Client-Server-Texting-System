import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String ipAddress;
    private int port;

    public Client(String ipAddress, int port) throws IOException {
        socket = new Socket(ipAddress, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Send a text message to the server
    public void sendMessage(String message) {
        System.out.println("\tSending message to server: " + message);
        out.println("TEXT");
        out.println(message);
        printClientInstructions();
    }

    // Send an image to the server
    public void sendImage(File imageFile) throws IOException {
        out.println("IMAGE");
        out.println(imageFile.getName());
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        out.println(imageBytes.length);
        socket.getOutputStream().write(imageBytes);
        printClientInstructions();
    }

    // Display an image received from the server
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

    // Attempt to reconnect to the server
    private boolean reconnect() {
        int maxRetries = 5;
        int retryDelayMillis = 2000; // 2 seconds delay
        for (int retryCount = 1; retryCount <= maxRetries; retryCount++) {
            try {
                // Attempt to reconnect to the server
                socket = new Socket(ipAddress, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("Reconnected to the server.");
                return true; // Reconnection successful
            } catch (IOException e) {
                System.err.println("Reconnection attempt " + retryCount + " failed.");
                try {
                    Thread.sleep(retryDelayMillis);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        System.err.println("Failed to reconnect after " + maxRetries + " attempts.");
        return false; // Reconnection failed
    }

    // Handle reconnection after a timeout
    private void handleTimeout() {
        System.err.println("Connection timed out. Reconnecting...");
        if (reconnect()) {
            System.out.println("Reconnection successful. Continuing to listen for data.");
        } else {
            System.err.println("Reconnection failed. Exiting...");
            stopClient();
        }
    }

    // Start the client
    public void startClient() {
        new Thread(() -> {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                while (true) {
                    String dataType;
                    try {
                        dataType = dataInputStream.readUTF(); // Read the data type
                    } catch (IOException e) {
                        e.printStackTrace();
                        handleTimeout();
                        break;
                    }

                    if ("TEXT".equals(dataType)) {
                        try {
                            String textMessage = dataInputStream.readUTF();
                            System.out.println("\tReceived from Server: " + textMessage);
                            System.out.println("Enter '1' to send a text message, '2' to send an image, or 'quit' to exit:");
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    } else if ("IMAGE".equals(dataType)) {
                        String imageName;
                        try {
                            imageName = dataInputStream.readUTF(); // Read image name
                        } catch (IOException e) {
                            // Handle the exception (e.g., log it)
                            e.printStackTrace();
                            break;
                        }
                        int imageSize;
                        try {
                            imageSize = dataInputStream.readInt(); // Read image size
                        } catch (IOException e) {
                            // Handle the exception (e.g., log it)
                            e.printStackTrace();
                            break;
                        }
                        byte[] imageBytes = new byte[imageSize];
                        try {
                            dataInputStream.readFully(imageBytes); // Read image bytes
                        } catch (IOException e) {
                            // Handle the exception (e.g., log it)
                            e.printStackTrace();
                            break;
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
                System.err.println("Client error: " + e.getMessage());
            } finally {
                stopClient();
            }
        }).start();

        // Main loop to send messages or images
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter '1' to send a text message, '2' to send an image, or 'quit' to exit:");
        String input;
        while (!(input = scanner.nextLine()).equalsIgnoreCase("quit")) {
            try {
                if ("1".equals(input)) {
                    System.out.println("Enter your message:");
                    String message = scanner.nextLine();
                    sendMessage(message);
                } else if ("2".equals(input)) {
                    System.out.println("Enter the path of the image file to send:");
                    String imagePath = scanner.nextLine();
                    File imageFile = new File(imagePath);
                    sendImage(imageFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        stopClient();
        System.out.println("Client Stopped.");
    }

    // Print client instructions
    private void printClientInstructions() {
        System.out.println("Enter '1' to send a text message, '2' to send an image, or 'quit' to exit:");
    }

    //Stop the client
    public void stopClient() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // Prompt for the server's IP address and port number
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the server IP address: ");
        String ipAddress = scanner.nextLine();
        System.out.print("Enter the server port number: ");
        int port = scanner.nextInt();
        scanner.nextLine();


        Client client = new Client(ipAddress, port);
        client.startClient();


        System.out.println("Enter messages to send to the server ('quit' to exit):");
        String message;
        while (!(message = scanner.nextLine()).equalsIgnoreCase("quit")) {
            client.sendMessage(message);
        }

        client.stopClient();
        System.out.println("Client Stopped.");
    }
}
