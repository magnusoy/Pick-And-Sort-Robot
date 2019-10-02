package main.java.communication;

import main.java.utility.Database;

import java.io.*;
import java.net.Socket;


/**
 * ClientHandler uses a socket for communication with the server.
 * Each client can GET and POST data. This makes it possible
 * to hand the commands to other processes and threads.
 */
public class ClientHandler extends Thread {

    private final DataInputStream dataInputStream;      // Input from client
    private final DataOutputStream dataOutputStream;    // Output to client
    private final Socket socket;                        // Client socket
    private final Database database;                    // Shared resource


    /**
     * ClientHandler constructor. Assign the given
     * input- and outputstreams to the client.
     *
     * @param socket           connected to the server
     * @param dataInputStream  inputstream object
     * @param dataOutputStream outputstream object
     */
    public ClientHandler(Socket socket, DataInputStream dataInputStream,
                         DataOutputStream dataOutputStream, Database database) {
        this.database = database;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.socket = socket;
    }

    /**
     * Starts a new thread that runs as long
     * the client is connected to the server.
     */
    @Override
    public void run() {
        String received = "";
        String toReturn = "";

        while (this.socket.isConnected()) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);

                received = in.readLine();

                switch (received) {
                    case "GET/Status":
                        toReturn = this.database.getJsonFromTeensy().toString();
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "GET/Objects":
                        toReturn = this.database.getAllShapes().toList().toString();
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Nothing":
                        this.database.setUserCommand(0);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Start":
                        this.database.setUserCommand(1);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Stop":
                        this.database.setUserCommand(2);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Reset":
                        this.database.setUserCommand(3);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Manual":
                        this.database.setUserCommand(4);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Automatic":
                        this.database.setUserCommand(5);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Calibrate":
                        this.database.setUserCommand(6);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Configure":
                        this.database.setUserCommand(7);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/All":
                        this.database.putType(10);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Squares":
                        this.database.putType(11);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Circles":
                        this.database.putType(12);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Rectangles":
                        this.database.putType(13);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Triangles":
                        this.database.putType(14);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "help":
                        toReturn = "GET/Status, GET/Objects, POST/Start, POST/Stop, POST/Reset" +
                                ",POST/Manual, POST/Automatic, POST/Calibrate, POST/All, POST/Squares" +
                                ", POST/Circles, POST/Triangles, POST/Rectangles";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    default:
                        this.dataOutputStream.writeUTF("Invalid input, type help");
                        break;
                }
            } catch (Exception e) {
                try {
                    this.dataInputStream.close();
                    this.dataOutputStream.close();
                    this.socket.close();
                    System.err.println("Lost connection: " + this.socket);
                    break;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        try {
            this.dataInputStream.close();
            this.dataOutputStream.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
