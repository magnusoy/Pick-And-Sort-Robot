package main.java.communication;

import main.java.utility.Database;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Semaphore;


/**
 * ClientHandler uses a socket connection from the client.
 * Each client can GET and POST data. This makes it possible
 * to hand the commands to other processes and threads
 * in the system.
 */
public class ClientHandler implements Runnable {

    private final DataInputStream dataInputStream;      // Input from client
    private final DataOutputStream dataOutputStream;    // Output to client
    private final Socket socket;                        // Client socket
    private final Database database;                    // Shared resource
    private final Semaphore semaphore;                  // Limits the number of threads


    /**
     * ClientHandler constructor. Receives an already
     * connected socket, with assigned input stream and output stream.
     *
     * @param socket           client connection
     * @param dataInputStream  client inputstream
     * @param dataOutputStream client outputstream
     * @param database         shared resource
     */
    public ClientHandler(Socket socket, DataInputStream dataInputStream,
                         DataOutputStream dataOutputStream, Database database) {
        this.semaphore = new Semaphore(5, true);
        this.database = database;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.socket = socket;
    }

    /**
     * This works as an API for the clients. Making them able to
     * communicate, fetch and send data to other resources within
     * the system.
     */
    @Override
    public void run() {
        try {
            semaphore.acquire();

        String received;
        String toReturn;

        while (this.socket.isConnected()) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

                received = in.readLine();

                // Handling Xbox controller inputs
                if (received.substring(2).startsWith("POST/Controller")) {
                    JSONObject controllerData = extractControllerInputs(received);
                    this.database.putXboxControllerData(controllerData);
                }

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
                        this.database.setUserCommand(Commands.NOTHING.ordinal());
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Start":
                        this.database.setUserCommand(Commands.START.ordinal());
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Stop":
                        this.database.setUserCommand(Commands.STOP.ordinal());
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Reset":
                        this.database.setUserCommand(Commands.RESET.ordinal());
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Manual":
                        this.database.setUserCommand(Commands.MANUAL_CONTROL.ordinal());
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Automatic":
                        this.database.setUserCommand(Commands.AUTOMATIC_CONTROL.ordinal());
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Calibrate":
                        this.database.setUserCommand(Commands.CALIBRATE.ordinal());
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Configure":
                        this.database.setUserCommand(Commands.CONFIGURE.ordinal());
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/All":
                        this.database.putType(Commands.ALL_OBJECTS.ordinal());
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Squares":
                        this.database.putType(Commands.SQUARES.ordinal());
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Circles":
                        this.database.putType(Commands.CIRCLES.ordinal());
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Rectangles":
                        this.database.putType(Commands.RECTANGLES.ordinal());
                        toReturn = "received";
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "POST/Triangles":
                        this.database.putType(Commands.TRIANGLES.ordinal());
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
                    e.printStackTrace();
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            semaphore.release();
        }
    }

    /**
     * Parses the input from client to desired
     * JSON format.
     *
     * @param input received input
     * @return JSONObject with parsed input
     */
    private JSONObject extractControllerInputs(String input) {
        String stringToBeParsed = input.substring(17);
        return new JSONObject(stringToBeParsed);
    }
}
