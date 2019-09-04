package main.java.communication;

import main.java.utility.ArduinoData;
import main.java.utility.ObjectHandler;

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
    private final ArduinoData arduinoData;
    private final ObjectHandler objectHandler;

    /**
     * ClientHandler constructor. Assign the given
     * input- and outputstreams to the client.
     *
     * @param socket connected to the server
     * @param dataInputStream inputstream object
     * @param dataOutputStream outputstream object
     */
    public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream, ArduinoData arduinoData) {
        this.arduinoData = arduinoData;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.socket = socket;
        this.objectHandler = new ObjectHandler("..\\resources\\Objects\\objects.json");
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
                        toReturn = this.arduinoData.get().toString();
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "GET/Objects":
                        //toReturn = "GET/Objects was called.";
                        toReturn = this.objectHandler.getAll().toString();
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Start":
                        toReturn = "GET/Start was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Stop":
                        toReturn = "POST/Stop was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Reset":
                        toReturn = "POST/Reset was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Manual":
                        toReturn = "POST/Manual was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Automatic":
                        toReturn = "POST/Automatic was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Calibrate":
                        toReturn = "POST/Calibrate was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/All":
                        toReturn = "POST/All was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Squares":
                        toReturn = "POST/Squares was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Circles":
                        toReturn = "POST/Circles was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Triangles":
                        toReturn = "POST/Triangle was called.";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Rectangles":
                        toReturn = "POST/Rectangles was called.";
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
