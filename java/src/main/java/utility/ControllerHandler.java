package main.java.utility;

import org.json.JSONObject;

/**
 * Parses data from Xbox controller.
 */
public class ControllerHandler {

    private JSONObject jsonObject;                 // Data Received from Xbox-controller

    /**
     * ControllerHandler constructor, initializes the JSONObject
     */
    public ControllerHandler() {
        this.jsonObject = new JSONObject();
    }

    /**
     * Set new data in JSONObject.
     *
     * @param jsonObject new data
     */
    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    /**
     * Returns value in X direction.
     *
     * @return Value in X direction
     */
    public double getRightX() {
        return Double.parseDouble(this.jsonObject.getString("Right X"));
    }

    /**
     * Returns value in Y direction.
     *
     * @return Value in Y direction
     */
    public double getRightY() {
        return Double.parseDouble(this.jsonObject.getString("Right Y"));
    }

    /**
     * Returns value in X direction.
     *
     * @return Value in X direction
     */
    public double getLeftX() {
        return Double.parseDouble(this.jsonObject.getString("Left X"));
    }

    /**
     * Returns value in Y direction.
     *
     * @return Value in Y direction
     */
    public double getLeftY() {
        return Double.parseDouble(this.jsonObject.getString("Left Y"));
    }

    /**
     * Returns true if button A is pressed.
     *
     * @return button A pressed
     */
    public boolean getButtonA() {
        return this.jsonObject.has("Button A");
    }

    /**
     * Returns true if button B is pressed.
     *
     * @return button B pressed
     */
    public boolean getButtonB() {
        return this.jsonObject.has("Button B");
    }

    /**
     * Returns true if button C is pressed.
     *
     * @return button C pressed
     */
    public boolean getButtonC() {
        return this.jsonObject.has("Button C");
    }
}
