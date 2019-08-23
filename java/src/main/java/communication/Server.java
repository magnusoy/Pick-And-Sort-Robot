package main.java.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 */
public class Server {

    private ServerSocket serverSocket; // Initialize socket
    private int port;  // Socket port

    /**
     * Server constructor. Initialize socket and
     * assign the given port to the socket.
     *
     * @param port socket port
     */
    public Server(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the server and creates a new thread for
     * new client requests.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        while (true) {
            Socket socket = null;

            try {
                socket = serverSocket.accept();
                System.out.printf("A new client is connected: %s%n", socket);

                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                System.out.println("Assigning new thread for this client");
                Thread clientThread = new ClientHandler(socket, dataInputStream, dataOutputStream);

                clientThread.start();
            } catch (Exception e) {
                socket.close();
                e.printStackTrace();
            }
        }
    }
}
