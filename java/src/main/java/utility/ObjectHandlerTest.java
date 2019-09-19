package main.java.utility;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Test experiment too see if code works
 * as expected.
 */
public class ObjectHandlerTest {

    public static void main(String[] args) {
        ObjectHandler jsonHandler = new ObjectHandler();
        JSONObject object = jsonHandler.get(0);
        System.out.println(object.toString());
        int size = jsonHandler.getSize();
        System.out.println(size);
    }
}
