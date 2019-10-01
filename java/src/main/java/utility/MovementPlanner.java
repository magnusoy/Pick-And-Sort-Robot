package main.java.utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * MovementPlanner decides what should be sent to
 * Teensy.
 */
public class MovementPlanner {

    private int shapeType;                                // Object type represented as int
    private int size;                                     // Number of objects left
    private RequestRemoteData remoteData;                 // Fetching remote data

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
     * Returns object type represented as int
     *
     * @return type, object represented as int
     */
    public int getShapeType() {
        return this.shapeType;
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
        this.remoteData.update();
        this.size = this.remoteData.getSize();

        if (this.size > 0) {
            switch (this.shapeType) {
                case 10:
                    shape = this.remoteData.get(0);
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
                shape = this.getShapeByType(shapeType, jsonArray);
            }
        }
        return shape;
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
        while ((it.hasNext()) && (shape == null)) {
            JSONObject temp = (JSONObject) it.next();
            if (temp.get("type").toString().equalsIgnoreCase(type)) {
                shape = temp;
            }
        }
        return shape;
    }
}
