package main.java;

import main.java.communication.SerialHandler;
import main.java.communication.Server;
import main.java.utility.Database;

import java.io.IOException;

/**
 *
 */
public class Main {

    public static void main(String[] args) {
        Database db = new Database();

        Server server = new Server(5056, db);
        SerialHandler serialHandler = new SerialHandler(db);
        Thread thread = new Thread(() -> {
            while (true) {
                serialHandler.sendData(db.getObjToSend());
            }
        });

        try {
            serialHandler.run();
            thread.start();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
