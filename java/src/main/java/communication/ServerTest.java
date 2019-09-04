package main.java.communication;

import main.java.utility.ArduinoData;

import java.io.IOException;

/**
 *
 */
public class ServerTest {
    public static void main(String[] args) {
        ArduinoData arduinoData = new ArduinoData();
        Server server = new Server(5056, arduinoData);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
