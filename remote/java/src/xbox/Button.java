package xbox;

/**
 * Represents a single button on the xbox controller
 */
public class Button extends Key{

    private boolean state = false; // button pressed or not
    private boolean stateChanged = false;

    Button(String name){
        super(name);
    }

    /**
     * Gets the current button state
     * @return the current button state
     */
    public boolean getState() {
        return state;
    }

    /**
     * Sets the button state.
     * @param state the new button state.
     */
    public void setState(boolean state) {
        // Button has changed states when new and old state is different from each other
        this.stateChanged = (state != this.state);
        this.state = state;
    }

    /**
     * Returs true when button has changed states when state was set last time.
     * @return true when button has changed states when state was set last time.
     */
    public boolean isStateChanged() {
        return stateChanged;
    }
}
