package main.java.communication;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 *
 */
public class Client {

    // Define socket and input, output streams
    private InetAddress ip;
    private final Scanner in;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private Socket socket;

    /**
     * Client constructor.
     *
     * @param host where to connect
     * @param port communication port
     * @throws IOException
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
        this.dataInputStream = new DataInputStream(this.socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
    }

    /**
     * Establishing connection and
     * communication with the server is now possible.
     */
    public void connect()  {
        try {
            while (true) {
                System.out.println(this.dataInputStream.readUTF());
                String toSend = this.in.nextLine();
                this.dataOutputStream.writeUTF(toSend);

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
