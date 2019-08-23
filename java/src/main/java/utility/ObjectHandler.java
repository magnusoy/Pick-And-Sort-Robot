package main.java.utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

public class ObjectHandler {

    JSONParser jsonParser;
    private String filePath;

    public ObjectHandler(String filePath) {
        this.filePath = filePath;
        this.jsonParser = new JSONParser();
    }

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

