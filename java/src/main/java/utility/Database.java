package main.java.utility;

import org.json.JSONObject;
import org.json.JSONArray;


/**
 * This class works as a binding point for
 * all the classes. The data stored here will
 * be a shared resource cross the system.
 */
public class Database {

    private Integer userCommand;                 // A number that instructs the Teensy
    private int shapeType;                       // Object type represented as int
    private double manualX;                      // Manual X input from joystick
    private double manualY;                      // Manual Y input from joystick
    private final ShapeFileHandler shapeFileHandler;   // Handles the storage of the object data
    private final ShapePlanner shapePlanner;     // Handles the figure type to be sorted
    private JSONObject jsonFromTeensy;           // JSON format of the tracked object
    private JSONObject jsonToTeensy;             // JSON format of the data that the Teensy will have

    /**
     * Database constructor, initializes the variables
     * and objects.
     */
    public Database() {
        this.userCommand = 0;
        this.shapeFileHandler = new ShapeFileHandler();
        this.shapePlanner = new ShapePlanner(this.shapeFileHandler);
        this.jsonFromTeensy = new JSONObject();
        this.jsonToTeensy = new JSONObject();
        this.shapeType = 10;                     // Has to be 10 to match picker state machine
        this.manualX = 0.0;
        this.manualY = 0.0;
    }

    /**
     * Returns all of the stored shapes.
     *
     * @return all of the stored shapes
     */
    public synchronized JSONArray getAllShapes() {
        return this.shapeFileHandler.getAll();
    }

    /**
     * Set a new user command to be sent.
     *
     * @param userCommand, an Integer positive number.
     */
    public void setUserCommand(Integer userCommand) {
        this.userCommand = userCommand;
    }

    /**
     * Returns the user current command.
     *
     * @return userCommand, the current user command
     */
    public Integer getUserCommand() {
        return this.userCommand;
    }

    /**
     * Set a new shape type to be sent.
     *
     * @param shapeType, an Integer positive number.
     */
    public void putType(int shapeType) {
        this.shapeType = shapeType;
    }

    /**
     * Returns the current shape type.
     *
     * @return shapeType, the current shape type
     */
    public int getShapeType() {
        return this.shapeType;
    }

    /**
     * Receives new JSON from Teensy.
     *
     * @param jsonFromTeensy received JSON from Teensy
     */
    public synchronized void setJsonFromTeensy(JSONObject jsonFromTeensy) {
        this.jsonFromTeensy = jsonFromTeensy;
    }

    /**
     * Returns received JSON from Teensy.
     *
     * @return received JSON from Teensy
     */
    public synchronized JSONObject getJsonFromTeensy() {
        JSONObject temp;
        if (this.jsonFromTeensy != null) {
            temp = this.jsonFromTeensy;
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
    public synchronized JSONObject getJsonToTeensy() {
        this.shapePlanner.setShapeType(this.shapeType);
        this.jsonToTeensy = this.shapePlanner.getShape();
        this.jsonToTeensy.put("command", this.userCommand);
        this.jsonToTeensy.put("manX", this.manualX);
        this.jsonToTeensy.put("manY", this.manualY);
        this.jsonToTeensy.put("size", this.shapePlanner.getSize());
        this.userCommand = 0; // Resets the command after object has been created.
        return this.jsonToTeensy;
    }
}
