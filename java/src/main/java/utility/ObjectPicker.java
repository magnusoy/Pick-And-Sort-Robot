package main.java.utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Iterator;

/**
 * ObjectPicker is controlling the object data
 * that is sent to trough the Serial over to
 * the Teensy.
 *
 */
public class ObjectPicker {

    private int type;                               // Object type represented as int
    private int size;                               // Number of objects left
    private final ObjectHandler objectHandler;      // Takes care of reading from file


    /**
     * ObjectPicker constructor initializes the
     * objecthandler that takes care of the file
     * all the located objects are stored.
     *
     * @param objectHandler, Filehandler for objects
     */
    public ObjectPicker(ObjectHandler objectHandler) {
        this.objectHandler = objectHandler;
        this.type = 0;
        this.size = 0;
    }

    /**
     * Sets the sort of type the robot
     * should pick.
     *
     * @param type, object type represented as int
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Returns object type represented as int
     *
     * @return type, object represented as int
     */
    public int getType() {
        return this.type;
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
    public JSONObject getObject() {
        String objectType = "";
        JSONObject object = null;
        this.size = this.objectHandler.getSize();

        if (this.size > 1) {
            switch (this.type) {
                case 10:
                    object = this.objectHandler.get(this.size-1);
                    break;

                case 11:
                    objectType = "square";
                    break;

                case 12:
                    objectType = "circle";
                    break;

                case 13:
                    objectType = "rectangle";
                    break;

                case 14:
                    objectType = "triangle";
                    break;

                default:
                    objectType = "";
                    break;
            }
            if (this.type != 10) {
                JSONArray jsonArray = this.objectHandler.getAll();
                object = this.getObjectByType(objectType, jsonArray);
            }
        }
        return object;
    }

    /**
     * Returns object matching the requested type.
     *
     * @param type, object represented as int
     * @param jsonArray, Array containing all objects
     *
     * @return object matching the conditions
     */
    private JSONObject getObjectByType(String type, JSONArray jsonArray) {
        JSONObject object = null;
        Iterator it = jsonArray.iterator();
        while ((it.hasNext()) && (object == null)) {
            JSONObject temp = (JSONObject) it.next();
            if (temp.get("type").toString().equalsIgnoreCase(type)) {
                object = temp;
            }
        }
        return object;
    }
}
