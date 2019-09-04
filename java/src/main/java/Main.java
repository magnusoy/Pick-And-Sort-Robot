package main.java;

import main.java.communication.ArduinoHandler;
import main.java.communication.Server;
import main.java.utility.ArduinoData;

import java.io.IOException;

/**
 *
 */
public class Main {

    public static void main(String[] args) {
        ArduinoData arduinoData = new ArduinoData();

        Server server = new Server(5056, arduinoData);
        ArduinoHandler arduinoHandler = new ArduinoHandler(arduinoData);

        try {
            arduinoHandler.start();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
