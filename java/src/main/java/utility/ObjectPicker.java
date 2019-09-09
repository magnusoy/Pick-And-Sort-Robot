package main.java.utility;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Iterator;

/**
 *
 *
 */
public class ObjectPicker {

    private int type;                               //
    private final ObjectHandler objectHandler;      //


    /**
     *
     *
     * @param objectHandler
     */
    public ObjectPicker(ObjectHandler objectHandler) {
        this.objectHandler = objectHandler;
        this.type = 0;
    }

    /**
     *
     *
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     *
     *
     * @return
     */
    public int getType() {
        return this.type;
    }

    /**
     *
     *
     * @return
     */
    public JSONObject getObject() {
        String objectType = "";
        JSONObject object = null;
        int size = this.objectHandler.getSize();
        if (size > 1) {
            switch (this.type) {
                case 0:
                    object = this.objectHandler.get(size);
                    break;

                case 1:
                    objectType = "circle";
                    break;

                case 2:
                    objectType = "square";
                    break;

                case 3:
                    objectType = "triangle";
                    break;

                case 4:
                    objectType = "rectangle";
                    break;

                default:
                    objectType = "";
                    break;
            }
            if (this.type != 0) {
                JSONArray jsonArray = this.objectHandler.getAll();
                object = this.getObjectByType(objectType, jsonArray);
            }
        }
        return object;
    }

    /**
     *
     * @param type
     * @param jsonArray
     *
     * @return
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
