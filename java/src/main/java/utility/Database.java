package main.java.utility;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;

/**
 * This class works as a binding point for
 * all the classes. The data stored here will
 * be a shared resource cross the system.
 */
public class Database {

    private Integer command;                     // A number that instructs the Teensy
    private int type;                            // Object type represented as int
    private double manualX;                      // Manual X input from joystick
    private double manualY;                      // Manual Y input from joystick
    private final ObjectHandler objectHandler;   // Handles the storage of the object data
    private final ObjectPicker objectPicker;     // Handles the figure type to be sorted
    private JSONObject obj;                      // JSON format of the tracked object
    private JSONObject objToSend;                // JSON format of the data that the Teensy will have

    /**
     * Database constructor, initializes the variables
     * and objects.
     */
    public Database() {
        this.command = 0;
        this.objectHandler = new ObjectHandler();
        this.objectPicker = new ObjectPicker(this.objectHandler);
        this.obj = new JSONObject();
        this.objToSend = new JSONObject();
        this.type = 10;      // Has to be 10 to match picker cases
        this.manualX = 0.0;
        this.manualY = 0.0;
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
     * Set a new type to be sent.
     *
     * @param type, an Integer positive number.
     */
    public void putType(int type) {
        this.type = type;
    }

    /**
     * Returns the current command.
     *
     * @return type, the current command
     */
    public int getType() {
        return this.type;
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
     * Returns the JSON structure that the
     * Teensy expects to receive.
     *
     * @return JSON structure
     */
    public synchronized JSONObject getObjToSend() {
        this.objectPicker.setType(this.type);
        this.objToSend = this.objectPicker.getObject();
        this.objToSend.put("command", this.command);
        this.objToSend.put("manX", this.manualX);
        this.objToSend.put("manY", this.manualY);
        this.objToSend.put("size", this.objectPicker.getSize());
        this.command = 0; // Resets the command after object has been created.
        return this.objToSend;
    }

}
