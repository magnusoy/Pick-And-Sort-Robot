package main.java;

import main.java.communication.ArduinoHandler;
import main.java.communication.Server;
import main.java.utility.Database;
import org.json.JSONObject;

import java.io.IOException;

/**
 *
 */
public class Main {

    public static void main(String[] args) {
        Database db = new Database();

        Server server = new Server(5056, db);
        ArduinoHandler arduinoHandler = new ArduinoHandler(db);

        try {
            arduinoHandler.run();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true){
            arduinoHandler.sendData(db.getObjToSend());
        }
    }
}
