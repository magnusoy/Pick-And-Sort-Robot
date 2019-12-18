package main.java.communication;

import main.java.utility.Database;

import java.io.IOException;

/**
 * Test experiment too see if code works
 * as expected.
 */
public class ServerTest {
    public static void main(String[] args) {
        Database db = new Database();
        Server server = new Server(5056, db);
        server.start();
    }
}
