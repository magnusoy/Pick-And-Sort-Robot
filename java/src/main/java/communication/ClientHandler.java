package main.java.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

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


        while (true) {
            try {
                this.dataOutputStream.writeUTF("ACK");
                received = this.dataInputStream.readUTF();

                if (received.equals("Exit")) {
                    System.out.println("Client " + this.socket + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    break;
                }

                // write on output stream based on the
                // answer from the client
                switch (received) {
                    case "Hello" :
                        toReturn = "Hello, how are you?";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "Goodbye" :
                        toReturn = "Goodbye to you too.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    default:
                        this.dataOutputStream.writeUTF("Invalid input");
                        break;
                }
            } catch (SocketException se) {
                se.printStackTrace();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
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
