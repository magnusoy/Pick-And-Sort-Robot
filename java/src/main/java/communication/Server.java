package main.java.communication;

import main.java.utility.Database;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server works as the core of the system.
 * Handling calls from clients and directing
 * the tasks to different processes and threads.
 */
public class Server {

    private ServerSocket serverSocket;    // Initialize socket
    private int port;                     // Socket port
    private Database db;                  // Shared resource

    /**
     * Server constructor. Initialize socket and
     * assign the given port to the socket.
     *
     * @param port, socket port
     * @param database, shared resource
     */
    public Server(int port, Database database) {
        this.db = database;
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

                // Assigning new thread for this client
                Thread clientThread = new ClientHandler(socket, dataInputStream, dataOutputStream, this.db);

                clientThread.start();
            } catch (Exception e) {
                socket.close();
                e.printStackTrace();
            }
        }
    }
}
