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
        Database db = new Database();

        Server server = new Server(5056, db);
        SerialHandler serialHandler = new SerialHandler(db);


        try {
            serialHandler.run();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while (true) {
                        serialHandler.sendData(db.getObjToSend());
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
