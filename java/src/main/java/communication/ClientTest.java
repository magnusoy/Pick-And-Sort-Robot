package main.java.communication;

import java.io.IOException;

/**
 * Test experiment too see if code works
 * as expected.
 */
public class ClientTest {
    public static void main(String[] args) {
        try {
            Client client = new Client("localhost", 5056);
            client.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
