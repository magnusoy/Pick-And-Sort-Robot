package main.java.utility;


import org.json.JSONObject;
import org.json.JSONArray;


/**
 * This class works as a binding point for
 * all the classes. The data stored here will
 * be a shared resource cross the system.
 */
public class Database {

    private Integer userCommand;                        // A number that instructs the Teensy
    private int shapeType;                              // Object type represented as int
    private final RequestRemoteData remoteData;         // REST API Call to remote server
    private final MovementPlanner movementPlanner;      // Handles the figure type to be sorted
    private final ControllerHandler controllerHandler;  // Parses data from Xbox controller
    private JSONObject jsonFromTeensy;                  // JSON format of the tracked object
    private JSONObject jsonToTeensy;                    // JSON format of the data that the Teensy will have

    /**
     * Database constructor, initializes the variables
     * and objects.
     */
    public Database() {
        this.userCommand = 0;
        this.remoteData = new RequestRemoteData();
        this.movementPlanner = new MovementPlanner();
        this.jsonFromTeensy = new JSONObject();
        this.jsonToTeensy = new JSONObject();
        this.controllerHandler = new ControllerHandler();
        this.shapeType = 10;                            // Has to be 10 to match picker state machine
    }

    /**
     * Returns all of the stored shapes.
     *
     * @return all of the stored shapes
     */
    public JSONArray getAllShapes() {
        this.remoteData.update();
        return this.remoteData.getAll();
    }

    /**
     * Set a new user command to be sent.
     *
     * @param userCommand, an Integer positive number.
     */
    public synchronized void setUserCommand(Integer userCommand) {
        this.userCommand = userCommand;
    }

    /**
     * Set a new shape type to be sent.
     *
     * @param shapeType, an Integer positive number.
     */
    public synchronized void putType(int shapeType) {
        this.shapeType = shapeType;
    }

    /**
     * Receives new JSON from Teensy.
     *
     * @param jsonFromTeensy received JSON from Teensy
     */
    public void setJsonFromTeensy(JSONObject jsonFromTeensy) {
        this.jsonFromTeensy = jsonFromTeensy;
    }

    /**
     * Puts Received data from xbox to parser.
     *
     * @param jsonObject data from xbox controller
     */
    public void putXboxControllerData(JSONObject jsonObject) {
        this.controllerHandler.setJsonObject(jsonObject);
    }


    /**
     * Returns received JSON from Teensy.
     *
     * @return received JSON from Teensy
     */
    public JSONObject getJsonFromTeensy() {
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
    public JSONObject getJsonToTeensy() {
        this.movementPlanner.setShapeType(this.shapeType);
        this.jsonToTeensy = this.movementPlanner.getShape();
        this.jsonToTeensy.put("command", this.userCommand);

        if (this.controllerHandler.getLength() > 0) {
            this.jsonToTeensy.put("manX", this.controllerHandler.getRightX());
            this.jsonToTeensy.put("manY", this.controllerHandler.getRightY());
            this.jsonToTeensy.put("speed", this.controllerHandler.getLeftY());
            this.jsonToTeensy.put("pick", this.controllerHandler.getButtonA());
            this.jsonToTeensy.put("drop", this.controllerHandler.getButtonX());
        }
        this.jsonToTeensy.put("size", this.movementPlanner.getSize());
        System.out.println(this.jsonFromTeensy.toString());
        //System.out.println(this.jsonToTeensy.toString());
        return this.jsonToTeensy;
    }
}
