package main.java.communication;

import main.java.utility.Database;

import java.io.IOException;

/**
 *
 */
public class ServerTest {
    public static void main(String[] args) {
        Database db = new Database();
        Server server = new Server(5056, db);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
