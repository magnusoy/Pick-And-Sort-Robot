package main.java;

import main.java.communication.SerialHandler;
import main.java.communication.Server;
import main.java.utility.Database;

import java.io.IOException;

/**
 * Test experiment too see if code works
 * as expected.
 */
public class Main {

    public static void main(String[] args) {
        Database database = new Database();
        Server server = new Server(5056, database);
        SerialHandler serialHandler = new SerialHandler(database);

        try {
            serialHandler.run();
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (true) {
                    serialHandler.sendData(database.getJsonToTeensy());
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
