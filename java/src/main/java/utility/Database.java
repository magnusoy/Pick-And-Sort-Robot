package main.java.utility;

import org.json.JSONObject;
import org.json.simple.JSONArray;

/**
 * This class works as a binding point for
 * all the classes. The data stored here will
 * be a shared resource cross the system.
 */
public class Database {

    private Integer command;                    // A number that instructs the Teensy
    private final ObjectHandler objectHandler;  // Handles the storage of the object data
    private JSONObject obj;                     // JSON format of the tracked object
    private JSONObject objToSend;               // JSON format of the data that the Teensy will have

    /**
     * Database constructor, initializes the variables
     * and objects.
     */
    public Database() {
        this.command = 0;
        this.objectHandler = new ObjectHandler("..\\resources\\Objects\\objects.json");
        this.obj = new JSONObject();
        this.objToSend = new JSONObject();
    }

    /**
     * Returns all of the stored objects.
     *
     * @return all of the stored objects
     */
    public synchronized JSONArray getObjects() {
        return this.objectHandler.getAll();
    }

    /**
     * Set a new command to be sent.
     *
     * @param command, an Integer positive number.
     */
    public void putCommand(Integer command) {
        this.command = command;
    }

    /**
     * Returns the current command.
     *
     * @return command, the current command
     */
    public Integer getCommand() {
        return this.command;
    }

    /**
     * Receives new JSON from Teensy.
     *
     * @param obj received JSON from Teensy
     */
    public synchronized void putObj(JSONObject obj) {
        this.obj = obj;
    }

    /**
     * Returns received JSON from Teensy.
     *
     * @return received JSON from Teensy
     */
    public synchronized JSONObject getObj() {
        JSONObject temp;
        if (this.obj != null) {
            temp = this.obj;
        } else {
            temp = new JSONObject();
        }
        return temp;
    }

    /**
     * Retunrs the JSON structure that the
     * Teensy expects to receive.
     *
     * @return JSON structure
     */
    public synchronized JSONObject getObjToSend() {
        this.objToSend.put("command", this.command);
        // Just because path planning is not made yet.
        this.objToSend.put("type", new Integer(1));
        this.objToSend.put("x", new Integer(200));
        this.objToSend.put("y", new Integer(300));
        this.objToSend.put("num", new Integer(1));
        this.command = 0;
        return this.objToSend;
    }

}
