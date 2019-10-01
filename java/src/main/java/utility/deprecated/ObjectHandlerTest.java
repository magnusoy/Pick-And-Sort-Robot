package main.java.utility.deprecated;

import org.json.JSONObject;


/**
 * Test experiment too see if code works
 * as expected.
 */
public class ObjectHandlerTest {

    public static void main(String[] args) {
        ShapeFileHandler jsonHandler = new ShapeFileHandler();
        JSONObject object = jsonHandler.get(0);
        System.out.println(object.toString());
        int size = jsonHandler.getSize();
        System.out.println(size);
    }
}
