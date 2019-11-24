package main.java.communication;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * This class implements client socket (also called just "socket"). 
 * A socket is an endpoint for communication between two machines.
 */
public class Client {
    
    private InetAddress ip;                             // Host name to server
    private final Scanner in;                           // Scanner for userinput
    private final DataInputStream dataInputStream;      // Input from user
    private final DataOutputStream dataOutputStream;    // Output to server
    private Socket socket;                              // Connection socket

    /**
     * Client constructor.
     * Creates a stream socket and connects it to the
     * specified port number at the specified IP address.
     *
     * @param host where to connect (localhost)
     * @param port communication port (5056)
     * @throws IOException, failed or interrupted I/O operation
     */
    public Client(String host, int port) throws IOException {
        this.in = new Scanner(System.in);
        try {
            this.ip = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            this.socket = new Socket(this.ip, port);
        } catch (ConnectException ce) {
            ce.printStackTrace();
        }
        assert this.socket != null;
        this.dataInputStream = new DataInputStream(this.socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
    }

    /**
     * Establishing connection and
     * communication with the TCP server
     * through the assigned socket.
     *
     * Stays connected until the client writes
     * "Exit" to disconnect.
     */
    public void connect()  {
        try {
            while (true) {
                System.out.println(this.dataInputStream.readUTF());
                String toSend = this.in.nextLine();
                this.dataOutputStream.writeUTF(toSend + "\n");

                if (toSend.equals("Exit")) {
                    System.out.println("Closing this connection: " + this.socket);
                    this.socket.close();
                    System.out.println("Connection closed.");
                    break;
                }
                String received = this.dataInputStream.readUTF();
                System.out.println(received);
            }
            this.in.close();
            this.dataInputStream.close();
            this.dataOutputStream.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
