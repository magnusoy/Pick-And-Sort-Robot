package main.java.utility;

import org.json.simple.JSONObject;


/**
 * Test experiment too see if code works
 * as expected.
 */
public class ObjectHandlerTest {

    public static void main(String[] args) {
        String filePath = "..\\resources\\Objects\\locations.json";
        ObjectHandler jsonHandler = new ObjectHandler(filePath);
        JSONObject object = jsonHandler.get(9);
        System.out.println(object.toString());
    }
}
