package main.java.utility;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;


/**
 * ObjectHandler handles reading and writing from the
 * Objects file.
 */
public class ObjectHandler {

    private static final String FILE_PATH = "..\\resources\\Objects\\objects.json";

    /**
     *ObjecHandler constructor.
     */
    public ObjectHandler() {
    }

    /**
     * Returns the number of objects remaining.
     *
     * @return the number of objects remaining.
     */
    public int getSize() {
        JSONArray objectList = new JSONArray();
        File file = new File(FILE_PATH);

        try {
            String content = FileUtils.readFileToString(file, "utf-8");
            objectList.put(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return objectList.length();
    }

    /**
     * Returns all of the objects
     * stored as JSON Array.
     *
     * @return all of the objects
     */
    public synchronized JSONArray getAll() {
        JSONArray objectList = new JSONArray();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(FILE_PATH));
            String line = reader.readLine();
            while (line != null) {
                objectList.put(line);
                line = reader.readLine();
            }
        reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return objectList;
    }


    /**
     *  Returns one Object represented
     *  as a JSON Object.
     *
     * @param index, in list
     * @return a single JSON object
     */
    public synchronized JSONObject get(int index) {
        JSONArray list = getAll();
        return new JSONObject(list.get(index).toString());
    }
}

