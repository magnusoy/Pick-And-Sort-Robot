package main.java.communication;

import main.java.utility.Database;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * Server works as the core of the system.
 * Handling calls from clients and directing
 * the tasks to different processes and threads.
 */
public class Server extends Thread{

    private ServerSocket serverSocket;    // Initialize socket
    private final Database database;      // Shared resource
    private ThreadPoolExecutor executor;  // Handle threads in the to pool


    /**
     * Server constructor. Initialize a server socket and
     * assign the given port to the socket.
     *
     * @param port, socket port
     * @param database, shared resource
     */
    public Server(int port, Database database) {

        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        this.database = database;
        // Socket port
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the server and creates a new thread for
     * every new client connecting. Assigning a new
     * clientHandler so the client can GET and POST
     * data.
     *
     */
    public void run() {
        while (true) {
           Socket socket = null;

            try {
                socket = serverSocket.accept();
                System.out.printf("A new client is connected: %s%n", socket);

                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                // Assigning new thread for this client
                this.executor.submit(new ClientHandler(socket, dataInputStream, dataOutputStream, this.database));

            } catch (Exception e) {
                assert socket != null;
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }
}
