package main.java.utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

/**
 * ObjectHandler handles reading and writing from the
 * Objects file.
 */
public class ObjectHandler {

    JSONParser jsonParser;      // Reading JSON
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
        this.jsonParser = new JSONParser();
    }

    public int getSize() {
        JSONArray objectList = null;
        try (FileReader fileReader = new FileReader(this.filePath)){
            Object object = this.jsonParser.parse(fileReader);

            objectList = (JSONArray) object;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        assert objectList != null;
        return objectList.size();
    }

    /**
     * Returns all of the objects
     * stored as JSON Array.
     *
     * @return all of the objects
     */
    public synchronized JSONArray getAll() {
        JSONArray objectList = null;
        try (FileReader fileReader = new FileReader(this.filePath)){
            Object object = this.jsonParser.parse(fileReader);

            objectList = (JSONArray) object;

        } catch (IOException | ParseException e) {
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
        JSONObject jsonObject = null;
        try (FileReader fileReader = new FileReader(this.filePath)){
            Object object = this.jsonParser.parse(fileReader);
            JSONArray objectList = (JSONArray) object;

            if (objectList.size() > index) {
                jsonObject = (JSONObject) objectList.get(index);
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}

