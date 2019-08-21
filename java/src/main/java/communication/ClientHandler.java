package main.java.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler extends Thread {
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final Socket socket;

    public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.socket = socket;
    }

    @Override
    public void run() {
        String received = "";
        String toReturn = "";

        while (true) {
            try {
                this.dataOutputStream.writeUTF("Welcome message");

                received = this.dataInputStream.readUTF();

                if (received.equals("Exit")) {
                    System.out.println("Client " + this.socket + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    break;
                }
                // creating Date object
                Date date = new Date();

                // write on output stream based on the
                // answer from the client
                switch (received) {

                    case "Date" :
                        toReturn = this.dateFormat.format(date);
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    case "Time" :
                        toReturn = this.timeFormat.format(date);
                        this.dataOutputStream.writeUTF(toReturn);
                        break;

                    default:
                        this.dataOutputStream.writeUTF("Invalid input");
                        break;
                }
            } catch (IOException ioe) {
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
