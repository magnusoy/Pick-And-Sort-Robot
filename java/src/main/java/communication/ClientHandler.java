package main.java.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


/**
 *
 */
public class ClientHandler extends Thread {

    // Defining local variables
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final Socket socket;

    /**
     * ClientHandler constructor. Assign the given
     * input- and outputstreams to the client.
     *
     * @param socket connected to the server
     * @param dataInputStream inputstream object
     * @param dataOutputStream outputstream object
     */
    public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
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
        int packageNumber = 0;


        while (this.socket.isConnected()) {
            try {
                this.dataOutputStream.writeUTF("ACK " + packageNumber);
                received = this.dataInputStream.readUTF();

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
                    case "GET/Status" :
                        toReturn = "GET/Status was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "GET/ArduinoData" :
                        toReturn = "GET/ArduinoData was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "GET/Objects":
                        toReturn = "GET/Objects was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/UpdateGUI":
                        toReturn = "POST/UpdateGUI was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "help":
                        toReturn = "help was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    default:
                        this.dataOutputStream.writeUTF("Invalid input");
                        break;
                }
                packageNumber ++;
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
