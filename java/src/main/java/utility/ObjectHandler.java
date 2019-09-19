package main.java.utility;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * ObjectHandler handles reading and writing from the
 * Objects file.
 */
public class ObjectHandler {

    private String filePath;    // Filepath to Object data

    /**
     *ObjecHandler constructor initialize the
     * JSON parser and assigning the filepath
     * to the stored objects.
     *
     * @param filePath, Objects stored
     */
    public ObjectHandler(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Returns the number of objects remaining.
     *
     * @return the number of objects remaining.
     */
    public int getSize() {
        JSONArray objectList = new JSONArray();
        File file = new File(this.filePath);

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
            reader = new BufferedReader(new FileReader(this.filePath));
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

