package main.java.communication;

import main.java.utility.Database;

import java.io.*;
import java.net.Socket;


/**
 *
 */
public class ClientHandler extends Thread {

    // Defining local variables
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final Socket socket;
    private Database db;

    /**
     * ClientHandler constructor. Assign the given
     * input- and outputstreams to the client.
     *
     * @param socket connected to the server
     * @param dataInputStream inputstream object
     * @param dataOutputStream outputstream object
     */
    public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream, Database database) {
        this.db = database;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.socket = socket;
    }

    /**
     * Starts a new thread that runs as long the client is connected to the server.
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

                if (received.equals("Exit")) {
                    System.out.println("Client " + this.socket + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    break;
                }

                // Write on outputstream based on the
                // answer from the client
                switch (received) {
                    case "GET/Status":
                        toReturn = this.db.getObj().toString();
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "GET/Objects":
                        toReturn = this.db.getObjects().toJSONString();
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Nothing":
                        this.db.putCommand(0);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Start":
                        this.db.putCommand(1);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Stop":
                        this.db.putCommand(2);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Reset":
                        this.db.putCommand(3);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Manual":
                        this.db.putCommand(4);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Automatic":
                        this.db.putCommand(5);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Calibrate":
                        this.db.putCommand(6);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/All":
                        this.db.putType(0);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Squares":
                        this.db.putType(1);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Circles":
                        this.db.putType(2);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Triangles":
                        this.db.putType(3);
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Rectangles":
                        this.db.putType(4);
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
