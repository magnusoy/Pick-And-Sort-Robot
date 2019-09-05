package main.java.communication;
import main.java.utility.Database;
import org.json.JSONObject;

public class ArduinoHandlerTest {


    public static void main(String[] args) {
        Database db = new Database();
        ArduinoHandler arduinoHandler = new ArduinoHandler(db);
        arduinoHandler.run();

        System.out.println("Started");


        boolean quit= false;
        while(!quit) {
            JSONObject objRead = arduinoHandler.getJsonObject();
            String data = objRead.toString();
            System.out.println(data);

            JSONObject obj = new JSONObject();
            obj.put("type", "circle");
            obj.put("x", new Integer(200));
            obj.put("y", new Integer(300));
            obj.put("num", new Integer(1));

            arduinoHandler.sendData(obj);
        }
    }
}