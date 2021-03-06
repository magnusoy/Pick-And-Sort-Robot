package main.java.utility;

import main.java.communication.Commands;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * MovementPlanner decides what should be sent to
 * Teensy.
 */
public class MovementPlanner {

    private int shapeType;                                // Object type represented as int
    private int size;                                     // Number of objects left
    private final RequestRemoteData remoteData;           // Fetching remote data

    /**
     * MovementPlanner constructor initializes the
     * remoteData that fetches new data from server.
     */
    public MovementPlanner() {
         this.remoteData = new RequestRemoteData();
         this.shapeType = 10;
         this.size = 0;
    }

    /**
     * Sets the sort of type the robot
     * should pick.
     *
     * @param shapeType, (circle, rectangle, square, triangle)
     */
    public void setShapeType(int shapeType) {
        this.shapeType = shapeType;
    }

    /**
     * Returns number of objects left
     *
     * @return numbers of objects left
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the object that will be
     * picked. It is based on the type
     * variable.
     *
     * @return JSON object to be picked
     */
    public JSONObject getShape() {
        String shapeType = "";
        JSONObject shape = null;
        JSONObject temp = null;
        this.remoteData.update();
        this.size = this.remoteData.getSize();

        if (this.size > 0) {
            switch (this.shapeType) {
                case 10:
                    temp = this.remoteData.get(0);
                    break;

                case 11:
                    shapeType = "square";
                    break;

                case 12:
                    shapeType = "circle";
                    break;

                case 13:
                    shapeType = "rectangle";
                    break;

                case 14:
                    shapeType = "triangle";
                    break;

                default:
                    shapeType = "";
                    break;
            }
            if (this.shapeType != 10) {
                JSONArray jsonArray = this.remoteData.getAll();
                temp = this.getShapeByType(shapeType, jsonArray);
            }
            assert temp != null;

            shape = parseObjectType(temp);
        } else {
            shape = new JSONObject();
        }
        return shape;
    }

    /**
     * Changes the object type to an integer code,
     * as this is what the Teensy expects.
     *
     * @param obj JSON to be sent
     *
     * @return modified JSON with correct format
     */
    private JSONObject parseObjectType(JSONObject obj) {
        try {
            String type = (String) obj.get("type");
            switch (type) {
                case "circle":
                    obj.put("type", 12);
                    break;
                case "rectangle":
                    obj.put("type", 13);
                    break;
                case "square":
                    obj.put("type", 11);
                    break;
                case "triangle":
                    obj.put("type", 14);
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            obj.put("type", 0);
        }
        return obj;
    }

    /**
     * Returns shape matching the requested type.
     *
     * @param type, object represented as int
     * @param shapeList, Array containing all objects
     *
     * @return object matching the conditions
     */
    private JSONObject getShapeByType(String type, JSONArray shapeList) {
        JSONObject shape = null;
        Iterator it = shapeList.iterator();
        int i = 0;
        while ((it.hasNext()) && (shape == null)) {
            JSONObject temp = null;
            String tmpString = it.next().toString();
            if (tmpString.length() > 15) {
                if (i == 0) {
                    temp = new JSONObject(tmpString.substring(1));
                } else {
                    temp = new JSONObject(tmpString);
                }
                if (temp.get("type").toString().equalsIgnoreCase(type)) {
                    shape = temp;
                }
            } else {
                shape = new JSONObject();
            }
            i += 1;
        }
        return shape;
    }
}
