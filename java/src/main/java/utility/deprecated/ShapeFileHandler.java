package main.java.utility.deprecated;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;


/**
 * ObjectHandler handles reading and writing from the
 * Shapes file.
 */
public class ShapeFileHandler {

    private static final String PATH_TO_SHAPES = "..\\resources\\Objects\\objects.json";

    /**
     *ObjecHandler constructor.
     */
    public ShapeFileHandler() {
    }

    /**
     * Returns the number of shapes remaining.
     *
     * @return the number of shapes remaining.
     */
    public int getSize() {
        JSONArray shapeList = new JSONArray();
        File file = new File(PATH_TO_SHAPES);

        try {
            String content = FileUtils.readFileToString(file, "utf-8");
            shapeList.put(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return shapeList.length();
    }

    /**
     * Returns all of the objects
     * stored as JSON Array.
     *
     * @return all of the objects
     */
    public synchronized JSONArray getAll() {
        JSONArray shapeList = new JSONArray();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(PATH_TO_SHAPES));
            String line = reader.readLine();
            while (line != null) {
                shapeList.put(line);
                line = reader.readLine();
            }
        reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return shapeList;
    }


    /**
     *  Returns one Object represented
     *  as a JSON Object.
     *
     * @param index, in list
     * @return a single JSON object
     */
    public synchronized JSONObject get(int index) {
        JSONArray shapeList = getAll();
        return new JSONObject(shapeList.get(index).toString());
    }
}

