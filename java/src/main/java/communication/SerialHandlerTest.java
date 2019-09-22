package main.java.communication;
import main.java.utility.Database;
import org.json.JSONObject;

/**
 * Test experiment too see if code works
 * as expected.
 */
public class SerialHandlerTest {


    public static void main(String[] args) throws InterruptedException {
        Database db = new Database();
        SerialHandler serialHandler = new SerialHandler(db);
        serialHandler.run();

        System.out.println("Started");

        while(true) {
            JSONObject objRead = serialHandler.getJsonObject();
            String data = objRead.toString();
            System.out.println(data);

            JSONObject obj = new JSONObject();
            obj.put("type", new Integer(1));
            obj.put("x", new Integer(200));
            obj.put("y", new Integer(300));
            obj.put("num", new Integer(1));
            obj.put("command", new Integer(6));

            //JSONObject obj = db.getObjToSend();
            //serialHandler.sendData(obj);
        }
    }
}