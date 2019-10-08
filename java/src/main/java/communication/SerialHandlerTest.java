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
            JSONObject objRead = serialHandler.getJsonFromTeensy();
            String data = objRead.toString();
            System.out.println(data);

            JSONObject obj = new JSONObject();
            obj.put("type", Integer.valueOf(1));
            obj.put("x", Integer.valueOf(200));
            obj.put("y", Integer.valueOf(300));
            obj.put("num", Integer.valueOf(1));
            obj.put("command", Integer.valueOf(6));

            //JSONObject obj = db.getObjToSend();
            //serialHandler.sendData(obj);
        }
    }
}