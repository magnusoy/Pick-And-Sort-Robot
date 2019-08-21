package main.java.communication;

import java.io.IOException;

public class ServerTest {

    public static void main(String[] args) {
        Server server = new Server(5056);

        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
