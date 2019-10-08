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
     * Returns the length of the json.
     *
     * @return length of the json
     */
    public int getLength() {
        return this.jsonObject.length();
    }

    /**
     * Returns value in X direction.
     *
     * @return Value in X direction
     */
    public double getRightX() {
        return Double.parseDouble(this.jsonObject.get("Right X").toString());
    }

    /**
     * Returns value in Y direction.
     *
     * @return Value in Y direction
     */
    public double getRightY() {
        return Double.parseDouble(this.jsonObject.get("Right Y").toString());
    }

    /**
     * Returns value in X direction.
     *
     * @return Value in X direction
     */
    public double getLeftX() {
        return Double.parseDouble(this.jsonObject.get("Left X").toString());
    }

    /**
     * Returns value in Y direction.
     *
     * @return Value in Y direction
     */
    public double getLeftY() {
        return Double.parseDouble(this.jsonObject.get("Left Y").toString());
    }

    /**
     * Returns true if button A is pressed.
     *
     * @return button A pressed
     */
    public boolean getButtonA() {
        return this.jsonObject.has("A");
    }

    /**
     * Returns true if button B is pressed.
     *
     * @return button B pressed
     */
    public boolean getButtonB() {
        return this.jsonObject.has("B");
    }

    /**
     * Returns true if button X is pressed.
     *
     * @return button X pressed
     */
    public boolean getButtonX() {
        return this.jsonObject.has("X");
    }

    /**
     * Returns true if button Y is pressed.
     *
     * @return button Y pressed
     */
    public boolean getButtonY() {
        return this.jsonObject.has("Y");
    }
}
