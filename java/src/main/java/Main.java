package main.java;

import main.java.communication.SerialHandler;
import main.java.communication.SerialHandler2;
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
        SerialHandler2 serialHandler = new SerialHandler2(db);

        try {
            serialHandler.start();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
